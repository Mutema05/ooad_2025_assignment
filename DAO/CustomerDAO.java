package DAO;

import Core.Customer;
import java.util.List;

public interface CustomerDAO {
    void create(Customer customer);
    Customer read(int customerId);
    void update(Customer customer);
    void delete(int customerId);
    List<Customer> getAllCustomers();
}
