package DAO;

import Core.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAOImpl implements AccountDAO {
    private Connection connection;

    public AccountDAOImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void create(Account account) {
        String sql = "INSERT INTO Account (customer_id, account_number, branch, balance, account_type, employer_name, employer_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, account.getCustomerId());
            stmt.setString(2, account.getAccountNumber());
            stmt.setString(3, account.getBranch());
            stmt.setDouble(4, account.getBalance());
            stmt.setString(5, account.getAccountType());
            stmt.setString(6, account.getEmployerName());
            stmt.setString(7, account.getEmployerAddress());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                account.setAccountId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Account read(int accountId) {
        String sql = "SELECT * FROM Account WHERE account_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ✅ NEW METHOD for lookup by account number
    @Override
    public Account getByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM Account WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void update(Account account) {
        String sql = "UPDATE Account SET customer_id=?, account_number=?, branch=?, balance=?, account_type=?, employer_name=?, employer_address=? WHERE account_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, account.getCustomerId());
            stmt.setString(2, account.getAccountNumber());
            stmt.setString(3, account.getBranch());
            stmt.setDouble(4, account.getBalance());
            stmt.setString(5, account.getAccountType());
            stmt.setString(6, account.getEmployerName());
            stmt.setString(7, account.getEmployerAddress());
            stmt.setInt(8, account.getAccountId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(int accountId) {
        String sql = "DELETE FROM Account WHERE account_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public List<Account> getAccountsByCustomerId(int customerId) {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM Account WHERE customer_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accounts;
    }

    public boolean customerHasAccountType(int customerId, String accountType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Account WHERE customer_id = ? AND account_type = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, accountType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ✅ Helper method to reduce duplication
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        String type = rs.getString("account_type");
        Account acc;
        switch (type) {
            case "ChequeAccount":
                acc = new Core.ChequeAccount(
                        rs.getString("account_number"),
                        rs.getString("branch"),
                        rs.getDouble("balance"),
                        rs.getString("employer_name"),
                        rs.getString("employer_address")
                );
                break;
            case "SavingsAccount":
                acc = new Core.SavingsAccount(
                        rs.getString("account_number"),
                        rs.getString("branch"),
                        rs.getDouble("balance")
                );
                break;
            case "InvestmentAccount":
                acc = new Core.InvestmentAccount(
                        rs.getString("account_number"),
                        rs.getString("branch"),
                        rs.getDouble("balance")
                );
                break;
            default:
                return null;
        }
        acc.setAccountId(rs.getInt("account_id"));
        acc.setCustomerId(rs.getInt("customer_id"));
        return acc;
    }
}
