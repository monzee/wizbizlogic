package codeia.ph.wizbizlogic.firebase;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.yahoo.squidb.data.AbstractModel;
import com.yahoo.squidb.data.TableModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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

public class DataService implements DataProtocol<String, Integer> {
    private static final String APP_URL = "https://vivid-torch-2758.firebaseio.com";

    public static final String CUSTOMERS = APP_URL + "/customers";
    public static final String PRODUCTS = APP_URL + "/products";
    public static final String GROUPS = APP_URL + "/groups";
    public static final String ACCOUNTS = APP_URL + "/accounts";
    public static final String FEEDBACK = APP_URL + "/feedback";
    public static final String PROMOS = APP_URL + "/promos";
    public static final String CONCERNS = APP_URL + "/concerns";

    private static final Map<String, Long> UID_MAP = new HashMap<>();
    private static final AtomicLong COUNTER = new AtomicLong(1);

    private static FirebaseError lastError;

    public static long getId(String uid) {
        if (UID_MAP.containsKey(uid)) {
            return UID_MAP.get(uid);
        }
        long id = COUNTER.getAndIncrement();
        UID_MAP.put(uid, id);
        return id;
    }


    @Override
    public Result<Customer, Integer> getCustomer(String id) {
        return get(id, Customer.class, CUSTOMERS);
    }

    @Override
    public Result<Product, Integer> getProduct(String id) {
        return get(id, Product.class, PRODUCTS);
    }

    @Override
    public Result<Group, Integer> getGroup(String id) {
        return get(id, Group.class, GROUPS);
    }

    @Override
    public Result<Account, Integer> getAccount(String id) {
        return get(id, Account.class, ACCOUNTS);
    }

    @Override
    public Result<String, Integer> putCustomer(Customer c) {
        return put(c, CUSTOMERS);
    }

    @Override
    public Result<String, Integer> putFeedback(Feedback f) {
        return put(f, FEEDBACK);
    }

    @Override
    public Result<String, Integer> putPromo(Promo p) {
        return put(p, PROMOS);
    }

    @Override
    public Result<String, Integer> putConcern(Concern c) {
        return put(c, CONCERNS);
    }

    @Override
    public Result<TypedCursor<Customer>, Integer> getCustomersForGroup(String groupId) {
        TryGetMany<Customer> query = new TryGetMany<>(Customer.ID, Customer.NAME);
        return query.start(new Firebase(CUSTOMERS).orderByChild("groupUid").equalTo(groupId));
    }

    @Override
    public Result<TypedCursor<Product>, Integer> getProductsForGroup(String groupId) {
        TryGetMany<Product> query = new TryGetMany<>(Product.ID, Product.SKU);
        return query.start(new Firebase(PRODUCTS).orderByChild("groupUid").equalTo(groupId));
    }

    @Override
    public Result<TypedCursor<Account>, Integer> getAccountsForGroup(String groupId) {
        TryGetMany<Account> query = new TryGetMany<>(Account.ID, Account.EMAIL);
        return query.start(new Firebase(ACCOUNTS).orderByChild("groupUid").equalTo(groupId));
    }

    @Override
    public Result<String, Integer> putProduct(Product p) {
        return put(p, PRODUCTS);
    }

    @Override
    public Result<String, Integer> putGroup(Group g) {
        return put(g, GROUPS);
    }

    @Override
    public Result<String, Integer> putAccount(Account a) {
        return put(a, ACCOUNTS);
    }

    public FirebaseError getLastError() {
        return lastError;
    }

    private <T extends TableModel> Result<T, Integer> get(String id, Class<T> modelClass, String path) {
        if (id == null) {
            return Result.error(R.string.error_not_found);
        }
        Firebase ref = new Firebase(path);
        return new TryGetOne<>(modelClass).start(ref.child(id));
    }

    private <T extends AbstractModel> Result<String, Integer> put(T model, String path) {
        Firebase ref = new Firebase(path);
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> pair : model.getMergedValues().valueSet()) {
            data.put(pair.getKey(), pair.getValue());
        }

        final Result<String, Integer> result = new Result<>();
        Firebase.CompletionListener then = new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError error, Firebase ref) {
                if (error == null) {
                    result.ok(ref.getKey());
                } else {
                    lastError = error;
                    result.fail(R.string.error_general);
                }
            }
        };

        if (data.containsKey("_id")) {
            long id = (long) data.get("_id");
            data.remove("_id");
            // TODO: or keep a SparseArray of strings, just make sure it's always in sync with UID_MAP
            // then it will be just O(1) search in exchange for more memory.
            for (Map.Entry<String, Long> entry : UID_MAP.entrySet()) {
                if (id == entry.getValue()) {
                    ref.child(entry.getKey()).setValue(data, then);
                    return result;
                }
            }
        }
        ref.push().setValue(data, then);
        return result;
    }
}
