create table projects (
   id bigint not null auto_increment,
    name varchar(255) not null,
    api_name varchar(255),
    api_endpoint_url varchar(255),
    db_name varchar(255),
    user_id bigint,
    schema_content text,
    primary key (id)
) engine=InnoDB;

create table users (
   id bigint not null auto_increment,
    email varchar(255) not null,
    username varchar(255) not null,
    ssh_public_key VARCHAR (255),
    primary key (id)
) engine=InnoDB;

alter table projects
   add constraint fk_user
   foreign key (user_id)
   references users (id);
