package io.fit20.wmanager.responses;

import spark.Response;

public class NotFound extends Base {
    protected int statusCode = 404;
    public String Code = "NOT_FOUND";
    public String Message;

    public NotFound() {
        this("The requested resource cannot be found.");
    }
    public NotFound(String message) {
        this.Message = message;
    }
}
