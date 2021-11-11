create SCHEMA IF NOT EXISTS sys_loader_test;

create table if not exists sys_loader_test.users (
    user_name varchar primary key NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);