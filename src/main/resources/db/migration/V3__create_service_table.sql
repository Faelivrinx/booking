CREATE TABLE services (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
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