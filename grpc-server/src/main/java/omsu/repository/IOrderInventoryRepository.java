package omsu.repository;

import java.util.UUID;

public interface IOrderInventoryRepository {
    boolean create(final UUID order_id, final UUID inventory_id, final long quantity);
    boolean updateCount(final UUID order_id, final UUID inventory_id, final long newQuantity);
    boolean delete(final UUID order_id, final UUID inventory_id);
}
