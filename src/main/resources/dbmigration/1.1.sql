-- apply changes
create table group_server (
  id                            bigint generated by default as identity not null,
  group_id                      bigint not null,
  server_id                     bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_group_server primary key (id)
);

create table permission_group (
  id                            bigint generated by default as identity not null,
  group_name                    varchar(255) not null,
  max_permission_level          integer not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_permission_group primary key (id)
);

create table user_group (
  id                            bigint generated by default as identity not null,
  user_id                       bigint not null,
  group_id                      bigint not null,
  when_created                  timestamp not null,
  when_modified                 timestamp not null,
  constraint pk_user_group primary key (id)
);

alter table user alter column permission_level integer;
