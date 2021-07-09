-- apply changes
alter table image alter column image_id set null;
alter table image add column can_remove boolean default false not null;

alter table server alter column container_id set null;
