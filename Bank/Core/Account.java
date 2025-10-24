// At the top of Account.java, SavingsAccount.java, etc.
package Core;

public abstract class Account {
    protected String accountNumber;
    protected double balance;
    protected String branch;

    public Account(String accountNumber, String branch, double balance) {
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.balance = balance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public String getBranch() {
        return branch;
    }

    public String deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            return "Transacation successful";

        } else {
            return "Invalid deposit amount.";

        }
    }

    public abstract String withdraw(double amount);
    public abstract void applyMonthlyInterest();
}
