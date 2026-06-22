CREATE TABLE auth_sessions (
                               id UUID PRIMARY KEY,
                               user_id UUID NOT NULL REFERENCES auth_users(id) ON DELETE CASCADE,
                               refresh_token_hash VARCHAR(255) NOT NULL UNIQUE,
                               expires_at TIMESTAMPTZ NOT NULL,
                               revoked_at TIMESTAMPTZ,
                               created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_auth_sessions_user_id ON auth_sessions (user_id);
