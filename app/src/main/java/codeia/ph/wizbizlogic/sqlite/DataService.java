package codeia.ph.wizbizlogic.sqlite;

import com.yahoo.squidb.data.DatabaseDao;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Concern;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Feedback;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.model.Promo;
import codeia.ph.wizbizlogic.service.DataProtocol;
import codeia.ph.wizbizlogic.service.Many;
import codeia.ph.wizbizlogic.service.Result;

public class DataService implements DataProtocol<Long, Integer> {

    private static final Result.Convert<Throwable, Integer>
    HANDLE_EXCEPTION = new Result.Convert<Throwable, Integer>() {
        @Override
        public Integer apply(Throwable value) {
            // log?
            return R.string.error_general;
        }
    };

    private final DatabaseDao db;

    public DataService(DatabaseDao dao) {
        db = dao;
    }

    @Override
    public Result<Group, Integer> getGroup(Long id) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Customer, Integer> getCustomer(Long id) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Product, Integer> getProduct(Long id) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Account, Integer> getAccount(Long id) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Long, Integer> putGroup(final Group g) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(g);
                return g.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putCustomer(final Customer c) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(c);
                return c.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putProduct(final Product p) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(p);
                return p.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putAccount(final Account a) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(a);
                return a.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putFeedback(final Feedback f) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(f);
                return f.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putPromo(final Promo p) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(p);
                return p.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Long, Integer> putConcern(final Concern c) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(c);
                return c.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    @Override
    public Result<Many<Customer>, Integer> getCustomersForGroup(Long groupId) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Many<Product>, Integer> getProductsForGroup(Long groupId) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<Many<Account>, Integer> getAccountsForGroup(Long groupId) {
        return Result.error(R.string.error_unimplemented);
    }
}
