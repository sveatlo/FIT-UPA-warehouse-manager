package io.fit20.wmanager.products;

import io.fit20.wmanager.misc.BaseController;
import io.fit20.wmanager.misc.NotFoundException;
import io.fit20.wmanager.responses.NotFound;
import io.fit20.wmanager.responses.Ok;
import oracle.jdbc.pooling.Tuple;
import oracle.ord.im.OrdImage;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductsController extends BaseController {
    private ProductsDAO dao;

    public ProductsController(Connection conn) {
        super(conn);

        this.dao = new ProductsDAO(conn);
    }

    public Route getProducts  = (Request req, Response res) -> {
        int limit = Integer.parseInt(req.queryParamOrDefault("limit", "10"));
        int offset = Integer.parseInt(req.queryParamOrDefault("offset", "0"));
        String orderField = req.queryParamOrDefault("orderField", "id");
        String orderDir = req.queryParamOrDefault("orderDir", "asc");

        ArrayList<Product> products = this.dao.getAll(orderField, orderDir, limit, offset);
        int totalCount = this.dao.countAll();
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("total", totalCount);

        return new Ok(products, metadata).send(res);
    };

    public Route createProduct = (Request req, Response res) -> {
        Product product = Product.fromJSON(req.body());

        this.dao.save(product);

        return new Ok(product).send(res);
    };

    public Route getProduct = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        Product product = null;
        try {
            product = this.dao.getByID(id);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }

        return new Ok(product).send(res);
    };

    public Route getProductFootprint = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        Product fakeProduct = new Product();
        fakeProduct.id = id;

        double footprint;
        try {
            footprint = this.dao.getFootprint(fakeProduct);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }


        return new Ok(footprint).send(res);
    };


    public Route getProductImage = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        Product fakeProduct = new Product();
        fakeProduct.id = id;

        try {
            OrdImage image = this.dao.getImage(fakeProduct);
            String mime = image.getMimeType();
            if (mime == null) {
                mime = "image/png";
            }
            res.header("Content-Type", mime);
            return image.getDataInByteArray();
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }
    };

    public Route rotateProductImage = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        int angle = Integer.parseInt(req.params(":angle"));
        Product fakeProduct = new Product();
        fakeProduct.id = id;

        try {
            this.dao.rotateImage(fakeProduct, angle);

            return new Ok("ok").send(res);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }
    };

    public Route getSimilarProducts = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        Product fakeProduct = new Product();
        fakeProduct.id = id;
        ArrayList<Tuple<Product, Double>> products;
        try {
            products = this.dao.getSimilar(fakeProduct, 10);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }

        return new Ok(products).send(res);
    };

    public Route deleteProduct = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        this.dao.deleteByID(id);

        return new Ok("ok").send(res);
    };



    public Route createUnit = (Request req, Response res) -> {
        ProductUnit productUnit = ProductUnit.fromJSON(req.body());

        this.dao.saveUnit(productUnit);

        return new Ok(productUnit).send(res);
    };


    public Route getUnit = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        ProductUnit unit = null;
        try {
            unit = this.dao.getUnitByID(id);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }

        return new Ok(unit).send(res);
    };

    public Route getUnits = (Request req, Response res) -> {
        int limit = Integer.parseInt(req.queryParamOrDefault("limit", "10"));
        int offset = Integer.parseInt(req.queryParamOrDefault("offset", "0"));
        String orderField = req.queryParamOrDefault("orderField", "id");
        String orderDir = req.queryParamOrDefault("orderDir", "asc");

        ArrayList<ProductUnit> products = this.dao.getAllUnits(orderField, orderDir, limit, offset);
        int totalCount = this.dao.countAllUnits();
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("total", totalCount);

        return new Ok(products, metadata).send(res);
    };

    public Route deleteUnit = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        this.dao.deleteByID(id);

        return new Ok("ok").send(res);
    };


    public Route getWarehouseMap = (Request req, Response res) -> {
        return null;
    };

    public Route getWarehouseUsedArea = (Request req, Response res) -> {
        double area = this.dao.getWarehouseUsedArea();
        return new Ok(area).send(res);
    };

    public Route getProductUsingMostSpace = (Request req, Response res) -> {
        HashMap<String, Object> r = this.dao.getProductUsingMostSpace();
        return new Ok(r).send(res);
    };

    public Route getOverlappingUnits = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        ArrayList<Integer> ids = this.dao.getOverlappingUnits(id);
        return new Ok(ids).send(res);
    };
}
