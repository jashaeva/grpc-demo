create table if not exists inventory
(
    id        UUID unique        not null default RANDOM_UUID(),
    name      varchar(50) unique not null,
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    primary key (id)
);

CREATE TABLE IF NOT EXISTS orders
(
    id        UUID UNIQUE NOT NULL DEFAULT RANDOM_UUID(),
    username  VARCHAR(100) UNIQUE NOT NULL,
    status    VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    -- добавляем CHECK для эмуляции ENUM
    CONSTRAINT check_order_status CHECK (status IN ('PENDING','CONFIRMED','ACTUAL','CANCELLED','CREATED'))
);

CREATE TABLE order_items (
    order_id     UUID NOT NULL,
    inventory_id UUID NOT NULL,
    quantity     INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, inventory_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id)
);
