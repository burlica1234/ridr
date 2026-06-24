CREATE TABLE vehicle_types (
                               id UUID PRIMARY KEY,
                               code VARCHAR(50) NOT NULL UNIQUE,
                               display_name VARCHAR(100) NOT NULL,
                               default_max_speed INTEGER NOT NULL,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_vehicles (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL,
                               vehicle_type_id UUID NOT NULL REFERENCES vehicle_types(id),
                               nickname VARCHAR(80),
                               routing_preference VARCHAR(30) NOT NULL,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_vehicles_user_id ON user_vehicles (user_id);

INSERT INTO vehicle_types (id, code, display_name, default_max_speed) VALUES
                                                                          (gen_random_uuid(), 'BICYCLE', 'Bicycle', 25),
                                                                          (gen_random_uuid(), 'E_BIKE', 'Electric Bike', 25),
                                                                          (gen_random_uuid(), 'E_SCOOTER', 'Electric Scooter', 20),
                                                                          (gen_random_uuid(), 'KICK_SCOOTER', 'Kick Scooter', 15),
                                                                          (gen_random_uuid(), 'SKATEBOARD', 'Skateboard', 15);
