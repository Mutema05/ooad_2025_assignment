package DAO;

import Core.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAOImpl implements TransactionDAO {
    private Connection connection;

    public TransactionDAOImpl(Connection connection) { this.connection = connection; }

    @Override
    public void create(Transaction transaction) {
        String sql = "INSERT INTO Transaction (account_id, transaction_type, amount, target_account_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getAmount());
            if (transaction.getTargetAccountId() != null)
                stmt.setInt(4, transaction.getTargetAccountId());
            else stmt.setNull(4, Types.INTEGER);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) transaction.setTransactionId(rs.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Transaction read(int transactionId) {
        String sql = "SELECT * FROM Transaction WHERE transaction_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getObject("target_account_id") != null ? rs.getInt("target_account_id") : null
                );
                t.setTransactionId(rs.getInt("transaction_id"));
                return t;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public void update(Transaction transaction) {
        String sql = "UPDATE Transaction SET account_id=?, transaction_type=?, amount=?, target_account_id=? WHERE transaction_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transaction.getAccountId());
            stmt.setString(2, transaction.getTransactionType());
            stmt.setDouble(3, transaction.getAmount());
            if (transaction.getTargetAccountId() != null)
                stmt.setInt(4, transaction.getTargetAccountId());
            else stmt.setNull(4, Types.INTEGER);
            stmt.setInt(5, transaction.getTransactionId());
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public void delete(int transactionId) {
        String sql = "DELETE FROM Transaction WHERE transaction_id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, transactionId);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM Transaction";
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Transaction t = new Transaction(
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getObject("target_account_id") != null ? rs.getInt("target_account_id") : null
                );
                t.setTransactionId(rs.getInt("transaction_id"));
                list.add(t);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public List<Transaction> getByCustomerId(int customerId) {
        List<Transaction> list = new ArrayList<>();

        String sql =
                "SELECT t.transaction_id, t.transaction_type, t.amount, " +
                        "a.account_id AS sender_acc, c.first_name || ' ' || c.surname AS sender_name, " +
                        "a.account_type AS sender_type, " +
                        "t.target_account_id, " +
                        "c2.first_name || ' ' || c2.surname AS receiver_name " +
                        "FROM Transaction t " +
                        "JOIN Account a ON t.account_id = a.account_id " +
                        "JOIN Customer c ON a.customer_id = c.customer_id " +
                        "LEFT JOIN Account a2 ON t.target_account_id = a2.account_id " +
                        "LEFT JOIN Customer c2 ON a2.customer_id = c2.customer_id " +
                        "WHERE c.customer_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, customerId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {

                Transaction t = new Transaction(
                        rs.getInt("sender_acc"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getObject("target_account_id") != null ? rs.getInt("target_account_id") : null
                );

                t.setTransactionId(rs.getInt("transaction_id"));
                t.setSenderName(rs.getString("sender_name"));
                t.setReceiverName(rs.getString("receiver_name"));
                t.setAccountType(rs.getString("sender_type"));

                list.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }


}
