create table users (
                       id bigserial primary key,
                       username varchar(100) not null unique,
                       password_hash varchar(200) not null,
                       enabled boolean not null default true,
                       created_at timestamptz not null default now()
);

create table roles (
                       id bigserial primary key,
                       name varchar(100) not null unique
);

create table user_roles (
                            user_id bigint not null references users(id) on delete cascade,
                            role_id bigint not null references roles(id) on delete cascade,
                            primary key (user_id, role_id)
);