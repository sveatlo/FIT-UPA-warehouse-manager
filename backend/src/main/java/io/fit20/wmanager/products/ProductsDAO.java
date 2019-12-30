package io.fit20.wmanager.products;

import io.fit20.wmanager.misc.NotFoundException;
import oracle.jdbc.OraclePreparedStatement;
import oracle.jdbc.OracleResultSet;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.pooling.Tuple;
import oracle.ord.im.OrdImage;
import oracle.spatial.geometry.JGeometry;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductsDAO {
    private Connection connection;
    private static final String SQL_UPDATE_IMAGE = "UPDATE products SET image = ? WHERE id = ?";
    private static final String SQL_UPDATE_STILLIMAGE = "UPDATE products p SET p.image_si = SI_StillImage(p.image.getContent()) WHERE p.id = ?"; // an SQL method call needs to be on table.column, not just column
    private static final String SQL_UPDATE_STILLIMAGE_META = "UPDATE products SET image_ac = SI_AverageColor(image_si), image_ch = SI_ColorHistogram(image_si), image_pc = SI_PositionalColor(image_si), image_tx = SI_Texture(image_si) WHERE id = ?";
    private static final String SQL_DELETE_BY_ID = "DELETE FROM products WHERE id = ?";

    public ProductsDAO(Connection conn) {
        this.connection = conn;
    }

    public void save(Product product) throws Exception {
        if (product.id == 0) {
            this.create(product);
            return;
        }

        this.update(product);
    }

    private void create(Product product) throws Exception {
        final String SQL_INSERT_NEW = "BEGIN INSERT INTO products(name, category_id, price, image, geometry, geometry_meta_type, geometry_meta_radius, geometry_meta_width, geometry_meta_height) VALUES(?, ?, ?, ordsys.ordimage.init(), ?, ?, ?, ?, ?) returning id into ?; END;";
        try(CallableStatement stmt = connection.prepareCall(SQL_INSERT_NEW)) {
            stmt.setString(1, product.name);
            stmt.setInt(2, product.categoryID);
            stmt.setFloat(3, product.price);

            if (product.getJGeometry() == null) {
                throw new Exception("Geometry must be set");
            } else {
                Struct obj = JGeometry.storeJS(this.connection, product.getJGeometry());
                stmt.setObject(4, obj);
            }

            stmt.setString(5, product.geometry.type);
            if (product.geometry.radius == null) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setDouble(6, product.geometry.radius);
            }
            if (product.geometry.width == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setDouble(7, product.geometry.width);
            }
            if (product.geometry.height == null) {
                stmt.setNull(8, Types.INTEGER);
            } else {
                stmt.setDouble(8, product.geometry.height);
            }

            stmt.registerOutParameter(9, OracleTypes.INTEGER);

            stmt.execute();
            product.id = stmt.getInt(9);

            // data is definitely in DB (or an error was thrown from updateStmt)
            //   => insert image
            if (product.imageData != null && product.imageData.length > 0) {
                this.saveProductImage(product);
            }
        }
    }

    private void update(Product product) throws Exception {
        final String SQL_UPDATE_DATA = "UPDATE products SET name = ?, category_id = ?, price = ?, geometry = ?, geometry_meta_type = ?, geometry_meta_radius = ?, geometry_meta_width = ?, geometry_meta_height = ? WHERE id = ?";
        // insert failed, try update
        try(PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_DATA)) {
            stmt.setString(1, product.name);
            stmt.setInt(2, product.categoryID);
            stmt.setFloat(3, product.price);

            if (product.getJGeometry() == null) {
                throw new Exception("Geometry must be set");
            } else {
                Struct obj = JGeometry.storeJS(this.connection, product.getJGeometry());
                stmt.setObject(4, obj);
            }

            stmt.setString(5, product.geometry.type);
            if (product.geometry.radius == null) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setDouble(6, product.geometry.radius);
            }
            if (product.geometry.width == null) {
                stmt.setNull(7, Types.INTEGER);
            } else {
                stmt.setDouble(7, product.geometry.width);
            }
            if (product.geometry.height == null) {
                stmt.setNull(8, Types.INTEGER);
            } else {
                stmt.setDouble(8, product.geometry.height);
            }

            stmt.setInt(9, product.id);

            stmt.executeUpdate();

            this.saveProductImage(product);
        }
    }

    public double getFootprint(Product product) throws SQLException, NotFoundException {
        final String SQL_GET_FOOTPRINT = "SELECT id, name, SDO_GEOM.SDO_AREA(geometry, 1) as area FROM products WHERE id = ?";
        try(PreparedStatement stmt = connection.prepareStatement(SQL_GET_FOOTPRINT)) {
            stmt.setInt(1, product.id);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("area");
            } else {
                throw new NotFoundException();
            }
        }
    }

    public OrdImage getImage(Product product) throws SQLException, NotFoundException {
        final String SQL_GET_IMAGE_FOR_UPDATE = "SELECT image FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_IMAGE_FOR_UPDATE)) {
            stmt.setInt(1, product.id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    final OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
                    return (OrdImage) oracleResultSet.getORAData(1, OrdImage.getORADataFactory());
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    private OrdImage getImageForUpdate(Product product) throws SQLException, NotFoundException {
        final String SQL_GET_IMAGE_FOR_UPDATE = "SELECT image FROM products WHERE id = ? FOR UPDATE";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_IMAGE_FOR_UPDATE)) {
            stmt.setInt(1, product.id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    final OracleResultSet oracleResultSet = (OracleResultSet) resultSet;
                    return (OrdImage) oracleResultSet.getORAData(1, OrdImage.getORADataFactory());
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }
    private void updateStillImage(Product product) throws SQLException {
        // now recreate still image
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_STILLIMAGE)) {
            stmt.setInt(1, product.id);
            stmt.executeUpdate();
        }
        try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_STILLIMAGE_META)) {
            stmt.setInt(1, product.id);
            stmt.executeUpdate();
        }
    }
    private void deleteProductImage(Product product) throws SQLException {
        final String SQL_DELETE_IMAGE = "UPDATE products SET image = ordsys.ordimage.init() WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DELETE_IMAGE)) {
            stmt.setInt(1, product.id);

            stmt.executeUpdate();
            this.updateStillImage(product);
        }
    }
    private void saveProductImage(Product product) throws SQLException, NotFoundException, IOException {
        if (product.imageData == null || product.imageData.length == 0) {
            this.deleteProductImage(product);
            return;
        }

        final boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);

        try {
            try (PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_IMAGE)) {
                OrdImage ordImage = this.getImageForUpdate(product);
                ordImage.loadDataFromByteArray(product.imageData);

                final OraclePreparedStatement oracleStmt = (OraclePreparedStatement)stmt;
                oracleStmt.setORAData(1, ordImage);
                stmt.setInt(2, product.id);
                stmt.executeUpdate();
            }

            connection.commit();
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private Product productFromResultSet(ResultSet resultSet) throws SQLException, IOException {
        Product product = new Product(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("category_id"),
                resultSet.getFloat("price")
        );

        try {
            resultSet.findColumn("image");

            product.setImageFromOrdImage((OrdImage) ((OracleResultSet)resultSet).getORAData("image", OrdImage.getORADataFactory()));
        } catch (SQLException e) {
            // do nothing
        }

        try {
//            resultSet.findColumn("geometry");
            resultSet.findColumn("geometry_meta_type");
            resultSet.findColumn("geometry_meta_radius");
            resultSet.findColumn("geometry_meta_width");
            resultSet.findColumn("geometry_meta_height");

            product.geometry.type = resultSet.getString("geometry_meta_type");
            product.geometry.radius = resultSet.getDouble("geometry_meta_radius");
            product.geometry.width  = resultSet.getDouble("geometry_meta_width");
            product.geometry.height = resultSet.getDouble("geometry_meta_height");
        } catch (SQLException e) {
            // do nothing
        }


        return product;
    }

    public Product getByID(int id) throws SQLException, NotFoundException, IOException {
        final String SQL_SELECT_BY_ID = "SELECT id, name, category_id, price, image, geometry, geometry_meta_type, geometry_meta_radius, geometry_meta_width, geometry_meta_height FROM products WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return this.productFromResultSet(resultSet);
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }

    public ArrayList<Tuple<Product, Double>> getSimilar(Product product, int top_n) throws SQLException, IOException, NotFoundException {
        final String SQL_SIMILAR_IMAGE = "SELECT dst.id, dst.name, dst.price, dst.category_id, SI_ScoreByFtrList(new SI_FeatureList(src.image_ac,?,src.image_ch,?,src.image_pc,?,src.image_tx,?),dst.image_si) AS similarity FROM products src, products dst WHERE (src.id = ?) AND (src.id <> dst.id) ORDER BY similarity ASC";

        double weightAC = 0.3;
        double weightCH = 0.3;
        double weightPC = 0.1;
        double weightTX = 0.3;
        try(PreparedStatement stmt = connection.prepareStatement(SQL_SIMILAR_IMAGE)) {
            stmt.setDouble(1, weightAC);
            stmt.setDouble(2, weightCH);
            stmt.setDouble(3, weightPC);
            stmt.setDouble(4, weightTX);
            stmt.setInt(5, product.id);

            ArrayList<Tuple<Product, Double>> products = new ArrayList<Tuple<Product, Double>>();
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                products.add(new Tuple<Product, Double>(this.productFromResultSet(resultSet), resultSet.getDouble("similarity")));
            }
            if (resultSet.getRow() == 0) {
                throw new NotFoundException();
            }
            return products;
        }
    }

    public void rotateImage(Product product, int angle) throws SQLException, NotFoundException {
        final String SQL_ROTATE_IMAGE = "" +
                "DECLARE\n" +
                "    obj ORDSYS.ORDImage;\n" +
                "BEGIN\n" +
                "  SELECT p.image INTO obj FROM products p WHERE p.id = ? FOR UPDATE;\n" +
                "    obj.process('rotate " + angle + "');\n" +
                "    UPDATE products SET image = obj\n" +
                "    WHERE id = ?;\n" +
                "   COMMIT;\n" +
                "END;";
        try(CallableStatement stmt = connection.prepareCall(SQL_ROTATE_IMAGE)) {
            stmt.setInt(1, product.id);
            stmt.setInt(2, product.id);

            try {
                stmt.execute();
            } catch (SQLException sqlException) {
                if (sqlException.getErrorCode() == 01403) {
                    throw new NotFoundException();
                }
                throw sqlException;
            }
        }
    }

    public ArrayList<Product> getAll() throws SQLException, IOException {
        return this.getAll("id", "asc", 10, 0);
    }
    public ArrayList<Product> getAll(String orderField, String orderDir, int limit, int offset) throws SQLException, IOException {
        // TODO: sanitize
        final String SQL_SELECT_ALL = "SELECT id, name, category_id, price FROM products ORDER BY " + orderField + " " + orderDir + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            try (ResultSet resultSet = stmt.executeQuery()) {
                ArrayList<Product> products = new ArrayList<Product>();

                while (resultSet.next()) {
                    products.add(this.productFromResultSet(resultSet));
                }

                return products;
            }
        }
    }

    public int countAll() throws SQLException, NotFoundException {
        final String SQL_COUNT_ALL = "SELECT COUNT(id) FROM products";
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

    public int countAllUnits() throws SQLException, NotFoundException {
        final String SQL_COUNT_ALL = "SELECT COUNT(id) FROM product_units";
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























    public void saveUnit(ProductUnit unit) throws Exception {
        Product product = this.getByID(unit.productID);
        unit.geometry.type = product.geometry.type;
        unit.geometry.radius = product.geometry.radius;
        unit.geometry.width = product.geometry.width;
        unit.geometry.height = product.geometry.height;

        if (unit.id == 0) {
            this.createUnit(unit);
            return;
        }

        this.updateUnit(unit);
    }

    public void createUnit(ProductUnit unit) throws Exception {
        final String SQL_INSERT_NEW = "BEGIN INSERT INTO product_units(product_id, checked_in, checked_out, geometry, geometry_meta_x, geometry_meta_y) VALUES(?, ?, ?, ?, ?, ?) returning id into ?; END;";
        try(CallableStatement stmt = connection.prepareCall(SQL_INSERT_NEW)) {
            stmt.setInt(1, unit.productID);
            stmt.setDate(2, unit.checkedIn);
            stmt.setDate(3, unit.checkedOut);

            if (unit.geometry == null) {
                throw new Exception("Geometry must be set");
            } else {
                Struct obj = JGeometry.storeJS(this.connection, unit.geometry.toJGeometry());
                stmt.setObject(4, obj);
            }

            stmt.setDouble(5, unit.geometry.x);
            stmt.setDouble(6, unit.geometry.y);

            stmt.registerOutParameter(7, OracleTypes.INTEGER);

            stmt.execute();
            unit.id = stmt.getInt(7);
        }
    }

    public void updateUnit(ProductUnit unit) throws Exception {
        final String SQL_UPDATE_DATA = "UPDATE product_units SET product_id = ?, checked_in = ?, checked_out = ?, geometry = ?, geometry_meta_x = ?, geometry_meta_y = ? WHERE id = ?";
        // insert failed, try update
        try(PreparedStatement stmt = connection.prepareStatement(SQL_UPDATE_DATA)) {
            stmt.setInt(1, unit.productID);
            stmt.setDate(2, unit.checkedIn);
            stmt.setDate(3, unit.checkedOut);

            if (unit.geometry.toJGeometry() == null) {
                throw new Exception("Geometry must be set");
            } else {
                Struct obj = JGeometry.storeJS(this.connection, unit.geometry.toJGeometry());
                stmt.setObject(4, obj);
            }

            stmt.setDouble(5, unit.geometry.x);
            stmt.setDouble(6, unit.geometry.y);

            stmt.setInt(7, unit.id);

            stmt.executeUpdate();
        }
    }

    public void deleteUnitByID(int id) throws SQLException {
        final String SQL_DELETE_BY_ID = "DELETE FROM product_units WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_DELETE_BY_ID)) {
            stmt.setInt(1, id);

            stmt.execute();
        }
    }

    public ArrayList<ProductUnit> getAllUnits() throws SQLException, IOException {
        return this.getAllUnits("id", "asc", 10, 0);
    }
    public ArrayList<ProductUnit> getAllUnits(String orderField, String orderDir, int limit, int offset) throws SQLException, IOException {
        // TODO: sanitize
        final String SQL_SELECT_ALL = "SELECT id, product_id, checked_in, checked_out FROM product_units ORDER BY " + orderField + " " + orderDir + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_ALL)) {
            stmt.setInt(1, offset);
            stmt.setInt(2, limit);

            try (ResultSet resultSet = stmt.executeQuery()) {
                ArrayList<ProductUnit> products = new ArrayList<ProductUnit>();

                while (resultSet.next()) {
                    ProductUnit pu = new ProductUnit();
                    pu.id = resultSet.getInt("id");
                    pu.productID = resultSet.getInt("product_id");
                    pu.checkedIn = resultSet.getDate("checked_in");
                    pu.checkedOut = resultSet.getDate("checked_out");

                    products.add(pu);
                }

                return products;
            }
        }
    }

    public ProductUnit getUnitByID(int id) throws SQLException, NotFoundException, IOException {
        final String SQL_SELECT_BY_ID = "SELECT u.id, u.product_id, u.checked_in, u.checked_out, u.geometry, u.geometry_meta_x, u.geometry_meta_y, p.geometry_meta_type, p.geometry_meta_width, p.geometry_meta_height, p.geometry_meta_radius FROM product_units u, products p WHERE u.id = ? AND u.product_id = p.id";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    ProductUnit pu = new ProductUnit();
                    pu.id = resultSet.getInt("id");
                    pu.productID = resultSet.getInt("product_id");
                    pu.checkedIn = resultSet.getDate("checked_in");
                    pu.checkedOut = resultSet.getDate("checked_out");

                    pu.geometry.x = resultSet.getDouble("geometry_meta_x");
                    pu.geometry.y = resultSet.getDouble("geometry_meta_y");
                    pu.geometry.type = resultSet.getString("geometry_meta_type");
                    pu.geometry.radius = resultSet.getDouble("geometry_meta_radius");
                    pu.geometry.width  = resultSet.getDouble("geometry_meta_width");
                    pu.geometry.height = resultSet.getDouble("geometry_meta_height");
                    return pu;
                } else {
                    throw new NotFoundException();
                }
            }
        }
    }


    public double getWarehouseUsedArea() throws SQLException {
        final String SQL_TOTAL_AREA_USED_BY_UNITS = "SELECT SUM(SDO_GEOM.SDO_AREA(u.geometry, 1)) total_area FROM product_units u WHERE u.checked_in <= CURRENT_DATE AND (u.checked_out >= CURRENT_DATE OR u.checked_out IS NULL)";
        try(PreparedStatement stmt = connection.prepareStatement(SQL_TOTAL_AREA_USED_BY_UNITS)) {
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getDouble("total_area");
        }
    }

    public HashMap<String, Object> getProductUsingMostSpace() throws SQLException {
        final String SQL_MOST_SPACED_USING_PRODUCT = "SELECT t1.* FROM (\n" +
                "    SELECT p.id as product_id, SUM(SDO_GEOM.SDO_AREA(u.geometry, 1)) as area FROM products p, product_units u WHERE p.id = u.product_id AND u.checked_in <= CURRENT_DATE AND (u.checked_out >= CURRENT_DATE OR u.checked_out IS NULL) GROUP BY p.id ORDER BY area DESC\n" +
                ") t1 WHERE rownum = 1";

        try(PreparedStatement stmt = connection.prepareStatement(SQL_MOST_SPACED_USING_PRODUCT)) {
            ResultSet rs = stmt.executeQuery();
            rs.next();

            Integer product_id = rs.getInt("product_id");
            Double area = rs.getDouble("area");

            HashMap<String, Object> r = new HashMap<String, Object>();
            r.put("product_id", product_id);
            r.put("area", area);
            return r;
        }
    }

    public ArrayList<Integer> getOverlappingUnits(int id) throws SQLException, NotFoundException {
        final String SQL_OVERLAPPING_UNITS = "SELECT u1.id as my_id, u2.id as overlapping_id FROM product_units u1, product_units u2 WHERE u1.id = ? AND u1.id <> u2.id AND SDO_FILTER(u1.geometry, u2.geometry) = 'TRUE'";
        try(PreparedStatement stmt = connection.prepareStatement(SQL_OVERLAPPING_UNITS)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            ArrayList<Integer> ids = new ArrayList<Integer>();
            while (rs.next()) {
                ids.add(rs.getInt("overlapping_id"));
            }
            return ids;
        }
    }

    public Integer getClosestNeighbour(int id) throws Exception {
        final String SQL_GET_CLOSES_NEIGHBOUR = "SELECT u2.id, SDO_GEOM.SDO_DISTANCE(u1.geometry, u2.geometry, 1) distance FROM product_units u1, product_units u2 WHERE u1.id = ? AND u1.id <> u2.id ORDER BY distance ASC FETCH NEXT 1 ROWS ONLY";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_CLOSES_NEIGHBOUR)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }
            throw new NotFoundException();
        }
    }

    public ArrayList<Tuple<Integer, String>> getRelationToOthers(int id) throws Exception {
        final String SQL_GET_RELATION = "SELECT u1.id, u2.id as theotherid, SDO_GEOM.RELATE(u1.geometry, 'determine', u2.geometry, 0.1) relation FROM product_units u1, product_units u2 WHERE u1.id = ? AND u1.id <> u2.id";
        try(PreparedStatement stmt = connection.prepareStatement(SQL_GET_RELATION)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            ArrayList<Tuple<Integer, String>> res = new ArrayList<Tuple<Integer, String>>();
            while(rs.next()) {
                res.add(new Tuple<Integer, String>(rs.getInt("theotherid"), rs.getString("relation")));
            }

            return res;
        }
    }

    public ArrayList<Integer> getCloseSameProducts(int id, double distance) throws  Exception {
        final String SQL_GET_CLOSE_SAME_PRODUCTS = "SELECT u1.id, u2.id as theotherid FROM product_units u1, product_units u2, user_sdo_geom_metadata m WHERE u1.id = ? AND u1.id <> u2.id AND m.table_name = 'PRODUCT_UNITS' AND m.column_name = 'GEOMETRY' AND SDO_GEOM.WITHIN_DISTANCE(u1.geometry, ?, u2.geometry) = 'TRUE'";
        try (PreparedStatement stmt = connection.prepareStatement(SQL_GET_CLOSE_SAME_PRODUCTS)) {
            stmt.setInt(1, id);
            stmt.setDouble(2, distance);

            ResultSet rs = stmt.executeQuery();
            ArrayList<Integer> arr = new ArrayList<Integer>();
            while(rs.next()) {
                arr.add(rs.getInt("theotherid"));
            }

            return arr;
        }
    }
}
