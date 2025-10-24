package Models;

import Core.*;
import java.util.*;

public class BankData {

    // ✅ 1. Static list to store all customers
    private static final List<Customer> customers = new ArrayList<>();

    // ✅ 2. Static block to pre-fill data
    static {
        // Create customers
        Customer atang = new Customer("Atang", "Mutema", "Gaborone, Botswana", "1234", "71727384");
        Customer jane = new Customer("Jane", "Doe", "Francistown, Botswana", "abcd", "75635241");
        Customer john = new Customer("John", "Smith", "Maun, Botswana", "pass123", "74758602");
        Customer mary = new Customer("Mary", "Johnson", "Lobatse, Botswana", "mypassword", "745362719");

        // Assign accounts
        atang.addAccount(new SavingsAccount("SA1001", "Gaborone Branch", 5000.0));
        atang.addAccount(new ChequeAccount("CA1001", "Gaborone Branch", 1500.0, "Bac", "Tlokweng"));
        atang.addAccount(new InvestmentAccount("IA1001", "Gaborone Branch", 500.0));

        jane.addAccount(new SavingsAccount("SA1002", "Francistown Branch", 3000.0));
        john.addAccount(new SavingsAccount("SA1003", "Maun Branch", 4000.0));
        mary.addAccount(new SavingsAccount("SA1004", "Lobatse Branch", 3500.0));

        // Transactions
        atang.addTransaction(new Transaction(UUID.randomUUID().toString(), "Deposit", 5000.0, null, "SA1001"));
        atang.addTransaction(new Transaction(UUID.randomUUID().toString(), "Transfer", 1000.0, "SA1001", "SA1002"));

        // Add customers to list
        customers.add(atang);
        customers.add(jane);
        customers.add(john);
        customers.add(mary);
    }

    // ✅ 3. Getter for all customers
    public static List<Customer> getCustomers() {
        return customers;
    }



    // ✅ 5. Optional helper - find a customer by phone or name
    public static Customer findCustomerByName(String name) {
        for (Customer c : customers) {
            if ((c.getFirstName() + " " + c.getSurname()).equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    // ✅ 6. Optional - clear all data (for testing)
    public static void clearData() {
        customers.clear();
    }
}
