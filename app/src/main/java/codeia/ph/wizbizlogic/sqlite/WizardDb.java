package codeia.ph.wizbizlogic.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.yahoo.squidb.data.AbstractDatabase;
import com.yahoo.squidb.sql.Table;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Concern;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Feedback;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.model.Promo;

public class WizardDb extends AbstractDatabase {

    public WizardDb(Context context) {
        super(context);
    }

    @Override
    protected String getName() {
        return "wizdata";
    }

    @Override
    protected int getVersion() {
        return 1;
    }

    @Override
    protected Table[] getTables() {
        return new Table[] {
                Account.TABLE,
                Concern.TABLE,
                Customer.TABLE,
                Feedback.TABLE,
                Group.TABLE,
                Product.TABLE,
                Promo.TABLE,
        };
    }

    @Override
    protected boolean onUpgrade(SQLiteDatabase sqLiteDatabase, int fromVersion, int toVersion) {
        return true;
    }
}
