package io.fit20.wmanager.categories;

import io.fit20.wmanager.misc.BaseController;
import io.fit20.wmanager.misc.NotFoundException;
import io.fit20.wmanager.products.Product;
import io.fit20.wmanager.responses.NotFound;
import io.fit20.wmanager.responses.Ok;
import spark.Request;
import spark.Response;
import spark.Route;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;

public class CategoriesController extends BaseController {
    private CategoriesDAO dao;

    public CategoriesController(Connection conn) {
        super(conn);

        this.dao = new CategoriesDAO(conn);
    }

    public Route getCategories  = (Request req, Response res) -> {
        int limit = Integer.parseInt(req.queryParamOrDefault("limit", "10"));
        int offset = Integer.parseInt(req.queryParamOrDefault("offset", "0"));
        String orderField = req.queryParamOrDefault("orderField", "id");
        String orderDir = req.queryParamOrDefault("orderDir", "asc");

        ArrayList<Category> categories = this.dao.getAll(orderField, orderDir, limit, offset);
        int totalCount = this.dao.countAll();
        HashMap<String, Object> metadata = new HashMap<String, Object>();
        metadata.put("total", totalCount);

        return new Ok(categories, metadata).send(res);
    };

    public Route createCategory = (Request req, Response res) -> {
        Category cat = Category.fromJSON(req.body());

        this.dao.save(cat);

        return new Ok(cat).send(res);
    };

    public Route getCategory = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        Category category = null;
        try {
            category = this.dao.getByID(id);
        } catch (NotFoundException e) {
            return new NotFound().send(res);
        }

        return new Ok(category).send(res);
    };

    public Route deleteCategory = (Request req, Response res) -> {
        int id = Integer.parseInt(req.params(":id"));
        this.dao.deleteByID(id);

        return new Ok("ok").send(res);
    };
}