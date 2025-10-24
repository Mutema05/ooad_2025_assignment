package Core;
public class ChequeAccount extends Account {

    private String employerName;
    private String employerAddress;
    private String accountType;

    public ChequeAccount(String accountNumber, String branch, double balance,
                         String employerName, String employerAddress) {
        super(accountNumber, branch, balance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
        this.accountType = "ChequeAccount";
    }

    @Override
    public String withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            System.out.println(balance -= amount);
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
        System.out.println("Cheque accounts do not earn interest.");
    }
}
