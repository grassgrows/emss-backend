-- apply changes
create table image (
  id                            bigint generated by default as identity not null,
  name                          varchar(255) not null,
  dockerfile_root_directory     varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_image primary key (id)
);

create table server (
  id                            bigint generated by default as identity not null,
  container_id                  varchar(255) not null,
  name                          varchar(255) not null,
  alias_name                    varchar(255) not null,
  abbr                          varchar(255) not null,
  location                      varchar(255) not null,
  start_command                 varchar(255) not null,
  last_crash_date               timestamp,
  last_start_date               timestamp,
  container_port                integer not null,
  host_port                     integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_server primary key (id)
);

create table setting (
  id                            bigint generated by default as identity not null,
  type                          integer not null,
  value                         varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint ck_setting_type check ( type in (0,1)),
  constraint pk_setting primary key (id)
);

create table user (
  id                            bigint generated by default as identity not null,
  username                      varchar(255) not null,
  password                      varchar(255) not null,
  permission_level              varchar(255) not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_user primary key (id)
);

create table user_server (
  id                            bigint generated by default as identity not null,
  user_id                       bigint not null,
  server_id                     bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_user_server primary key (id)
);

