package io.fit20.wmanager.responses;

public class InternalServerError extends Base {
    protected int statusCode = 500;
    public String Code = "INTERNAL_ERROR";
    public String Message = "Something happened... something wrong...";
    public Object Data;

    public InternalServerError(Object error) {
        this.Data = error;
    };
}
