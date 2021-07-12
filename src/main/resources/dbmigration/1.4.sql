-- apply changes
alter table server alter column port_bindings set null;
alter table server alter column volume_bind set null;
