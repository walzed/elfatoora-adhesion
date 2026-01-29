package tn.tn.elfatoora.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OidcUserInfoDTO {
    private String sub;

    @JsonProperty("preferred_username")
    private String preferredUsername;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;

    // selon ton impl, tu utilises fatherName/motherName/code/nbf...
    private String fatherName;
    private String motherName;
    private String code;
    private Long nbf;

    private String scope;
    private String iss;
    private String aud;
    private Long exp;
    private Long iat;
    private String jti;

    private String message;

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getPreferredUsername() { return preferredUsername; }
    public void setPreferredUsername(String preferredUsername) { this.preferredUsername = preferredUsername; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Long getNbf() { return nbf; }
    public void setNbf(Long nbf) { this.nbf = nbf; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getIss() { return iss; }
    public void setIss(String iss) { this.iss = iss; }

    public String getAud() { return aud; }
    public void setAud(String aud) { this.aud = aud; }

    public Long getExp() { return exp; }
    public void setExp(Long exp) { this.exp = exp; }

    public Long getIat() { return iat; }
    public void setIat(Long iat) { this.iat = iat; }

    public String getJti() { return jti; }
    public void setJti(String jti) { this.jti = jti; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
