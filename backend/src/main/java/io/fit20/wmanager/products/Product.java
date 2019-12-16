package io.fit20.wmanager.products;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Value;
import oracle.ord.im.OrdImage;
import oracle.spatial.geometry.JGeometry;

import static oracle.spatial.geometry.JGeometry.GTYPE_POLYGON;

@Value
@Builder
@JsonNaming(PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy.class)
public class Product {

    public int id;
    public String name;
    public int categoryID;
    public float price;
    public byte[] imageData; //base64-encoded image data
    public FakeGeometry geometry;

    public static Product fromJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Product.class);
    }
    public Product() {
        this.geometry = new FakeGeometry();
    }
    public Product(int id, String name, int categoryID, float price) {
        this();
        this.id = id;
        this.name = name;
        this.categoryID = categoryID;
        this.price = price;
    }
    public Product(int id, String name, int categoryID, float price, byte[] imageData) throws IOException, SQLException {
        this(id, name, categoryID, price);

        this.imageData = Base64.getDecoder().decode(imageData);

    }
    public Product(int id, String name, int categoryID, float price, OrdImage image) throws IOException, SQLException {
        this(id, name, categoryID, price);
        if (image != null) {
            this.setImageFromOrdImage(image);
        }
    }

    @JsonIgnore
    public void setImageFromOrdImage(OrdImage image) throws IOException, SQLException {
        this.imageData = image.getDataInByteArray();
    }

    @JsonIgnore
    public JGeometry getJGeometry() throws Exception {
        if (this.geometry == null) {
            return null;
        }

        return this.geometry.toJGeometry();
    }
}