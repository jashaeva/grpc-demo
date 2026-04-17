create extension if not exists "uuid-ossp";

alter table inventory_schema."product"  RENAME TO inventory_schema."inventory";

