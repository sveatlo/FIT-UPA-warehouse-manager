package io.fit20.wmanager.index;

import io.fit20.wmanager.misc.BaseController;
import spark.*;

import java.sql.Connection;

public class IndexController extends BaseController {
    public IndexController(Connection conn) {
        super(conn);
    }

    public Route serveIndexPage = (Request request, Response response) -> {
        return new IndexResponse().send(response);
    };
}