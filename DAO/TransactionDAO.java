package DAO;

import Core.Transaction;
import java.util.List;

public interface TransactionDAO {
    void create(Transaction transaction);
    Transaction read(int transactionId);
    void update(Transaction transaction);
    void delete(int transactionId);
    List<Transaction> getAllTransactions();

    // NEW – fetch user transactions with friendly names
    List<Transaction> getByCustomerId(int customerId);
}

