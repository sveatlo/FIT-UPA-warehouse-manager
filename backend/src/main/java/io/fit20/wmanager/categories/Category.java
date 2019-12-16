package io.fit20.wmanager.categories;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Value;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Value
@Builder
public class Category {
    public int id;
    public String name;
    public Integer parent;

    public static Category fromJSON(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Category.class);
    }

    public Category() {}
    public Category(int id, String name, int parent) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        if (this.parent == 0) {
            this.parent = null;
        }
    }
}
