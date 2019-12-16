package io.fit20.wmanager.responses;

import java.io.IOException;
import java.io.StringWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;
import spark.Response;


@Value
@Builder
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class Base {
    protected int statusCode = 200;

    public String send(Response response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, this);
            String resBody =  sw.toString();


            response.header("Content-Type", "application/json");
            response.status(this.statusCode);
            response.body(resBody);

            return resBody;
        } catch (IOException e) {
            throw new RuntimeException("IOEXception while mapping object (" + this + ") to JSON: " + e);
        }
    }
}
