package DAO;

import Core.Account;
import java.util.List;

public interface AccountDAO {
    void create(Account account);
    Account read(int accountId);
    void update(Account account);
    void delete(int accountId);
    List<Account> getAllAccounts();

    // Get all accounts for a specific customer
    List<Account> getAccountsByCustomerId(int customerId);

    // ✅ Get account by its account number (needed for transfers)
    Account getByAccountNumber(String accountNumber);
}
