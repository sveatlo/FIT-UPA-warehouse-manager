package io.fit20.wmanager.misc;

import io.fit20.wmanager.responses.InternalServerError;
import io.sentry.Sentry;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import static spark.Spark.exception;

public class FIT20ExceptionHandler implements ExceptionHandler {
    public void handle(Exception exception, Request request, Response response) {
        Sentry.capture(exception);

        new InternalServerError(exception).send(response);
    }
}
