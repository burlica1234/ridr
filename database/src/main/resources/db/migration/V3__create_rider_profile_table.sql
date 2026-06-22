CREATE TABLE rider_profiles (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL UNIQUE,
                                full_name VARCHAR(150) NOT NULL,
                                phone_number VARCHAR(30),
                                date_of_birth DATE,
                                preferred_city VARCHAR(100),
                                avatar_url VARCHAR(512),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
