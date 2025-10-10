public class ChequeAccount extends Account {

    private String employerName;
    private String employerAddress;

    public ChequeAccount(String accountNumber, String branch, double balance,
                         String employerName, String employerAddress) {
        super(accountNumber, branch, balance);
        this.employerName = employerName;
        this.employerAddress = employerAddress;
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
        System.out.println("Cheque accounts do not earn interest.");
    }
}
