create SCHEMA IF NOT EXISTS sys_loader;

create table if not exists sys_loader.migrations (
    name varchar primary key NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

