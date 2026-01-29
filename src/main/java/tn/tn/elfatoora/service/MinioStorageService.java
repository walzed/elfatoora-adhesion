package tn.tn.elfatoora.service;

import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 

@Service
public class MinioStorageService {

    private final MinioClient minioClient;
    private final String bucket;
    
    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    public MinioStorageService(MinioClient minioClient,
                              @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
    }

    public void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

 

 public void upload(String objectKey, MultipartFile file) throws Exception {
     // 1. On récupère le flux original et on l'enveloppe dans un Buffer
     // Taille du buffer recommandée : 8 Ko à 16 Ko
     try (InputStream inputStream = new BufferedInputStream(file.getInputStream())) {
         
         minioClient.putObject(
             PutObjectArgs.builder()
                 .bucket(bucket)
                 .object(objectKey)
                 // On passe le flux bufferisé. 
                 // Le "-1" indique au SDK de gérer lui-même la taille des parties (part size)
                 .stream(inputStream, file.getSize(), -1) 
                 .contentType(file.getContentType())
                 .build()
         );
     } catch (Exception e) {
         log.error("Erreur lors du streaming vers MinIO pour la clé : {}", objectKey, e);
         throw new Exception("Erreur technique lors du stockage du document.");
     }
 }
    
 
 public String uploadAndComputeHash(String objectKey, MultipartFile file) throws Exception {
	    // Initialisation de l'algorithme SHA-256
	    MessageDigest md = MessageDigest.getInstance("SHA-256");
	    
	    try (InputStream is = file.getInputStream();
	         // Le DigestInputStream calcule le hash pendant que MinIO lit le flux
	         DigestInputStream dis = new DigestInputStream(new BufferedInputStream(is), md)) {
	        
	        minioClient.putObject(
	            PutObjectArgs.builder()
	                .bucket(bucket)
	                .object(objectKey)
	                .stream(dis, file.getSize(), -1) // -1 laisse MinIO gérer la taille des parts
	                .contentType(file.getContentType())
	                .build()
	        );
	        
	        // Récupération du hash (tableau de bytes)
	        byte[] digest = md.digest();
	        
	        // Conversion manuelle des bytes en Hexadécimal (car HexFormat n'existe pas en Java 8)
	        StringBuilder sb = new StringBuilder();
	        for (byte b : digest) {
	            sb.append(String.format("%02x", b));
	        }
	        return sb.toString();
	        
	    } catch (Exception e) {
	        log.error("Erreur lors de l'upload/hachage du fichier {} : {}", objectKey, e.getMessage());
	        throw e;
	    }
	}

    public InputStream download(String objectKey) throws Exception {
        ensureBucket();
        return minioClient.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
    }
}
