package DAO;

import Core.Customer;
import Core.Account;
import Core.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAOImpl implements CustomerDAO {
    private Connection connection;
    private AccountDAO accountDAO;
    private TransactionDAO transactionDAO;

    public CustomerDAOImpl(Connection connection) {
        this.connection = connection;
        this.accountDAO = new AccountDAOImpl(connection);
        this.transactionDAO = new TransactionDAOImpl(connection);
    }

    @Override
    public void create(Customer customer) {
        String sql = "INSERT INTO Customer (first_name, surname, address, phone_number, password) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getSurname());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getPassword());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                customer.setCustomerId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Customer read(int customerId) {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(
                        rs.getString("first_name"),
                        rs.getString("surname"),
                        rs.getString("address"),
                        rs.getString("password"),
                        rs.getString("phone_number")
                );
                c.setCustomerId(rs.getInt("customer_id"));

                // ✅ Fetch related accounts and transactions
                List<Account> accounts = accountDAO.getAccountsByCustomerId(c.getCustomerId());
                c.setAccounts(accounts);

                // Load transactions for each account
                for (Account acc : accounts) {
                    List<Transaction> txs = transactionDAO.getAllTransactions(); // filter or create specific method
                    for (Transaction t : txs) {
                        if (t.getAccountId() == acc.getAccountId()) {
                            c.addTransaction(t);
                        }
                    }
                }
                return c;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customer"; // your table name (not 'customers')

        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getString("first_name"),
                        rs.getString("surname"),
                        rs.getString("address"),
                        rs.getString("password"),
                        rs.getString("phone_number")
                );
                c.setCustomerId(rs.getInt("customer_id"));

                // ✅ Load accounts for each customer
                List<Account> accounts = accountDAO.getAccountsByCustomerId(c.getCustomerId());
                c.setAccounts(accounts);

                customers.add(c);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return customers;
    }
    @Override
    public void update(Customer customer) {
        String sql = "UPDATE Customer SET first_name = ?, surname = ?, address = ?, phone_number = ?, password = ? WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getSurname());
            stmt.setString(3, customer.getAddress());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getPassword());
            stmt.setInt(6, customer.getCustomerId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // Add this method inside CustomerDAOImpl
    public Customer findByPhoneNumber(String phoneNumber) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE phone_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phoneNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Customer c = new Customer(
                        rs.getString("first_name"),
                        rs.getString("surname"),
                        rs.getString("address"),
                        rs.getString("password"),
                        rs.getString("phone_number")
                );
                c.setCustomerId(rs.getInt("customer_id"));
                return c;
            }
        }
        return null;
    }

    @Override
    public void delete(int customerId) {
        String sql = "DELETE FROM Customer WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
