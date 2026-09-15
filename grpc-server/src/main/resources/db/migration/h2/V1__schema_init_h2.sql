-- H2 совместимая версия
-- В H2 UUID генерируется встроенной функцией, не нужно создавать extension

CREATE SCHEMA IF NOT EXISTS inventory_schema;
SET SCHEMA inventory_schema;

create table if not exists inventory
(
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0)
);

CREATE TABLE IF NOT EXISTS orders
(
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    username  VARCHAR(100) UNIQUE NOT NULL,
    status    VARCHAR(10) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    order_id     UUID NOT NULL,
    inventory_id UUID NOT NULL,
    quantity     INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, inventory_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id)
);
