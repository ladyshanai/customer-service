CREATE DATABASE IF NOT EXISTS customer_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE customer_service;

CREATE TABLE IF NOT EXISTS customer (
    id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name_or_company_name VARCHAR(255),
    document_number VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    phone_number VARCHAR(50),
    email VARCHAR(255) NOT NULL UNIQUE,
    customer_type VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    outstanding_balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    registration_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modification_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

INSERT INTO customer (
    first_name,
    last_name_or_company_name,
    document_number,
    address,
    phone_number,
    email,
    customer_type,
    active,
    outstanding_balance,
    registration_date,
    modification_date
) VALUES
('Ana', 'Gonzalez', 'DOC-1001', 'Av. Siempre Viva 123', '+54 11 4000-1001', 'ana.gonzalez@example.com', 'PERSON', TRUE, 1200.50, NOW(), NOW()),
('Luis', 'Martinez', 'DOC-1002', 'Calle Falsa 456', '+54 11 4000-1002', 'luis.martinez@example.com', 'PERSON', TRUE, 0.00, NOW(), NOW()),
('Carla', 'Fernandez', 'DOC-1003', 'Mitre 789', '+54 11 4000-1003', 'carla.fernandez@example.com', 'PERSON', TRUE, 350.75, NOW(), NOW()),
('Sofia', 'Tech Solutions SRL', 'CUIT-30711222334', 'Parque Industrial 1200', '+54 11 5000-2001', 'contacto@techsolutions.com', 'COMPANY', TRUE, 9800.00, NOW(), NOW()),
('Javier', 'Comercial Delta SA', 'CUIT-30744555667', 'Ruta 9 Km 45', '+54 11 5000-2002', 'ventas@delta.com', 'COMPANY', TRUE, 2150.30, NOW(), NOW());
