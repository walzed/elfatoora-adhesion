package tn.tn.elfatoora.entity;

import java.time.Instant;
import javax.persistence.*;

@Entity
@Table(name = "oidc_auth_log")
public class OidcAuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "state", nullable = false, unique = true, length = 200)
    private String state;

    @Column(name = "code_verifier", nullable = false, length = 500)
    private String codeVerifier;

    @Column(name = "auth_code", length = 2000)
    private String authCode;

    @Column(name = "id_token", length = 4000)
    private String idToken;

    @Column(name = "access_token", length = 4000)
    private String accessToken;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCodeVerifier() { return codeVerifier; }
    public void setCodeVerifier(String codeVerifier) { this.codeVerifier = codeVerifier; }

    public String getAuthCode() { return authCode; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }

    public String getIdToken() { return idToken; }
    public void setIdToken(String idToken) { this.idToken = idToken; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
