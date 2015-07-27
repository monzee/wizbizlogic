package codeia.ph.wizbizlogic;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.yahoo.squidb.data.AbstractModel;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Product;
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

    private TypedAdapter<Customer> customerAdapter;
    private TypedAdapter<Product> productAdapter;
    private TypedAdapter<Account> accountAdapter;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        customerAdapter = new CustomerAdapter().buildAdapter(this, new Customer());
        productAdapter = new ProductAdapter().buildAdapter(this, new Product());
        accountAdapter = new AccountAdapter().buildAdapter(this, new Account());
        spinner = (Spinner) findViewById(R.id.the_spinner);
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
