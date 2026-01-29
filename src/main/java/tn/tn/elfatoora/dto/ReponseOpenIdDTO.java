package tn.tn.elfatoora.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReponseOpenIdDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    // champs d'erreur possibles
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public String getAccess_token() { return accessToken; }
    public void setAccess_token(String accessToken) { this.accessToken = accessToken; }

    public String getId_token() { return idToken; }
    public void setId_token(String idToken) { this.idToken = idToken; }

    public String getRefresh_token() { return refreshToken; }
    public void setRefresh_token(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpires_in() { return expiresIn; }
    public void setExpires_in(Long expiresIn) { this.expiresIn = expiresIn; }

    public String getToken_type() { return tokenType; }
    public void setToken_type(String tokenType) { this.tokenType = tokenType; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getErrorDescription() { return errorDescription; }
    public void setErrorDescription(String errorDescription) { this.errorDescription = errorDescription; }

    @Override
    public String toString() {
        return "ReponseOpenIdDTO{tokenType='" + tokenType + "', expiresIn=" + expiresIn + ", scope='" + scope + "', error='" + error + "'}";
    }
}
