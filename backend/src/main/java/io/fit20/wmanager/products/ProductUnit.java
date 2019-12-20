package io.fit20.wmanager.products;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;
import oracle.spatial.geometry.JGeometry;

import java.io.IOException;
import java.sql.Date;

@Value
@Builder
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class ProductUnit {
    public int id;
    public int productID;
    public Date checkedIn;
    public Date checkedOut;
    public FakeGeometry geometry;

    public static ProductUnit fromJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ProductUnit.class);
    }

    public ProductUnit() {
        this.geometry = new FakeGeometry();
    }
    public ProductUnit(int id, int productID, FakeGeometry geometry) {
        this.id = id;
        this.productID = productID;
        this.geometry = geometry;
    }
}
