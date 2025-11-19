package Core;

import DAO.TransactionDAO;
import java.util.ArrayList;
import java.util.List;

public class Customer {
    private int customerId;
    private String firstName;
    private String surname;
    private String address;
    private String password;
    private String phoneNumber;
    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private String Username;

    // DAO for database operations
    private TransactionDAO transactionDAO;

    public Customer(String firstName, String surname, String address, String password, String phoneNumber) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.Username = firstName + surname;
    }

    // 🧩 Optional constructor or setter to inject DAO
    public void setTransactionDAO(TransactionDAO transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    // ✅ Getters and setters
    public int getCustomerId() { return customerId; }
    public String getUsername() { return Username; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getFirstName() { return firstName; }
    public String getSurname() { return surname; }
    public String getAddress() { return address; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<Account> getAccounts() { return accounts; }
    public String getAccountTypes() {
        if (accounts == null || accounts.isEmpty()) {
            return "None";
        }

        StringBuilder sb = new StringBuilder();
        for (Account a : accounts) {
            String type = a.getClass().getSimpleName().replace("Account", ""); // "Savings", "Cheque", etc.
            sb.append(type).append(", ");
        }
        return sb.substring(0, sb.length() - 2); // remove trailing comma
    }
    public List<Transaction> getTransactions() { return transactions; }

    public void addAccount(Account account) { accounts.add(account); }

    // 🧠 Updated method — saves transaction to DB too
    public void addTransaction(Transaction t) {
        transactions.add(t);  // store temporarily in memory
        if (transactionDAO != null) {
            transactionDAO.create(t);  // 💾 persist to DB
            System.out.println("✅ Transaction saved to database for customer: " + getFullName());
        } else {
            System.out.println("⚠️ TransactionDAO not set — transaction only stored in memory.");
        }
    }

    public String getFullName() { return firstName + " " + surname; }

    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }
}
