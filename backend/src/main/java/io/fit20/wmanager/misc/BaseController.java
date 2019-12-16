package io.fit20.wmanager.misc;

import java.sql.Connection;

public class BaseController {
    protected Connection DB;
    public BaseController(Connection connection) {
        this.DB = connection;
    }
}
