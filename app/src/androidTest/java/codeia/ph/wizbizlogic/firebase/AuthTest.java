package codeia.ph.wizbizlogic.firebase;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.util.Pair;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.service.Result;

// don't run this test too often; it's very slow and messes up the uids
@LargeTest
public class AuthTest extends ApplicationTestCase<Application> {

    private final AuthService auth = new AuthService();
    private static final List<Pair<String, String>> CLEANUP = new ArrayList<>();
    private static final String PATH = "%s/%s";

    public AuthTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        Firebase.setAndroidContext(getContext());
        final CountDownLatch settingUp = new CountDownLatch(1);
        Firebase ref = new Firebase(DataService.APP_URL);
        ref.createUser("foo@example.com", "secret123", new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                CLEANUP.add(new Pair<>("foo@example.com", "secret123"));
                settingUp.countDown();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                fail(firebaseError.getMessage());
            }
        });
        try {
            assertTrue("timeout during setup", settingUp.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted during setup");
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        final CountDownLatch removing = new CountDownLatch(CLEANUP.size());
        Firebase ref = new Firebase(DataService.APP_URL);
        Firebase.ResultHandler noop = new Firebase.ResultHandler() {
            @Override
            public void onSuccess() {
                removing.countDown();
            }

            @Override
            public void onError(FirebaseError firebaseError) {
                removing.countDown();
            }
        };
        for (Pair<String, String> creds : CLEANUP) {
            ref.removeUser(creds.first, creds.second, noop);
        }
        removing.await();
        CLEANUP.clear();
    }

    public void testSuccessfulLogin() {
        final CountDownLatch p = new CountDownLatch(1);
        auth.login("foo@example.com", "secret123").then(new Result.Consume<String>() {
            @Override
            public void apply(String token) {
                assertNotNull(token);
                assertTrue(token.length() > 0);
                p.countDown();
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testLoginWrongPassword() {
        final CountDownLatch p = new CountDownLatch(1);
        auth.login("foo@example.com", "wrong password").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                fail("unexpected success");
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer error) {
                assertEquals(R.string.error_wrong_password, error.intValue());
                p.countDown();
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testLoginNonexistentEmail() {
        final CountDownLatch p = new CountDownLatch(1);
        auth.login("a@example.com", "asdflkj").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                fail("unexpected success");
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                Log.e("mz", "firebase error: " + DataService.lastError.getCode());
                assertEquals(R.string.error_unknown_user, value.intValue());
                p.countDown();
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterAccountThenLogin() {
        final CountDownLatch p = new CountDownLatch(1);
        auth.register("mz@codeia.ph", "password").then(new Result.Chain<String, String, Integer>() {
            @Override
            public Result<String, Integer> apply(String value) {
                Log.i("mz", "registered " + value);
                CLEANUP.add(new Pair<>("mz@codeia.ph", "password"));
                return auth.login("mz@codeia.ph", "password");
            }
        }).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                p.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getString(value, "?"));
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterExistingEmail() {
        final CountDownLatch p = new CountDownLatch(1);
        auth.register("foo@example.com", "asdf").then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                fail("unexpected success");
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                assertEquals(R.string.error_already_registered, value.intValue());
                p.countDown();
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testRegisterThenWrongPassword() {
        final CountDownLatch p = new CountDownLatch(1);
        final String email = "z@example.com";
        auth.register(email, "correct.").then(new Result.Chain<String, String, Integer>() {
            @Override
            public Result<String, Integer> apply(String value) {
                CLEANUP.add(new Pair<>(email, "correct."));
                return auth.login(email, "wrong!");
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                Log.e("mz", DataService.lastError.getMessage());
                assertEquals(R.string.error_wrong_password, value.intValue());
                p.countDown();
            }
        });
        try {
            assertTrue("timeout", p.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    private String getString(int resId, Object... fmtArgs) {
        return getContext().getString(resId, fmtArgs);
    }
}