create extension if not exists "uuid-ossp";

create table if not exists public.inventory
(
    id        UUID unique        not null default uuid_generate_v1(),
    name      varchar(50) unique not null,
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    primary key (id)
);

alter table public.inventory owner to postgres;
