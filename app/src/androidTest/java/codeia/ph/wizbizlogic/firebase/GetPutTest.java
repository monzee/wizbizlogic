package codeia.ph.wizbizlogic.firebase;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import com.firebase.client.Firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.service.Result;

@LargeTest
public class GetPutTest extends ApplicationTestCase<Application> {

    private DataService service;
    private static final List<String> CLEANUP = new ArrayList<>();

    public GetPutTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Firebase.setAndroidContext(getContext());
        service = new DataService();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        for (String path : CLEANUP) {
            Firebase ref = new Firebase(path);
            ref.removeValue();
        }
        CLEANUP.clear();
    }

    public void testPutAccount() {
        final CountDownLatch latch = new CountDownLatch(1);
        Account a = new Account();
        a.setEmail("foo@abc.def");
        a.setPassword("password");
        a.setGroupId(1L);
        service.putAccount(a).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                assertNotNull(value);
                CLEANUP.add(String.format("%s/%s", DataService.ACCOUNTS, value));
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
            }
        });

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testPutCustomer() {
        final CountDownLatch latch = new CountDownLatch(1);
        Customer c = new Customer();
        c.setName("foobar");
        c.setGroupId(1L);
        service.putCustomer(c).then(new Result.Consume<String>() {
            @Override
            public void apply(String value) {
                assertNotNull(value);
                CLEANUP.add(String.format("%s/%s", DataService.CUSTOMERS, value));
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
            }
        });

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testPutThenGet() {
        final CountDownLatch latch = new CountDownLatch(1);
        Customer c = new Customer();
        c.setName("foobar");
        c.setGroupId(1L);
        service.putCustomer(c).then(new Result.Chain<String, Customer, Integer>() {
            @Override
            public Result<Customer, Integer> apply(String value) {
                CLEANUP.add(String.format("%s/%s", DataService.CUSTOMERS, value));
                return service.getCustomer(value);
            }
        }).then(new Result.Consume<Customer>() {
            @Override
            public void apply(Customer value) {
                assertEquals("foobar", value.getName());
                assertEquals(1L, value.getGroupId().longValue());
                Log.i("mz", "got: " + value);
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
            }
        });

        try {
            assertTrue("timeout", latch.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }
}
