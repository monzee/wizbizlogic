package codeia.ph.wizbizlogic.service;

import codeia.ph.wizbizlogic.model.Account;
import codeia.ph.wizbizlogic.model.Concern;
import codeia.ph.wizbizlogic.model.Customer;
import codeia.ph.wizbizlogic.model.Feedback;
import codeia.ph.wizbizlogic.model.Group;
import codeia.ph.wizbizlogic.model.Product;
import codeia.ph.wizbizlogic.model.Promo;

public interface DataProtocol<Id, E> {

    Result<Group, E> getGroup(Id id);
    Result<Customer, E> getCustomer(Id id);
    Result<Product, E> getProduct(Id id);
    Result<Account, E> getAccount(Id id);

    Result<Id, E> putGroup(Group g);
    Result<Id, E> putCustomer(Customer c);
    Result<Id, E> putProduct(Product p);
    Result<Id, E> putAccount(Account a);

    Result<Id, E> putFeedback(Feedback f);
    Result<Id, E> putPromo(Promo p);
    Result<Id, E> putConcern(Concern c);

    Result<Many<Customer>, E> getCustomersForGroup(Id groupId);
    Result<Many<Product>, E> getProductsForGroup(Id groupId);
    Result<Many<Account>, E> getAccountsForGroup(Id groupId);

}
