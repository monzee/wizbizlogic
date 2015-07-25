package codeia.ph.wizbizlogic.sqlite;

import codeia.ph.wizbizlogic.service.Auth;
import codeia.ph.wizbizlogic.service.Result;

public class AuthService implements Auth<Long> {
    @Override
    public Result<String, Long> login(String email, String password) {
        return null;
    }

    @Override
    public Result<String, Long> register(String email, String password) {
        return null;
    }

    @Override
    public Result<Boolean, Long> changeEmail(String oldEmail, String newEmail, String password) {
        return null;
    }

    @Override
    public Result<Boolean, Long> changePassword(String email, String oldPassword, String newPassword) {
        return null;
    }
}
