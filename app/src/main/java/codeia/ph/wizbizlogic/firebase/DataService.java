package codeia.ph.wizbizlogic.firebase;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.yahoo.squidb.data.AbstractModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public FirebaseError lastError;

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
        return put(f, PROMOS);
    }

    @Override
    public Result<String, Integer> putConcern(Concern c) {
        return put(c, CONCERNS);
    }

    @Override
    public Result<TypedCursor<Customer>, Integer> getCustomersForGroup(String groupId) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<TypedCursor<Product>, Integer> getProductsForGroup(String groupId) {
        return Result.error(R.string.error_unimplemented);
    }

    @Override
    public Result<TypedCursor<Account>, Integer> getAccountsForGroup(String groupId) {
        return Result.error(R.string.error_unimplemented);
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

    private static final Map<Long, String> UID_MAP = new HashMap<>();
    private static final AtomicLong COUNTER = new AtomicLong(1);
    private static final long TIMEOUT = 10000L;

    private class TryGetOne<T extends TableModel> extends CountDownTimer implements ValueEventListener {
        public final Result<T, Integer> result = new Result<>();
        private final Class<T> modelClass;
        private Firebase ref;

        public TryGet(Class<T> modelClass) {
            super(TIMEOUT, TIMEOUT);
            this.modelClass = modelClass;
        }

        @Override
        public void onTick(long remaining) {
            // no-op
        }

        @OVerride
        public void onFinish() {
            if (ref != null) {
                ref.removeEventListener(this);
                result.fail(R.string.error_not_found);
            }
        }

        @Override
        public void onDataChange(DataSnapshot snapshot) {
            long id = COUNTER.getAndIncrement();
            T model = snapshot.getValue(modelClass);
            model.setId(id);
            UID_MAP.put(id, snapshot.getKey());
            unlisten();
            result.ok(model);
        }

        @Override
        public void onCancelled(FirebaseError error) {
            lastError = error;
            unlisten();
            result.fail(R.string.error_cancelled);
        }

        public void start(Firebase ref) {
            this.ref = ref;
            ref.addValueEventListener(this);
            start();
        }

        private unlisten() {
            cancel();
            ref.removeEventListener(this);
            ref = null;
        }
    }

    private <T extends TableModel> Result<T, Integer> get(String id, Class<T> modelClass, String path) {
        if (id == null) {
            return Result.error(R.string.error_not_found);
        }
        Firebase ref = new Firebase(path);
        return new TryGetOne<>(modelClass).start(ref.child(id)).result;
    }

    private <T extends AbstractModel> Result<String, Integer> put(T model, String path) {
        Firebase ref = new Firebase(path);
        Map<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> pair : model.getMergedValues().valueSet()) {
            data.put(pair.getKey(), pair.getValue());
        }

        final Result<String, Integer> result = new Result<>();
        Firebase.CompletionListener then = new Firebase.CompletionListener {
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
            long id = data.getLong("_id");
            if (UID_MAP.containsKey(id)) {
                data.remove("_id");
                ref.child(UID_MAP.get(id)).setValue(data, then);
                return result;
            }
        }
        ref.push().setValue(data, then);
        return result;
    }
}
