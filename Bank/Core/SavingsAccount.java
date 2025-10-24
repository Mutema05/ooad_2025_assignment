package Core;
public class SavingsAccount extends Account {

    private static final double INTEREST_RATE = 0.0005; // 0.05%
    private String accountType;
    public SavingsAccount(String accountNumber, String branch, double balance) {
        super(accountNumber, branch, balance);
        this.accountType = "SavingsAccount";

    }

    @Override
    public String withdraw(double amount) {
        return "Withdrawals are not allowed from a Savings Account.";
    }
    public String getAccountType(){
        return  accountType;
    }
    @Override
    public void applyMonthlyInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
    }
}
