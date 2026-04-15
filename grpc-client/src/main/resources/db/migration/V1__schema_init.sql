-- create database "inventory" with owner postgres;

create extension if not exists "uuid-ossp";

-- =====================================================
-- 1. Создание базы данных для интернет-магазина
-- =====================================================

-- Создание базы данных


-- Подключение к созданной базе
--\c inventorydb;

-- =====================================================
-- 2. Создание пользователя и назначение прав
-- =====================================================

-- Создание пользователя
--CREATE USER inv_user WITH PASSWORD 'secure';
-- Назначение прав
--GRANT ALL PRIVILEGES ON DATABASE inventory TO inv_user;

-- =====================================================
-- 3. Создание схемы
-- =====================================================

CREATE SCHEMA IF NOT EXISTS inventory_schema;
GRANT ALL ON SCHEMA inventory_schema TO postgres;


create table if not exists "product"
(
    id        UUID unique        not null default uuid_generate_v1(),
    name  varchar(50) unique not null,
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    primary key (id)
);

alter table "product" owner to postgres;
