-- src/main/resources/db/migration/V2__create_offer_tables.sql

-- Business table
CREATE TABLE businesses (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    owner_id VARCHAR(255) NOT NULL, -- Keycloak ID
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Services table
CREATE TABLE services (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

-- Staff members table
CREATE TABLE staff_members (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    job_title VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

-- Staff service assignments (many-to-many)
CREATE TABLE staff_service_assignments (
    staff_id UUID NOT NULL,
    service_id UUID NOT NULL,
    business_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL,
    PRIMARY KEY (staff_id, service_id),
    FOREIGN KEY (staff_id) REFERENCES staff_members(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_businesses_owner_id ON businesses(owner_id);
CREATE INDEX idx_businesses_is_active ON businesses(is_active);
CREATE INDEX idx_businesses_name ON businesses(name);

CREATE INDEX idx_services_business_id ON services(business_id);
CREATE INDEX idx_services_is_active ON services(is_active);
CREATE INDEX idx_services_business_name ON services(business_id, name);

CREATE INDEX idx_staff_members_business_id ON staff_members(business_id);
CREATE INDEX idx_staff_members_keycloak_id ON staff_members(keycloak_id);
CREATE INDEX idx_staff_members_email ON staff_members(email);
CREATE INDEX idx_staff_members_is_active ON staff_members(is_active);
CREATE INDEX idx_staff_members_business_email ON staff_members(business_id, email);

CREATE INDEX idx_staff_service_assignments_staff_id ON staff_service_assignments(staff_id);
CREATE INDEX idx_staff_service_assignments_service_id ON staff_service_assignments(service_id);
CREATE INDEX idx_staff_service_assignments_business_id ON staff_service_assignments(business_id);

-- Constraints to ensure data integrity
ALTER TABLE services ADD CONSTRAINT uq_services_business_name UNIQUE (business_id, name);
ALTER TABLE staff_members ADD CONSTRAINT uq_staff_business_email UNIQUE (business_id, email);

-- Comments for documentation
COMMENT ON TABLE businesses IS 'Business profiles with basic information';
COMMENT ON TABLE services IS 'Services offered by businesses with fixed pricing';
COMMENT ON TABLE staff_members IS 'Staff members associated with businesses';
COMMENT ON TABLE staff_service_assignments IS 'Many-to-many relationship between staff and services';

COMMENT ON COLUMN businesses.owner_id IS 'Keycloak user ID of the business owner';
COMMENT ON COLUMN services.duration_minutes IS 'Service duration in minutes';
COMMENT ON COLUMN services.price IS 'Fixed price for the service';
COMMENT ON COLUMN staff_members.keycloak_id IS 'Keycloak user ID for the staff member';
COMMENT ON COLUMN staff_service_assignments.business_id IS 'Denormalized business_id for easier querying';