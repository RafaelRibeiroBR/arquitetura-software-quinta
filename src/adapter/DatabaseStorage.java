package adapter;

import domain.EntityInterface;

import java.util.ArrayList;
import java.util.UUID;

public class DatabaseStorage implements PersistInterface {

    private ArrayList<EntityInterface> db = new ArrayList<>();

    @Override
    public void save(EntityInterface entity) {
        db.add(entity);
    }

    @Override
    public void delete(EntityInterface entity) {
        db.remove(entity);
    }

    @Override
    public ArrayList<EntityInterface> listAll() {
        return db;
    }

    @Override
    public EntityInterface findOneById(UUID id) {
        //percorrer a lista (db) - correto
        for (int i = 0; i < db.size(); i++) {
            //se ele encontrar algum elemento da lista que tenha o mesmo
            // UUID, ele retorna esse elemento

            if (db.get(i).getUUID().equals(id)) {

            }
        }
        return null;
    }
}
