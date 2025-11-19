package Core;

public abstract class Account {
    private int accountId;            // DB primary key
    protected String accountNumber;
    protected double balance;
    protected String branch;
    protected String accountType;
    protected String employerName;
    protected String employerAddress;
    protected int customerId;         // for FK

    public Account(String accountNumber, String branch, double balance) {
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.balance = balance;
    }

    // ✅ Getters and setters for DAO
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getAccountNumber() { return accountNumber; }
    public String getBranch() { return branch; }
    public double getBalance() { return balance; }
    public String getAccountType() { return accountType; }
    public String getEmployerName() { return employerName; }
    public String getEmployerAddress() { return employerAddress; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String deposit(double amount) {
        if (amount > 0) { balance += amount; return "Transaction successful"; }
        else return "Invalid deposit amount.";
    }

    public abstract String withdraw(double amount);
    public abstract void applyMonthlyInterest();
}
