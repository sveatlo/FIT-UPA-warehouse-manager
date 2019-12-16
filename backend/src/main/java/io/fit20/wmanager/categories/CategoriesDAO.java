package io.fit20.wmanager.categories;

import io.fit20.wmanager.misc.NotFoundException;
import io.fit20.wmanager.products.Product;
import oracle.jdbc.OracleTypes;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

public class CategoriesDAO {
    private Connection connection;

    private static final String SQL_INSERT_NEW = "BEGIN INSERT INTO categories(name, parent) VALUES(?, ?) returning id into ?; END;";
    private static final String SQL_UPDATE_DATA = "UPDATE categories SET name = ?, parent = ? WHERE id = ?";
    private static final String SQL_SELECT_BY_ID = "SELECT id, name, parent FROM categories WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM categories WHERE id = ?";

    public CategoriesDAO(Connection conn) {
        this.connection = conn;
    }

    public void save(Category category) throws Exception {
        if (category.id == 0) {
            this.create(category);
            return;
        }

        this.update(category);

    }
    private void create(Category category) throws SQLException, NotFoundException, IOException {
        try(CallableStatement stmt = connection.prepareCall(SQL_INSERT_NEW)) {
            stmt.setString(1, category.name);
            if (category.parent == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, category.parent);
            }
            stmt.registerOutParameter(3, OracleTypes.INTEGER);

            stmt.execute();
            category.id = stmt.getInt(3);
        }
    }
    private void update(Category category) throws SQLException, NotFoundException, IOException {
        // insert failed, try update
        try(PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_DATA)) {
            stmt.setString(1, category.name);
            if (category.parent == null) {
                stmt.setNull(2, Types.INTEGER);
            } else {
                stmt.setInt(2, category.parent);
            }
            stmt.setFloat(3, category.id);

            stmt.executeUpdate();
        }
    }

    public Category getByID(int id) throws SQLException, NotFoundException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return this.categoryFromResultSet(resultSet);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    private Category categoryFromResultSet(ResultSet resultSet) throws SQLException {
        return new Category(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getInt("parent")
        );
    }

    public ArrayList<Category> getAll() throws SQLException, IOException {
        return this.getAll("id", "asc", 10, 0);
    }
    public ArrayList<Category> getAll(String orderField, String orderDir, int limit, int offset) throws SQLException, IOException {
        // TODO: sanitize
        final String SQL_SELECT_ALL = "SELECT id, name, parent FROM categories ORDER BY " + orderField + " " + orderDir + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            try (ResultSet resultSet = stmt.executeQuery()) {
                ArrayList<Category> categories = new ArrayList<Category>();

                while (resultSet.next()) {
                    categories.add(this.categoryFromResultSet(resultSet));
                }

                return categories;
            }
        }
    }


    public int countAll() throws SQLException, NotFoundException {
        final String SQL_COUNT_ALL = "SELECT COUNT(id) FROM categories";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_COUNT_ALL)) {
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    public void deleteByID(int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DELETE_BY_ID)) {
            stmt.setInt(1, id);

            stmt.execute();
        }
    }

}
