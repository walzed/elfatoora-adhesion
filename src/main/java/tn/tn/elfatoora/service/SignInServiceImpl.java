package tn.tn.elfatoora.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import tn.tn.elfatoora.dto.OidcUserInfoDTO;
import tn.tn.elfatoora.dto.ReponseOpenIdDTO;
import tn.tn.elfatoora.dto.ResponseMobileIdOpenDTO;
import tn.tn.elfatoora.entity.OidcAuthLog;
import tn.tn.elfatoora.repo.OidcAuthLogRepository;
import tn.tn.elfatoora.util.StateOpenIdGenerator;
import tn.tn.elfatoora.util.SslInsecureHelper;

@Service
public class SignInServiceImpl implements SignInService {

    private static final Logger logger = LoggerFactory.getLogger(SignInServiceImpl.class);

    @Value("${oidc.url.authorize}")
    private String oidcUrlAuthorize;

    @Value("${oidc.url.logout}")
    private String oidcUrlLogout;

    @Value("${oidc.url.user-info}")
    private String oidcUrlUserInfo;

    @Value("${oidc.scopes}")
    private String scopes;

    @Value("${oidc.url.get-token}")
    private String oidcUrlGetToken;

    @Value("${oidc.client-secret}")
    private String oidcClientSecret;

    @Value("${oidc.grant-type}")
    private String oidcGrantType;

    @Value("${oidc.client-id}")
    private String oidcClientId;

    @Value("${oidc.redirect-uri}")
    private String oidcRedirectUri; ///sign-in/callback

    @Value("${oidc.code-challenge-method}")
    private String oidcCodeChallengeMethod;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Value("${oidc.ssl.insecure}")
    private boolean sslInsecure;

    private final StateOpenIdGenerator stateOpenIdGenerator;
    private final OidcAuthLogRepository authLogRepository;

    public SignInServiceImpl(StateOpenIdGenerator stateOpenIdGenerator,
                             OidcAuthLogRepository authLogRepository) {
        this.stateOpenIdGenerator = stateOpenIdGenerator;
        this.authLogRepository = authLogRepository;
    }

    private RestTemplate restTemplateNoHostnameVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
            } };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClients.custom()
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build()
            );

            return new RestTemplate(requestFactory);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create RestTemplate without hostname verification", e);
        }
    }

    private RestTemplate chooseRestTemplate() {
        if (!sslInsecure) {
            return new RestTemplate();
        }
        // si ton helper SslInsecureHelper.trustAll() est déjà global, c’est OK,
        // mais on garde aussi un RestTemplate permissif (utile selon JVM / client)
        try {
            SslInsecureHelper.trustAll();
        } catch (Exception ex) {
            logger.warn("SslInsecureHelper.trustAll failed: {}", ex.getMessage());
        }
        return restTemplateNoHostnameVerification();
    }

    @Override
    public ResponseMobileIdOpenDTO signInAuthorise(String baseUrl) {
        String codeVerifier = generateCodeVerifier();
        String codeChallenge;
        try {
            codeChallenge = generateCodeChallenge(codeVerifier);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        String state = stateOpenIdGenerator.generateState();

        // persister state + codeVerifier
        OidcAuthLog log = new OidcAuthLog();
        log.setState(state);
        log.setCodeVerifier(codeVerifier);
        authLogRepository.save(log);

        String callback = resolveBaseUrl(baseUrl) + oidcRedirectUri;

        String authorizeUrl =
            oidcUrlAuthorize
            + "?response_type=code"
            + "&client_id=" + urlEnc(oidcClientId)
            + "&scope=" + urlEnc(scopes)
            + "&redirect_uri=" + urlEnc(callback)
            + "&state=" + urlEnc(state)
            + "&code_challenge_method=" + urlEnc(oidcCodeChallengeMethod)
            + "&code_challenge=" + urlEnc(codeChallenge)
            + "&response_mode=query";

        ResponseMobileIdOpenDTO dto = new ResponseMobileIdOpenDTO();
        dto.setState(state);
        dto.setMessage(authorizeUrl);

        logger.info("OIDC authorize URL built for state={}", state);
        logger.info("OIDC authorize full URL: {}", authorizeUrl);
        logger.info("OIDC callback used: {}", callback);
        logger.info("OIDC client_id used: {}", oidcClientId);
        logger.info("OIDC scopes used: {}", scopes);

        return dto;
    }

    @Override
    public ReponseOpenIdDTO postDataToExternalApi(String baseUrl, String code, String state) throws Exception {
        Optional<OidcAuthLog> opt = authLogRepository.findByState(state);
        if (!opt.isPresent()) {
            logger.warn("No auth log found for state={}", state);
            return null;
        }

        OidcAuthLog log = opt.get();
        log.setAuthCode(code);
        authLogRepository.save(log);

        String callback = resolveBaseUrl(baseUrl) + oidcRedirectUri;

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("grant_type", oidcGrantType);
        formData.add("client_secret", oidcClientSecret);
        formData.add("redirect_uri", callback);
        formData.add("client_id", oidcClientId);
        formData.add("code_verifier", log.getCodeVerifier());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        RestTemplate rt = chooseRestTemplate();

        try {
            ResponseEntity<ReponseOpenIdDTO> responseEntity =
                rt.exchange(oidcUrlGetToken, HttpMethod.POST, requestEntity, ReponseOpenIdDTO.class);

            ReponseOpenIdDTO body = responseEntity.getBody();
            if (body != null) {
                log.setIdToken(body.getId_token());
                log.setAccessToken(body.getAccess_token());
                authLogRepository.save(log);
            }

            logger.info("OIDC token exchange OK for state={}", state);
            return body;

        } catch (RestClientException ex) {
            logger.error("OIDC token exchange failed for state={}: {}", state, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public OidcUserInfoDTO userInfo(String accessToken) throws Exception {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate rt = chooseRestTemplate();

        ResponseEntity<OidcUserInfoDTO> response =
            rt.exchange(oidcUrlUserInfo, HttpMethod.GET, entity, OidcUserInfoDTO.class);

        return response.getBody();
    }

    @Override
    public String buildLogoutUrl(String baseUrl, String state) {
        Optional<OidcAuthLog> opt = authLogRepository.findByState(state);
        if (!opt.isPresent() || opt.get().getIdToken() == null) {
            return null;
        }

        String idToken = opt.get().getIdToken();
        String postLogoutRedirect = resolveBaseUrl(baseUrl) + "/";

        // Selon le doc Mobile-ID: ajout de clientid
        return oidcUrlLogout
            + "?id_token_hint=" + urlEnc(idToken)
            + "&post_logout_redirect_uri=" + urlEnc(postLogoutRedirect)
            + "&clientid=" + urlEnc(oidcClientId);
    }

    private String resolveBaseUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            return stripTrailingSlash(baseUrl.trim());
        }
        return stripTrailingSlash(publicBaseUrl);
    }

    private static String stripTrailingSlash(String s) {
        while (s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String urlEnc(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public static String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }
}
