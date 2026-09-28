alter table sys_loader.EVENT_LOGS
    add column if not exists APP_NAME varchar(255);
