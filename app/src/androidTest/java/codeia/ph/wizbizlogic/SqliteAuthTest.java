package codeia.ph.wizbizlogic;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.yahoo.squidb.data.AbstractDatabase;
import com.yahoo.squidb.data.DatabaseDao;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.sqlite.AuthService;
import codeia.ph.wizbizlogic.sqlite.DataService;
import codeia.ph.wizbizlogic.sqlite.WizardDb;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class SqliteAuthTest extends ApplicationTestCase<Application> {
    private AuthService auth;

    public SqliteAuthTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        AbstractDatabase db = new WizardDb(getContext());
        DatabaseDao dao = new DatabaseDao(db);
        db.recreate();
        auth = new AuthService(dao);
        DataService service = new DataService(dao);
        Account a = new Account();
        a.setEmail("foo@example.com");
        a.setPassword("secret123");
        service.putAccount(a);
    }

    private static class Probe<T> extends CountDownLatch {
        public T state;

        public Probe() {
            super(1);
        }

        public void signal(T value) {
            state = value;
            countDown();
        }
    }

    public void testSuccessfulLogin() {
        final Probe<Boolean> p = new Probe<>();
        auth.login("foo@example.com", "secret123").then(new Result.Consume<String>() {
            @Override
            public void apply(String token) {
                Log.i("mz", token);
                p.signal(true);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertTrue(p.state);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testLoginWrongPassword() {
        final Probe<Boolean> p = new Probe<>();
        auth.login("foo@example.com", "wrong password").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                p.signal(false);
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer error) {
                Log.i("mz", getString(error, "?"));
                p.signal(error == R.string.error_wrong_password);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertTrue(p.state);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testLoginNonexistentEmail() {
        final Probe<Boolean> p = new Probe<>();
        auth.login("a@b.c", "asdflkj").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                p.signal(false);
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                Log.i("mz", getString(value, "?"));
                p.signal(value == R.string.error_unknown_user);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertTrue(p.state);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterAccountThenLogin() {
        final Probe<Boolean> p = new Probe<>();
        auth.register("mz@codeia.ph", "password").then(new Result.Chain<String, String, Integer>() {
            @Override
            public Result<String, Integer> apply(String value) {
                Log.i("mz", "registered " + value);
                return auth.login("mz@codeia.ph", "password");
            }
        }).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                Log.i("mz", "logged in " + value);
                p.signal(true);
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                Log.i("mz", getString(value, "?"));
                p.signal(false);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertTrue(p.state);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterExistingEmail() {
        final Probe<Boolean> p = new Probe<>();
        auth.register("foo@example.com", "asdf").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                p.signal(false);
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                Log.i("mz", getString(value, "?"));
                p.signal(value == R.string.error_already_registered);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertTrue(p.state);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterThenWrongPassword() {
        final Probe<Integer> p = new Probe<>();
        final String email = "z@x.c";
        auth.register(email, "correct.").then(new Result.Chain<String, String, Integer>() {
            @Override
            public Result<String, Integer> apply(String value) {
                return auth.login(email, "wrong!");
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                p.signal(value);
            }
        });
        try {
            assertTrue("timeout", p.await(1, TimeUnit.SECONDS));
            assertEquals(getString(p.state, "?"), p.state.intValue(), R.string.error_wrong_password);
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    private String getString(int resId, Object... fmtArgs) {
        return getContext().getString(resId, fmtArgs);
    }
}