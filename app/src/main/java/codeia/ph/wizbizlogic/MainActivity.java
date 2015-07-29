package codeia.ph.wizbizlogic;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.yahoo.squidb.data.AbstractModel;

import codeia.ph.wizbizlogic.firebase.DataService;
import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.service.Result;
import codeia.ph.wizbizlogic.service.TypedAdapter;
import codeia.ph.wizbizlogic.service.TypedCursor;
import codeia.ph.wizbizlogic.sqlite.AdapterFactory;


public class MainActivity extends ActionBarActivity {

    private static abstract class SpinnerAdapter<T extends AbstractModel> extends AdapterFactory<T> {
        @Override
        protected int getLayout(ItemType type) {
            switch (type) {
                case ITEM:
                    return android.R.layout.simple_spinner_item;
                case DROPDOWN:
                default:
                    return android.R.layout.simple_spinner_dropdown_item;
            }
        }
    }

    private static class CustomerAdapter extends SpinnerAdapter<Customer> {
        @Override
        protected void prepare(View view, Customer item) {
            ((TextView) view).setText(item.getName());
        }
    }

    private static class ProductAdapter extends SpinnerAdapter<Product> {
        @Override
        protected void prepare(View view, Product item) {
            ((TextView) view).setText(item.getSku());
        }
    }

    private static class AccountAdapter extends SpinnerAdapter<Account> {
        @Override
        protected void prepare(View view, Account item) {
            ((TextView) view).setText(item.getEmail());
        }
    }

    private static class ListAdapter extends AdapterFactory<Customer> {
        @Override
        protected int getLayout(ItemType type) {
            return android.R.layout.simple_list_item_1;
        }

        @Override
        protected void prepare(View view, Customer item) {
            ((TextView) view).setText(item.getName());
        }
    };

    private TypedAdapter<Customer> customerAdapter;
    private TypedAdapter<Product> productAdapter;
    private TypedAdapter<Account> accountAdapter;
    private Spinner spinner;
    private ListView list;
    private TypedAdapter<Customer> listAdapter;

    private String groupId;

    private final View.OnClickListener testFirebase = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final DataService fb = new DataService();
            Result<TypedCursor<Customer>, ?> didGet;
            if (groupId == null) {
                Firebase.setAndroidContext(getApplicationContext());
                final Group g = new Group();
                g.setName("group A");
                didGet = fb.putGroup(g).then(new Result.Chain<String, Pair<String, String>, Integer>() {
                    @Override
                    public Result<Pair<String, String>, Integer> apply(final String firstGroup) {
                        g.setName("group B");
                        return fb.putGroup(g).then(new Result.Convert<String, Pair<String, String>>() {
                            @Override
                            public Pair<String, String> apply(String secondGroup) {
                                return new Pair<>(firstGroup, secondGroup);
                            }
                        });
                    }
                }).then(new Result.Consume<Pair<String, String>>() {
                    @Override
                    public void apply(Pair<String, String> ids) {
                        String grpA = ids.first;
                        String grpB = ids.second;
                        Customer c = new Customer();
                        c.setName("foo");
                        c.setGroupUid(grpA);
                        fb.putCustomer(c);
                        c.setName("bar");
                        fb.putCustomer(c);
                        c.setName("nope.");
                        c.setGroupUid(grpB);
                        fb.putCustomer(c);
                        c.setName("baz");
                        c.setGroupUid(grpA);
                        fb.putCustomer(c);
                        c.setName("bat");
                        fb.putCustomer(c);
                    }
                }).then(new Result.Chain<Pair<String, String>, TypedCursor<Customer>, Integer>() {
                    @Override
                    public Result<TypedCursor<Customer>, Integer> apply(Pair<String, String> groups) {
                        groupId = groups.second;
                        return fb.getCustomersForGroup(groups.first);
                    }
                });
            } else {
                didGet = fb.getCustomersForGroup(groupId);
                final Customer c = new Customer();
                c.setGroupUid(groupId);
                c.setName("nope.");
                new CountDownTimer(5000, 1000) {
                    @Override
                    public void onTick(long l) {
                        fb.putCustomer(c);
                    }

                    @Override
                    public void onFinish() {
                        c.setName("NOPE");
                        fb.putCustomer(c).then(new Runnable() {
                            @Override
                            public void run() {
                                fb.getCustomersForGroup(groupId).then(new Result.Consume<TypedCursor<Customer>>() {
                                    @Override
                                    public void apply(TypedCursor<Customer> value) {
                                        listAdapter.populateWith(value);
                                    }
                                });
                            }
                        });
                    }
                }.start();
            }
            didGet.then(new Result.Consume<TypedCursor<Customer>>() {
                @Override
                public void apply(TypedCursor<Customer> value) {
                    listAdapter.populateWith(value);
                    showCustomers(value);
                }
            });
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customerAdapter = new CustomerAdapter().buildAdapter(this, new Customer());
        productAdapter = new ProductAdapter().buildAdapter(this, new Product());
        accountAdapter = new AccountAdapter().buildAdapter(this, new Account());
        spinner = (Spinner) findViewById(R.id.the_spinner);
        list = (ListView) findViewById(R.id.the_list);
        listAdapter = new ListAdapter().buildAdapter(this, new Customer());
        list.setAdapter(listAdapter);
        Button btn = (Button) findViewById(R.id.the_button);
        btn.setOnClickListener(testFirebase);
    }

    public void showCustomers(TypedCursor<Customer> customers) {
        customerAdapter.populateWith(customers);
        spinner.setAdapter(customerAdapter);
    }

    public void showProducts(TypedCursor<Product> products) {
        productAdapter.populateWith(products);
        spinner.setAdapter(productAdapter);
    }

    public void showAccounts(TypedCursor<Account> accounts) {
        accountAdapter.populateWith(accounts);
        spinner.setAdapter(accountAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == R.id.action_settings;
    }
}
