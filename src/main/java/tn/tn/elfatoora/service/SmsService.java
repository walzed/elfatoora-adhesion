package tn.tn.elfatoora.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class SmsService {

    @Value("${app.sms.api-key:28a6f75d42a91088b5eb2444f42479c7}")
    private String apiKey;

    @Value("${app.sms.api-url:https://wbm.tn/wbmonitor/send/webapi/v5/send_generic}")
    private String apiURL;

    @Async
    public void sendOTP(String phoneNumber, String msg) {
        try {
            // 1. Initialiser le SSL (pour ignorer les erreurs de certificat comme dans l'exemple)
            configureSSL();

            // 2. Préparer les données
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
            String dateStr = dateFormat.format(new Date());

            // 3. Construire le JSON
            JsonObjectBuilder postData = Json.createObjectBuilder()
                    .add("type", 0) // Latin
                    .add("auto_detect", 1)
                    .add("dt", dateStr)
                    .add("hr", "00")
                    .add("mn", "00")
                    .add("label", "El Fatoora") // Ton Sender ID
                    .add("ref", "OTP-" + System.currentTimeMillis())
                    .add("dest_num", "+216"+phoneNumber)
                    .add("msg", msg);

            String jsonPayload = postData.build().toString();

            // 4. Connexion HTTP
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "ElFatoora-Portal/1.0");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("X-API-Key", apiKey);
            con.setDoOutput(true);

            // 5. Envoi
            try (OutputStream os = con.getOutputStream();
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8))) {
                bw.write(jsonPayload);
                bw.flush();
            }

            // 6. Lecture Réponse (Optionnel pour log)
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("responseCode " + responseCode);
                System.out.println("SMS envoyé avec succès à " + phoneNumber);
            } else {
                System.err.println("Erreur envoi SMS: " + responseCode);
            }

        } catch (Exception e) {
            System.err.println("Exception lors de l'envoi SMS: " + e.getMessage());
        }
    }

    private void configureSSL() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}