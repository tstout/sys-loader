create table if not exists sys_loader.EVENT_LOGS (
    ID varchar(50) primary key,
    DATE_TIME timestamp,
    APP_NAME varchar(255),
    THREAD_NAME varchar(255),
    CLASS varchar(100),
    LEVEL varchar(10),
    MESSAGE varchar(4000),
    EXCEPTION varchar(4000)
);