package hr.javafx.onlinelearningsystem.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface UserRepository<T> {

    List<T> findAll();

    T findByUsername(String name);

    T findById (Integer id);

    T getEntityFromResultSet (ResultSet resultSet) throws SQLException;

    void saveAll(List<T> entities);

    void save(T entity);

    void delete(String username);

    void update(T entity, String username);

    Integer count();
}