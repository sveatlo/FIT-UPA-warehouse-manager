package io.fit20.wmanager;

import static spark.Spark.*;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import io.fit20.wmanager.categories.CategoriesController;
import io.fit20.wmanager.products.ProductsController;
import io.sentry.Sentry;
import oracle.jdbc.pool.OracleDataSource;
import io.fit20.wmanager.index.*;
import io.fit20.wmanager.misc.*;
import io.fit20.wmanager.responses.NotFound;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;


public class Application {
    public static void main(String[] args) throws Exception {
        // add sentry
        String dsn = System.getenv("SENTRY_DSN");
        if (dsn == "") {
            dsn = "https://d0cef951e04144008f48e983388c7843@sentry.io/1855122";
        }
        Sentry.init(dsn);

        // set up DB
        // fit gort:
        //// url: "jdbc:oracle:thin:@//gort.fit.vutbr.cz:1521/orclpdb"
        //// user: "xhanze10"
        //// password: "XGvFJtaP"
        // localhost:
        //// url: "jdbc:oracle:thin:@localhost:1521:ORCLCDB"
        //// user: "sys as sysdba"
        //// password: "Oradoc_db1"
        String dbURL = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPassword = System.getenv("DB_PASSWORD");
        if (dbURL == "" || dbUser == "" || dbPassword == "") {
            throw new Exception("You have to configure the DB properly using env variables DB_URL, DB_USER, DB_PASSWORD");
        }
        OracleDataSource ods = new OracleDataSource();
        ods.setURL(dbURL);
        ods.setUser(dbUser);
        ods.setPassword(dbPassword);
        Connection conn = ods.getConnection();


        String scriptPath = System.getenv("INITAL_SCRIPT_PATH");
        if (scriptPath != null && scriptPath != "") {
            File file = new File(scriptPath);
            String content = Files.toString(file, Charsets.UTF_8);

            try (PreparedStatement stmt = conn.prepareStatement(content)) {
                stmt.executeQuery();
            }
        }


        // configure Spark
        port(1522);
        exception(Exception.class, new FIT20ExceptionHandler());

        // set up before-filters (called before each get/post)
        Application.enableCORS("*", "GET,POST,DELETE", "");
        before("*", Filters.addTrailingSlashes);

        // set up controllers

        IndexController indexController = new IndexController(conn);
        CategoriesController categoriesController = new CategoriesController(conn);
        ProductsController productsController = new ProductsController(conn);

        // set up routes

        get("/",  indexController.serveIndexPage);

        post("/categories/", categoriesController.createCategory);
        get("/categories/", categoriesController.getCategories);
        get("/categories/:id/", categoriesController.getCategory);
        delete("/categories/:id/", categoriesController.deleteCategory);

        post("/products/", productsController.createProduct);
        get("/products/", productsController.getProducts);
        get("/products/:id/", productsController.getProduct);
        get("/products/:id/footprint/", productsController.getProductFootprint);
        get("/products/:id/image/", productsController.getProductImage);
        post("/products/:id/image/rotate/:angle/", productsController.rotateProductImage);
        get("/products/:id/similar/", productsController.getSimilarProducts);
        delete("/products/:id/", productsController.deleteProduct);


        post("/product_units/", productsController.createUnit);
        get("/product_units/", productsController.getUnits);
        delete("/product_units/:id/", productsController.deleteUnit);
        get("/product_units/:id/overlapping/", "*/*",  productsController.getOverlappingUnits);
        get("/warehouse/most_used_space/", productsController.getProductUsingMostSpace);
//        get("/warehouse/image", productsController.getWarehouseMap);
        get("/warehouse/total_used_area/", productsController.getWarehouseUsedArea);

        get("*", (req, res) -> new NotFound("No such route").send(res));
        post("*", (req, res) -> new NotFound("No such route").send(res));
        put("*", (req, res) -> new NotFound("No such route").send(res));
        delete("*", (req, res) -> new NotFound("No such route").send(res));
    }

    private static void enableCORS(final String origin, final String methods, final String headers) {
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
            response.header("Access-Control-Allow-Headers", headers);

            if (request.requestMethod() == "OPTIONS") {
                String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
                if (accessControlRequestHeaders != null) {
                    response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
                }

                String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
                if (accessControlRequestMethod != null) {
                    response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
                }

                halt(200, "OK");
            }
        });
    }
}
