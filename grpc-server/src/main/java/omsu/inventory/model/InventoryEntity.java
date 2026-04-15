package omsu.inventory.model;


import java.util.Objects;
import java.util.UUID;

public class InventoryEntity {
    private UUID id;
    private String name;
    private long count;

    public InventoryEntity(UUID id, String name, long count) {
        this.name = name;
        this.count = count;
        this.id = id;
    }


    public InventoryEntity(String name, long count) {
        this.name = name;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InventoryEntity that = (InventoryEntity) o;
        return getCount() == that.getCount() && Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getCount());
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
