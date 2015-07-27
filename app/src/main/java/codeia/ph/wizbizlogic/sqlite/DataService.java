package codeia.ph.wizbizlogic.sqlite;

import android.database.Cursor;
import android.util.Log;

import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.DatabaseDao;
import com.yahoo.squidb.data.SquidCursor;
import com.yahoo.squidb.data.TableModel;
import com.yahoo.squidb.sql.Field;
import com.yahoo.squidb.sql.Property;
import com.yahoo.squidb.sql.Query;

import java.util.List;

import codeia.ph.wizbizlogic.R;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Concern;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Feedback;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.model.Promo;
import codeia.ph.wizbizlogic.service.DataProtocol;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.service.TypedCursor;

public class DataService implements DataProtocol<Long, Integer> {

    private static final Result.Convert<Throwable, Integer>
    HANDLE_EXCEPTION = new Result.Convert<Throwable, Integer>() {
        @Override
        public Integer apply(Throwable value) {
            Log.e("mz", "exception: ", value);
            return R.string.error_general;
        }
    };

    private final DatabaseDao db;

    public DataService(DatabaseDao dao) {
        db = dao;
    }

    @Override
    public Result<Group, Integer> getGroup(Long id) {
        return getModel(id, Group.class, Group.PROPERTIES);
    }

    @Override
    public Result<Customer, Integer> getCustomer(Long id) {
        return getModel(id, Customer.class, Customer.PROPERTIES);
    }

    @Override
    public Result<Product, Integer> getProduct(Long id) {
        return getModel(id, Product.class, Product.PROPERTIES);
    }

    @Override
    public Result<Account, Integer> getAccount(Long id) {
        return getModel(id, Account.class, Account.PROPERTIES);
    }

    @Override
    public Result<Long, Integer> putGroup(Group g) {
        return putModel(g);
    }

    @Override
    public Result<Long, Integer> putCustomer(Customer c) {
        return putModel(c);
    }

    @Override
    public Result<Long, Integer> putProduct(Product p) {
        return putModel(p);
    }

    @Override
    public Result<Long, Integer> putAccount(Account a) {
        return putModel(a);
    }

    @Override
    public Result<Long, Integer> putFeedback(Feedback f) {
        return putModel(f);
    }

    @Override
    public Result<Long, Integer> putPromo(Promo p) {
        return putModel(p);
    }

    @Override
    public Result<Long, Integer> putConcern(Concern c) {
        return putModel(c);
    }

    @Override
    public Result<TypedCursor<Customer>, Integer> getCustomersForGroup(final Long groupId) {
        return getRowsWithGroup(groupId, Customer.class, Customer.GROUP_ID,
                Customer.ID, Customer.NAME);
    }

    @Override
    public Result<TypedCursor<Product>, Integer> getProductsForGroup(Long groupId) {
        return getRowsWithGroup(groupId, Product.class, Product.GROUP_ID,
                Product.ID, Product.SKU);
    }

    @Override
    public Result<TypedCursor<Account>, Integer> getAccountsForGroup(Long groupId) {
        return getRowsWithGroup(groupId, Account.class, Account.GROUP_ID,
                Account.ID, Account.EMAIL);
    }

    private <T extends TableModel> Result<T, Integer>
    getModel(final Long id, final Class<T> tableClass, final Property<?>... fields) {
        return Result.background(new Result.Produce<T>() {
            @Override
            public T apply() {
                return db.fetch(tableClass, id, fields);
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    private static class Wrap<T extends AbstractModel> implements TypedCursor<T> {
        private final Cursor cursor;
        private final Field<?>[] fields;

        public Wrap(SquidCursor<T> cursor) {
            this.cursor = cursor;
            List<? extends Field<?>> fields = cursor.getFields();
            this.fields = fields.toArray(new Field<?>[fields.size()]);
        }

        @Override
        public Cursor getCursor() {
            return cursor;
        }

        @Override
        public Field<?>[] getFields() {
            return fields;
        }
    }

    private <T extends TableModel> Result<Long, Integer> putModel(final T model) {
        return Result.background(new Result.Produce<Long>() {
            @Override
            public Long apply() {
                db.persist(model);
                return model.getId();
            }
        }).orElse(HANDLE_EXCEPTION);
    }

    private <T extends AbstractModel> Result<TypedCursor<T>, Integer>
    getRowsWithGroup(final Long id, final Class<T> modelClass,
                     final Property<Long> groupColumn, final Property<?>... fields) {
        return Result.background(new Result.Produce<TypedCursor<T>>() {
            @Override
            public TypedCursor<T> apply() {
                SquidCursor<T> cursor = db.query(modelClass,
                        Query.select(fields).where(groupColumn.eq(id)));
                return new Wrap<>(cursor);
            }
        }).orElse(HANDLE_EXCEPTION);
    }
}
