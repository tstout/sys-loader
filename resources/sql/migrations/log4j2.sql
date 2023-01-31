create table EVENT_LOGS (
    ID varchar(50) primary key,
    DATE_TIME timestamp,
    CLASS varchar(100),
    LEVEL varchar(10),
    MESSAGE varchar(4000),
    EXCEPTION varchar(4000)
);