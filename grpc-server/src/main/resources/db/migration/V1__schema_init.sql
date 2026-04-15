create extension if not exists "uuid-ossp";

CREATE SCHEMA IF NOT EXISTS inventory_schema;
GRANT ALL ON SCHEMA inventory_schema TO postgres;

create table if not exists inventory_schema."product"
(
    id        UUID unique        not null default uuid_generate_v1(),
    name  varchar(50) unique not null,
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    primary key (id)
);

alter table inventory_schema."product" owner to postgres;

