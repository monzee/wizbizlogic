package codeia.ph.wizbizlogic.service;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Concern;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Feedback;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.model.Promo;

public interface Data<Id, E> {

    Result<Group, E> getGroup(Id id);
    Result<Customer, E> getCustomer(Id id);
    Result<Product, E> getProduct(Id id);
    Result<Account, E> getAccount(Id id);

    Result<Boolean, E> putGroup(Group g);
    Result<Boolean, E> putCustomer(Customer c);
    Result<Boolean, E> putProduct(Product p);
    Result<Boolean, E> putAccount(Account a);

    Result<Boolean, E> putFeedback(Feedback f);
    Result<Boolean, E> putPromo(Promo p);
    Result<Boolean, E> putConcern(Concern c);

    Result<Many<Customer>, E> getCustomersForGroup(Id groupId);
    Result<Many<Product>, E> getProductsForGroup(Id groupId);
    Result<Many<Account>, E> getAccountsForGroup(Id groupId);

}
