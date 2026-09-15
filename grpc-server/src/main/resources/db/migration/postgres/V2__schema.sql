CREATE TABLE IF NOT EXISTS orders
(
    id        UUID UNIQUE NOT NULL DEFAULT uuid_generate_v1(),
    username  VARCHAR(100) UNIQUE NOT NULL,
    status    VARCHAR(10) NOT NULL DEFAULT 'CREATED',  -- используем созданный тип
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE order_items (
    order_id     UUID NOT NULL,
    inventory_id UUID NOT NULL,
    quantity     INT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, inventory_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id)
);
alter table orders owner to postgres;
alter table order_items owner to postgres;


