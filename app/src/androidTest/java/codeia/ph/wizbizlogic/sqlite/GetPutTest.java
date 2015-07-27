package codeia.ph.wizbizlogic.sqlite;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.yahoo.squidb.data.DatabaseDao;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.service.Result;

public class GetPutTest extends ApplicationTestCase<Application> {
    private DataService service;

    public GetPutTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WizardDb db = new WizardDb(getContext());
        db.recreate();
        service = new DataService(new DatabaseDao(db));
    }

    public void testPutAccount() {
        final CountDownLatch latch = new CountDownLatch(1);
        Account a = new Account();
        a.setEmail("foo@abc.def");
        a.setPassword("password");
        a.setGroupId(1L);
        service.putAccount(a).then(new Result.Consume<Long>() {
            @Override
            public void apply(Long value) {
                assertEquals(1L, value.longValue());
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
                latch.countDown();
            }
        });

        try {
            assertTrue("timeout", latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testPutCustomer() {
        final CountDownLatch latch = new CountDownLatch(1);
        Customer c = new Customer();
        c.setName("foobar");
        c.setGroupId(1L);
        service.putCustomer(c).then(new Result.Consume<Long>() {
            @Override
            public void apply(Long value) {
                assertEquals(1L, value.longValue());
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
                latch.countDown();
            }
        });

        try {
            assertTrue("timeout", latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }

    public void testPutThenGet() {
        final CountDownLatch latch = new CountDownLatch(1);
        Customer c = new Customer();
        c.setName("foobar");
        c.setGroupId(1L);
        service.putCustomer(c).then(new Result.Chain<Long, Customer, Integer>() {
            @Override
            public Result<Customer, Integer> apply(Long value) {
                return service.getCustomer(value);
            }
        }).then(new Result.Consume<Customer>() {
            @Override
            public void apply(Customer value) {
                assertEquals("foobar", value.getName());
                assertEquals(1L, value.getGroupId().longValue());
                latch.countDown();
            }
        }).orElse(new Result.Consume<Integer>() {
            @Override
            public void apply(Integer value) {
                fail(getContext().getString(value, "?"));
                latch.countDown();
            }
        });

        try {
            assertTrue("timeout", latch.await(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("interrupted");
        }
    }
}
