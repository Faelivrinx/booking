--- BUSINESS

-- Create businesses table
CREATE TABLE businesses_profile (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255),
    postal_code VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create business identity
CREATE TABLE businesses_identity (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    business_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create index on email for faster lookups
CREATE INDEX idx_business_email ON businesses_identity(email);

-- Create index on keycloak_id for faster lookups
CREATE INDEX idx_business_keycloak_id ON businesses_identity(keycloak_id);


-- Business services
CREATE TABLE services (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses_identity(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    duration_minutes INT NOT NULL,
    description VARCHAR(500),
    price DECIMAL(10, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    -- Ensure name is unique per business
    CONSTRAINT uk_business_service_name UNIQUE (business_id, name)
);

-- Index for faster lookups of services by business
CREATE INDEX idx_services_business_id ON services(business_id);

---------- CLIENTS ---------
CREATE TABLE clients_identity (
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
CREATE INDEX idx_client_email ON clients_identity(email);

-- Create index on phone_number for faster lookups
CREATE INDEX idx_client_phone_number ON clients_identity(phone_number);

-- Create index on keycloak_id for faster lookups
CREATE INDEX idx_client_keycloak_id ON clients_identity(keycloak_id);