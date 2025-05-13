-- V2__create_appointment_table.sql

-- First, add the required extension for GiST indexes with range types
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- Create the appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    business_id UUID NOT NULL REFERENCES businesses_profile(id),
    client_id UUID NOT NULL REFERENCES clients_identity(id),
    staff_id UUID NOT NULL REFERENCES business_staff_members(id),
    service_id UUID NOT NULL REFERENCES services(id),
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,

    -- Add exclusion constraint to prevent overlapping appointments
    -- This ensures that no two appointments for the same staff member overlap in time
    -- except for cancelled appointments which are excluded from this constraint
    CONSTRAINT no_overlapping_appointments
    EXCLUDE USING gist (
        staff_id WITH =,
        tsrange(date + start_time, date + end_time, '[]') WITH &&
    )
    WHERE (status <> 'CANCELLED')
);

-- Create indexes for efficient queries
CREATE INDEX idx_app_business ON appointments(business_id);
CREATE INDEX idx_app_client ON appointments(client_id);
CREATE INDEX idx_app_staff ON appointments(staff_id);
CREATE INDEX idx_app_service ON appointments(service_id);
CREATE INDEX idx_app_date_status ON appointments(date, status);
CREATE INDEX idx_app_staff_date ON appointments(staff_id, date);

-- Create a view for upcoming appointments
CREATE VIEW upcoming_appointments AS
SELECT
    a.id,
    a.business_id,
    a.client_id,
    a.staff_id,
    a.service_id,
    a.date,
    a.start_time,
    a.end_time,
    a.status,
    a.notes,
    a.created_at,
    a.updated_at,
    s.name as service_name,
    b.name as business_name,
    c.first_name || ' ' || c.last_name as client_name,
    sm.first_name || ' ' || sm.last_name as staff_name
FROM
    appointments a
JOIN
    services s ON a.service_id = s.id
JOIN
    businesses_profile b ON a.business_id = b.id
JOIN
    clients_identity c ON a.client_id = c.id
JOIN
    business_staff_members sm ON a.staff_id = sm.id
WHERE
    a.date >= CURRENT_DATE
AND
    a.status IN ('SCHEDULED', 'CONFIRMED')
ORDER BY
    a.date ASC, a.start_time ASC;