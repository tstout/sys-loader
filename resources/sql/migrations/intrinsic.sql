create table if not exists migrations (
    name varchar primary key NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

