-- apply changes
create table server_vo (
  container_id                  varchar(255),
  name                          varchar(255) not null,
  alias_name                    varchar(255) not null,
  abbr                          varchar(255) not null,
  location                      varchar(255) not null,
  start_command                 varchar(255) not null,
  last_crash_date               timestamp,
  last_start_date               timestamp,
  image_id                      bigint not null,
  container_port                integer not null,
  host_port                     integer not null
);

alter table server drop constraint if exists fk_server_image_id;
alter table server drop constraint uq_server_image_id;
