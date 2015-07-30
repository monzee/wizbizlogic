package codeia.ph.wizbizlogic.firebase;

import android.test.suitebuilder.annotation.LargeTest;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.service.AuthProtocol;
import codeia.ph.wizbizlogic.service.Result;

@LargeTest
public class AuthService implements AuthProtocol<Integer> {
    @Override
    public Result<String, Integer> login(String email, String password) {
        final Result<String, Integer> result = new Result<>();
        Firebase ref = new Firebase(DataService.APP_URL);
        ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData auth) {
                result.ok(auth.getUid());
            }

            @Override
            public void onAuthenticationError(FirebaseError error) {
                DataService.lastError = error;
                switch (error.getCode()) {
                    case FirebaseError.INVALID_PASSWORD:
                        result.fail(R.string.error_wrong_password);
                        break;

                    case FirebaseError.USER_DOES_NOT_EXIST:
                        result.fail(R.string.error_unknown_user);
                        break;

                    default:
                        result.fail(R.string.error_general);
                        break;
                }
            }
        });
        return result;
    }

    @Override
    public Result<String, Integer> register(String email, String password) {
        final Result<String, Integer> result = new Result<>();
        Firebase ref = new Firebase(DataService.APP_URL);
        ref.createUser(email, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
            @Override
            public void onSuccess(Map<String, Object> account) {
                result.ok(account.get("uid").toString());
            }

            @Override
            public void onError(FirebaseError error) {
                DataService.lastError = error;
                switch (error.getCode()) {
                    case FirebaseError.EMAIL_TAKEN:
                        result.fail(R.string.error_already_registered);
                        break;

                    default:
                        result.fail(R.string.error_general);
                        break;
                }
            }
        });
        return result;
    }

    @Override
    public Result<Boolean, Integer> changeEmail(String oldEmail, String newEmail, String password) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Boolean, Integer> changePassword(String email, String oldPassword, String newPassword) {
        return Result.error(R.string.error_unimplemented);
    }
}
