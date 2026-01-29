package tn.tn.elfatoora.service;

import tn.tn.elfatoora.dto.OidcUserInfoDTO;
import tn.tn.elfatoora.dto.ReponseOpenIdDTO;
import tn.tn.elfatoora.dto.ResponseMobileIdOpenDTO;

public interface SignInService {

    ResponseMobileIdOpenDTO signInAuthorise(String baseUrl);

    ReponseOpenIdDTO postDataToExternalApi(String baseUrl, String code, String state) throws Exception;

    OidcUserInfoDTO userInfo(String accessToken) throws Exception;

    String buildLogoutUrl(String baseUrl, String state);
}
