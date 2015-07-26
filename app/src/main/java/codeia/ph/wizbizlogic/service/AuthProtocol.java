package codeia.ph.wizbizlogic.service;

public interface AuthProtocol<E> {
    Result<String, E> login(String email, String password);
    Result<String, E> register(String email, String password);
    Result<Boolean, E> changeEmail(String oldEmail, String newEmail, String password);
    Result<Boolean, E> changePassword(String email, String oldPassword, String newPassword);
}
