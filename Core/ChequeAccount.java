package Core;

public class ChequeAccount extends Account {

    public ChequeAccount(String accountNumber, String branch, double balance, String employerName, String employerAddress) {
        super(accountNumber, branch, balance);
        this.accountType = "ChequeAccount";
        this.employerName = employerName;
        this.employerAddress = employerAddress;
    }

    @Override
    public String withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return "Transaction successful";
        } else return "Invalid or insufficient funds.";
    }

    @Override
    public void applyMonthlyInterest() {
        // No interest for Cheque Accounts
    }
}
