package tetish_inn_backend.tetish_inn.modules.auth;

public record AuthRequest(String email, String password) {
    public String getEmail() {
        return email;
    }
    public String getPassword() {
        return password;
    }
}
