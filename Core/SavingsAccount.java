package Core;

public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.0005;

    public SavingsAccount(String accountNumber, String branch, double balance) {
        super(accountNumber, branch, balance);
        this.accountType = "SavingsAccount";
    }

    @Override
    public String withdraw(double amount) {
        return "Withdrawals are not allowed from a Savings Account.";
    }

    @Override
    public void applyMonthlyInterest() {
        balance += balance * INTEREST_RATE;
    }
}
