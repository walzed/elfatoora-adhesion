package tn.tn.elfatoora.dto;

public class ResponseMobileIdOpenDTO {
    private String state;
    private String message; // on y met l'URL authorize

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "ResponseMobileIdOpenDTO{state='" + state + "', message='" + message + "'}";
    }
}
