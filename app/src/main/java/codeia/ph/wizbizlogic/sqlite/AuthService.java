package codeia.ph.wizbizlogic.sqlite;

import com.yahoo.squidb.data.DatabaseDao;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.service.Auth;
import codeia.ph.wizbizlogic.service.Result;

public class AuthService implements Auth<Integer> {
    private final DatabaseDao db;

    public AuthService(DatabaseDao dao) {
        db = dao;
    }

    @Override
    public Result<String, Integer> login(final String email, final String password) {
        return Result.background(new Result.Produce<Result<String, Integer>>() {
            @Override
            public Result<String, Integer> apply() {
                Account a = db.fetchByCriterion(Account.class, Account.EMAIL.eq(email),
                        Account.PASSWORD);
                if (a == null) {
                    return Result.error(R.string.error_unknown_user);
                } else if (!a.getPassword().equals(password)) {
                    return Result.error(R.string.error_wrong_password);
                } else {
                    return Result.success("<generate auth token here>");
                }
            }
        }, new Result.Convert<Throwable, Integer>() {
            @Override
            public Integer apply(Throwable value) {
                return R.string.error_general;
            }
        });
    }

    @Override
    public Result<String, Integer> register(final String email, final String password) {
        final Account a = new Account();
        a.setEmail(email);
        a.setPassword(password);
        return Result.background(new Result.Produce<Result<String, Integer>>() {
             @Override
             public Result<String, Integer> apply() {
                 Account check = db.fetchByCriterion(Account.class, Account.EMAIL.eq(email));
                 if (check != null) {
                     return Result.error(R.string.error_already_registered);
                 }
                 if (db.persist(a)) {
                     return Result.success(Long.toString(a.getId()));
                 } else {
                     return Result.error(R.string.error_general);
                 }
             }
         }, new Result.Convert<Throwable, Integer>() {
             @Override
             public Integer apply(Throwable value) {
                 return R.string.error_general;
             }
         });
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
