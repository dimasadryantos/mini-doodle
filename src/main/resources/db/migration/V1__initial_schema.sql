-- btree_gist lets us combine an equality column (calendar_id) with a range
-- operator (&&) in one exclusion constraint.
CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- One personal calendar per user. The calendar is an internal domain concept
-- and is never exposed through the API.
CREATE TABLE calendars (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    timezone   VARCHAR(64) NOT NULL DEFAULT 'UTC',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE time_slots (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id UUID        NOT NULL REFERENCES calendars (id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    status      VARCHAR(16) NOT NULL,
    version     BIGINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_slot_range  CHECK (end_time > start_time),
    CONSTRAINT chk_slot_status CHECK (status IN ('FREE', 'BUSY')),
    -- Slots of the same calendar must never overlap. Enforced by the database
    -- so it also holds under concurrent inserts/updates.
    CONSTRAINT excl_slot_overlap EXCLUDE USING gist (
        calendar_id WITH =,
        tstzrange(start_time, end_time, '[)') WITH &&
    )
);

-- Range queries ("slots of calendar X between A and B") are the hot path.
CREATE INDEX idx_time_slots_calendar_start ON time_slots (calendar_id, start_time);

CREATE TABLE meetings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slot_id     UUID         NOT NULL UNIQUE REFERENCES time_slots (id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE meeting_participants (
    meeting_id UUID         NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    email      VARCHAR(255) NOT NULL,
    PRIMARY KEY (meeting_id, email)
);
