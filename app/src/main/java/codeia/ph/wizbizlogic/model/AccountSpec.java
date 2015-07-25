package codeia.ph.wizbizlogic.model;

import com.yahoo.squidb.annotations.TableModelSpec;

@TableModelSpec(className = "Account", tableName = "accounts")
public class AccountSpec {
    public String email;
    public String password;
    public String uid;
}
