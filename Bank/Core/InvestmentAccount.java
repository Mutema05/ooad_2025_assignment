package Core;
public class InvestmentAccount extends Account {

    private static final double INTEREST_RATE = 0.05; // 5%
    private static final double MIN_INITIAL_DEPOSIT = 500.0;
    private String accountType;

    public InvestmentAccount(String accountNumber, String branch, double balance) {
        super(accountNumber, branch, balance);
        this.accountType = "InvestmentAccount";
        if (balance < MIN_INITIAL_DEPOSIT) {
            throw new IllegalArgumentException("Initial deposit must be at least BWP500.00");
        }
    }

    @Override
    public String withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return "Transacation successful";
        } else {
            return "Invalid or insufficient funds.";
        }
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
