alter table sys_loader.EVENT_LOGS
    add column if not exists THREAD_NAME varchar(255);
