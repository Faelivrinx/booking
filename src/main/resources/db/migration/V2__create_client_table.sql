CREATE TABLE clients (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verification_code VARCHAR(10),
    verification_code_expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create index on email for faster lookups
CREATE INDEX idx_client_email ON clients(email);

-- Create index on phone_number for faster lookups
CREATE INDEX idx_client_phone_number ON clients(phone_number);

-- Create index on keycloak_id for faster lookups
CREATE INDEX idx_client_keycloak_id ON clients(keycloak_id);