create table if not exists migrations (
    name varchar NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

