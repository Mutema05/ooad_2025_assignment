CREATE TABLE Customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    surname VARCHAR(50) NOT NULL,
    address VARCHAR(100),
    phone_number VARCHAR(20),
    password VARCHAR(50) NOT NULL
);

CREATE TABLE Account (
    account_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    branch VARCHAR(50),
    balance DECIMAL(15, 2) DEFAULT 0.0,
    account_type VARCHAR(20) NOT NULL,
    employer_name VARCHAR(50),
    employer_address VARCHAR(100),
    CONSTRAINT fk_customer FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

CREATE TABLE Transaction (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    target_account_id INT,
    description VARCHAR(100),
    CONSTRAINT fk_account FOREIGN KEY (account_id) REFERENCES Account(account_id),
    CONSTRAINT fk_target_account FOREIGN KEY (target_account_id) REFERENCES Account(account_id)
);
