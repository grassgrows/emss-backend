-- apply changes
alter table permission_group add column permitted_location clob default '[]' not null;

