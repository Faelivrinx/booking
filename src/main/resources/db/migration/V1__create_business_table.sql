CREATE TABLE businesses_identity (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
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