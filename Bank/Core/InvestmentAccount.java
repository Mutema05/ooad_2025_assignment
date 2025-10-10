public class InvestmentAccount extends Account {

    private static final double INTEREST_RATE = 0.05; // 5%
    private static final double MIN_INITIAL_DEPOSIT = 500.0;

    public InvestmentAccount(String accountNumber, String branch, double balance) {
        super(accountNumber, branch, balance);
        if (balance < MIN_INITIAL_DEPOSIT) {
            throw new IllegalArgumentException("Initial deposit must be at least BWP500.00");
        }
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        } else {
            System.out.println("Invalid or insufficient funds.");
        }
    }

    @Override
    public void payInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
    }
}
