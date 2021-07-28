-- apply changes
alter table server_real_time add column auto_restart boolean default false not null;

