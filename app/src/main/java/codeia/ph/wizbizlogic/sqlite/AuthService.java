package codeia.ph.wizbizlogic.sqlite;

import com.yahoo.squidb.data.DatabaseDao;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.service.AuthProtocol;
import codeia.ph.wizbizlogic.service.Result;

public class AuthService implements AuthProtocol<Integer> {
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
                            Account.ID, Account.PASSWORD);
                if (a == null) {
                    return Result.error(R.string.error_unknown_user);
                } else if (!a.getPassword().equals(password)) {
                    return Result.error(R.string.error_wrong_password);
                } else {
                    return Result.success(String.format("via$password:%d", a.getId()));
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
                 db.beginTransaction();
                 try {
                     if (!db.persist(a)) {
                         return Result.error(R.string.error_general);
                     } else {
                         String uid = String.format("account:%d", a.getId());
                         // when is double saving ever a good idea?
                         // i wonder if i really have to do this here. or at all.
                         a.setUid(uid);
                         db.persist(a);
                         db.setTransactionSuccessful();
                         return Result.success(uid);
                     }
                 } finally {
                     db.endTransaction();
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
