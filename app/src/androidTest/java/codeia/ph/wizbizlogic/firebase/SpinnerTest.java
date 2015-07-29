package codeia.ph.wizbizlogic.firebase;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Pair;
import android.widget.Spinner;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.MainActivity;
import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.service.DataProtocol;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.service.TypedCursor;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
public class SpinnerTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity activity;

    private final DataProtocol<String, Integer> service = new DataService();
    private CountDownLatch settingUp;
    private static final List<String> CLEANUP = new ArrayList<>();
    private static final String PATTERN = "%s/%s";
    private String group1;
    private String group2;

    private final Result.Consume<Integer> fail = new Result.Consume<Integer>() {
        @Override
        public void apply(Integer value) {
            fail("failed with error: " + activity.getString(value, "?"));
        }
    };

    public SpinnerTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        settingUp = new CountDownLatch(1);
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
        Firebase.setAndroidContext(activity);

        final Group g = new Group();
        g.setName("group 1");
        service.putGroup(g).then(new Result.Chain<String, Pair<String, String>, Integer>() {
            @Override
            public Result<Pair<String, String>, Integer> apply(final String firstId) {
                g.setName("group 2");
                return service.putGroup(g).then(new Result.Convert<String, Pair<String, String>>() {
                    @Override
                    public Pair<String, String> apply(String secondId) {
                        return new Pair<>(firstId, secondId);
                    }
                });
            }
        }).then(new Result.Consume<Pair<String, String>>() {
            @Override
            public void apply(Pair<String, String> groups) {
                group1 = groups.first;
                group2 = groups.second;
                CLEANUP.add(String.format(PATTERN, DataService.GROUPS, group1));
                CLEANUP.add(String.format(PATTERN, DataService.GROUPS, group2));
                putCustomer("foo", group1);
                putCustomer("bar", group1);
                putCustomer("baz", group1);
                putCustomer("bat", group1);
                putProduct("BKB", group2);
                putProduct("AGHS", group2);
                putProduct("MEK", group1);
                putProduct("SKDI", group2);
                putAccount("a@b.c", group1);
                putAccount("z@x.y", group2);
                putAccount("l@m.n", group1);
                settingUp.countDown();
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (String path : CLEANUP) {
            new Firebase(path).removeValue();
        }
        CLEANUP.clear();
    }

    private void putCustomer(String name, String groupId) {
        Customer c = new Customer();
        c.setName(name);
        c.setGroupUid(groupId);
        service.putCustomer(c).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                CLEANUP.add(String.format(PATTERN, DataService.CUSTOMERS, value));
            }
        });
    }

    private void putProduct(String sku, String groupId) {
        Product p = new Product();
        p.setSku(sku);
        p.setGroupUid(groupId);
        service.putProduct(p).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                CLEANUP.add(String.format(PATTERN, DataService.PRODUCTS, value));
            }
        });
    }

    private void putAccount(String email, String groupId) {
        Account a = new Account();
        a.setEmail(email);
        a.setGroupUid(groupId);
        service.putAccount(a).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                CLEANUP.add(String.format(PATTERN, DataService.ACCOUNTS, value));
            }
        });
    }

    public void testSpinnerWithCustomers() {
        try {
            assertTrue("setup timeout", settingUp.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted during setup");
        }

        final Spinner s = (Spinner) activity.findViewById(R.id.the_spinner);
        final CountDownLatch latch = new CountDownLatch(1);

        service.getCustomersForGroup(group1).then(new Result.Consume<TypedCursor<Customer>>() {
            @Override
            public void apply(TypedCursor<Customer> value) {
                activity.showCustomers(value);
                s.setSelection(2, false);
                assertEquals("wrong number of items", 4, s.getCount());
                assertTrue("wrong type", s.getSelectedItem() instanceof Customer);
                assertEquals("baz", ((Customer) s.getSelectedItem()).getName());
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
            onView(withId(R.id.the_spinner)).check(matches(hasDescendant(withText("baz"))));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testSpinnerWithProducts() {
        try {
            assertTrue("setup timeout", settingUp.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted during setup");
        }

        final Spinner s = (Spinner) activity.findViewById(R.id.the_spinner);
        final CountDownLatch latch = new CountDownLatch(1);
        service.getProductsForGroup(group2).then(new Result.Consume<TypedCursor<Product>>() {
            @Override
            public void apply(TypedCursor<Product> value) {
                activity.showProducts(value);
                assertEquals("wrong count", 3, s.getCount());
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
            onView(withId(R.id.the_spinner)).check(matches(hasDescendant(withText("BKB"))));
            s.post(new Runnable() {
                @Override
                public void run() {
                    s.setSelection(1, false);
                }
            });
            onView(withId(R.id.the_spinner)).check(matches(hasDescendant(withText("AGHS"))));
            s.post(new Runnable() {
                @Override
                public void run() {
                    s.setSelection(2, false);
                }
            });
            onView(withId(R.id.the_spinner)).check(matches(hasDescendant(withText("SKDI"))));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testSpinnerWithAccounts() {
        try {
            assertTrue("setup timeout", settingUp.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted during setup");
        }

        int id = R.id.the_spinner;
        final Spinner s = (Spinner) activity.findViewById(id);
        final CountDownLatch latch = new CountDownLatch(1);
        service.getAccountsForGroup(group1).then(new Result.Consume<TypedCursor<Account>>() {
            @Override
            public void apply(TypedCursor<Account> value) {
                activity.showAccounts(value);
                assertEquals(2, s.getCount());
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
            onView(withId(id)).check(matches(hasDescendant(withText("a@b.c"))));
            s.post(new Runnable() {
                @Override
                public void run() {
                    s.setSelection(1, false);
                }
            });
            onView(withId(id)).check(matches(hasDescendant(withText("l@m.n"))));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
