CREATE TABLE user_profiles (
    id UUID PRIMARY KEY,
    keycloak_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(50) NOT NULL,
    business_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Create indexes for better query performance
CREATE INDEX idx_user_profiles_keycloak_id ON user_profiles(keycloak_id);
CREATE INDEX idx_user_profiles_email ON user_profiles(email);
CREATE INDEX idx_user_profiles_business_id ON user_profiles(business_id);
CREATE INDEX idx_user_profiles_role ON user_profiles(role);

-- Add constraint to ensure business_id is set for business owners and employees
ALTER TABLE user_profiles ADD CONSTRAINT chk_business_id_for_business_users
    CHECK (
        (role IN ('BUSINESS_OWNER', 'EMPLOYEE') AND business_id IS NOT NULL) OR
        (role NOT IN ('BUSINESS_OWNER', 'EMPLOYEE'))
    );