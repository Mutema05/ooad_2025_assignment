package Core;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;        // DB PK
    private int accountId;            // sender account FK
    private String transactionType;   // "Deposit", "Withdraw", "Transfer"
    private double amount;
    private Integer targetAccountId;  // receiver account FK (nullable)
    private LocalDateTime date;
    private String senderName;
    private String receiverName;
    private String accountType;


    public Transaction(int accountId, String transactionType, double amount, Integer targetAccountId) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.targetAccountId = targetAccountId;
        this.date = LocalDateTime.now();
    }

    // ✅ Getters and setters for DAO
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getAccountId() { return accountId; }
    public String getTransactionType() { return transactionType; }
    public double getAmount() { return amount; }
    public Integer getTargetAccountId() { return targetAccountId; }
    public LocalDateTime getDate() { return date; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String name) { this.senderName = name; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String name) { this.receiverName = name; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String type) { this.accountType = type; }

}
