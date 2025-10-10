public class SavingsAccount extends Account {

    private static final double INTEREST_RATE = 0.0005; // 0.05%

    public SavingsAccount(String accountNumber, String branch, double balance) {
        super(accountNumber, branch, balance);
    }

    @Override
    public void withdraw(double amount) {
        System.out.println("Withdrawals are not allowed from a Savings Account.");
    }

    @Override
    public void payInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
    }
}
