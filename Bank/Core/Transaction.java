package Core;


import java.time.LocalDateTime;

public class Transaction {
    private String transactionId;
    private String type; // e.g. "Withdraw" or "Transfer"
    private double amount;
    private String senderAccount;
    private String receiverAccount; // null for withdraw
    private LocalDateTime date;

    public Transaction(String transactionId, String type, double amount, String senderAccount, String receiverAccount) {
        this.transactionId = transactionId;
        this.type = type;
        this.amount = amount;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.date = LocalDateTime.now();
    }

    // Getters and toString()
    public String toString() {
        return date + " | " + type + " | " + amount + " | " + senderAccount +
                (receiverAccount != null ? " -> " + receiverAccount : "");
    }
    // ✅ Add getters
    public String getId() {
        return transactionId;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }}
