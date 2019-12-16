package io.fit20.wmanager.responses;

import java.util.HashMap;

public class Ok extends Base {
    public boolean Success = true;
    public Object data = null;
    public HashMap<String, Object> metadata = null;

    public Ok(Object data) { this.data = data; }
    public Ok(Object data, HashMap<String, Object> metadata) { this.data = data; this.metadata = metadata; }
}
