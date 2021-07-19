-- apply changes
create table group_server (
  id                            bigint generated by default as identity not null,
  group_id                      bigint not null,
  server_id                     bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_group_server primary key (id)
);

create table image (
  id                            bigint generated by default as identity not null,
  name                          varchar(255) not null,
  repository                    varchar(255) not null,
  tag                           varchar(255) not null,
  can_remove                    boolean default false not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_image primary key (id)
);

create table permission_group (
  id                            bigint generated by default as identity not null,
  group_name                    varchar(255) not null,
  max_permission_level          integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_permission_group primary key (id)
);

create table server (
  id                            bigint generated by default as identity not null,
  name                          varchar(255) not null,
  alias_name                    varchar(255) not null,
  abbr                          varchar(255) not null,
  location                      varchar(255) not null,
  start_command                 varchar(255) not null,
  image_id                      bigint not null,
  working_dir                   varchar(255) not null,
  port_bindings                 clob not null,
  volume_bind                   clob not null,
  container_id                  varchar(255),
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_server primary key (id)
);

create table server_real_time (
  id                            bigint generated by default as identity not null,
  last_crash_date               timestamp,
  last_start_date               timestamp,
  status                        integer not null,
  server_id                     bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_server_real_time_status check ( status in (0,1,2)),
  constraint pk_server_real_time primary key (id)
);

create table setting (
  type                          integer generated by default as identity not null,
  value                         varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_setting_type check ( type in (0,1,2,3)),
  constraint pk_setting primary key (type)
);

create table user (
  id                            bigint generated by default as identity not null,
  username                      varchar(255) not null,
  password                      varchar(255) not null,
  permission_level              integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_user primary key (id)
);

create table user_group (
  id                            bigint generated by default as identity not null,
  user_id                       bigint not null,
  group_id                      bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_user_group primary key (id)
);

