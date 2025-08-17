-- create_table.sql
CREATE DATABASE IF NOT EXISTS customers_db;
USE customers_db;

CREATE TABLE IF NOT EXISTS customers (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    lastName VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL,
    country VARCHAR(100),
    registered_at DATETIME
    );


-- Ejemplo
INSERT INTO customers(name, email, country, registered_at) VALUES
("John", "Doe", "john.die@example.com", "USA", NOW()),
("Ana", "Gomez", "ana.gomez@example.com", "Argentina", NOW()),
("Invalid", "Invalid", "ivalid-email", "Chile", NOW());


--To import CSV into MySQL
LOAD DATA LOCAL INFILE '/absolute/path/to/customers_seed.csv'
INTO TABLE customers
FIELDS TERMINATED BY ','
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(name, email, country, registered_at);