package Core;

import java.util.ArrayList;
import java.util.List;


public class Customer {
    private String firstName;
    private String surname;
    private String address;
    private List<Account> accounts;
    private  String password;
    private  String phoneNumber;
    private List<Transaction> transactions = new ArrayList<>();



    public Customer(String firstName, String surname, String address,String password,String phoneNumber) {
        this.firstName = firstName;
        this.surname = surname;
        this.address = address;
        this.accounts = new ArrayList<>();
        this.password=password;
        this.phoneNumber=phoneNumber;
    }

    public void addAccount(Account account) {
        accounts.add(account);
    }

    public String getFullName() {
        return firstName+" "+surname;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getSurname() {
        return surname;
    }
    public String getAddress() {
        return address;
    }
    public String getNumber() {
        return phoneNumber;
    }
    public String getUsername() {
        return firstName+surname;
    }
    public String getPassword(){
        return password;
    }
    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
    public List<Account> getAccounts() {
        return accounts;
    }
}
