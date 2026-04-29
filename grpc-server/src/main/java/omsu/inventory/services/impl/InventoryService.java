package omsu.inventory.services.impl;

import omsu.inventory.model.InventoryEntity;
import omsu.inventory.repository.IInventoryRepository;
import omsu.inventory.services.IInventoryService;

import java.util.UUID;

public class InventoryService implements IInventoryService {
    private final IInventoryRepository repository;

    public InventoryService(IInventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public UUID create(InventoryEntity entity) {
        return repository.create(entity);
    }

    @Override
    public boolean update(InventoryEntity entity) {
        return repository.update(entity);
    }

    @Override
    public InventoryEntity getById(UUID uuid) {
        return repository.getById(uuid);
    }

    @Override
    public void deleteById(UUID uuid) {
        repository.deleteById(uuid);
    }
}
