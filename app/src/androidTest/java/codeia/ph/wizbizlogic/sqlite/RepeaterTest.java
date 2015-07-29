package codeia.ph.wizbizlogic.sqlite;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Spinner;

import com.yahoo.squidb.data.DatabaseDao;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.MainActivity;
import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.service.DataProtocol;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.service.TypedCursor;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@MediumTest
public class RepeaterTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity activity;

    private DataProtocol<Long, Integer> service;

    private final Result.Consume<Integer> fail = new Result.Consume<Integer>() {
        @Override
        public void apply(Integer value) {
            fail("failed with error: " + activity.getString(value, "?"));
        }
    };

    public RepeaterTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        activity = getActivity();
        WizardDb db = new WizardDb(activity.getApplicationContext());
        db.recreate();

        service = new DataService(new DatabaseDao(db));
        putCustomer("foo", 1);
        putCustomer("bar", 1);
        putCustomer("baz", 1);
        putCustomer("bat", 1);
        putProduct("BKB", 2);
        putProduct("AGHS", 2);
        putProduct("MEK", 1);
        putProduct("SKDI", 2);
        putAccount("a@b.c", 1);
        putAccount("z@x.y", 2);
        putAccount("l@m.n", 1);
    }

    private void putCustomer(String name, long groupId) {
        Customer c = new Customer();
        c.setName(name);
        c.setGroupId(groupId);
        service.putCustomer(c);
    }

    private void putProduct(String sku, long groupId) {
        Product p = new Product();
        p.setSku(sku);
        p.setGroupId(groupId);
        service.putProduct(p);
    }

    private void putAccount(String email, long groupId) {
        Account a = new Account();
        a.setEmail(email);
        a.setGroupId(groupId);
        service.putAccount(a);
    }

    public void testSpinnerWithCustomers() {
        final Spinner s = (Spinner) activity.findViewById(R.id.the_spinner);
        final CountDownLatch latch = new CountDownLatch(1);

        service.getCustomersForGroup(1L).then(new Result.Consume<TypedCursor<Customer>>() {
            @Override
            public void apply(TypedCursor<Customer> value) {
                activity.showCustomers(value);
                s.setSelection(2);
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(3, TimeUnit.SECONDS));
            assertEquals("wrong number of items", 4, s.getCount());
            assertTrue("wrong type", s.getSelectedItem() instanceof Customer);
            assertEquals("baz", ((Customer) s.getSelectedItem()).getName());
            assertEquals(3L, ((Customer) s.getSelectedItem()).getId());
            onView(withId(R.id.the_spinner)).check(matches(hasDescendant(withText("baz"))));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testSpinnerWithProducts() {
        final Spinner s = (Spinner) activity.findViewById(R.id.the_spinner);
        final CountDownLatch latch = new CountDownLatch(1);
        service.getProductsForGroup(2L).then(new Result.Consume<TypedCursor<Product>>() {
            @Override
            public void apply(TypedCursor<Product> value) {
                activity.showProducts(value);
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(3, TimeUnit.SECONDS));
            assertEquals("wrong count", 3, s.getCount());
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
        int id = R.id.the_spinner;
        final Spinner s = (Spinner) activity.findViewById(id);
        final CountDownLatch latch = new CountDownLatch(1);
        service.getAccountsForGroup(1L).then(new Result.Consume<TypedCursor<Account>>() {
            @Override
            public void apply(TypedCursor<Account> value) {
                activity.showAccounts(value);
                latch.countDown();
            }
        }).orElse(fail);

        try {
            assertTrue("timeout", latch.await(3, TimeUnit.SECONDS));
            assertEquals(2, s.getCount());
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
