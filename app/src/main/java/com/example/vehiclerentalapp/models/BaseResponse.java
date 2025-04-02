package com.example.vehiclerentalapp.models;

import java.util.List;
import java.util.Map;

public class BaseResponse {
    private int code;
    private String status;
    private String message;
    private Map<String, Object> data;

    public BaseResponse(int code, String status, String message, Map<String, Object> data) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Map<String, Object> getNestedMap(String key) {
        if (data != null && data.containsKey(key) && data.get(key) instanceof Map) {
            return (Map<String, Object>) data.get(key);
        }
        return null;
    }

    public String getString(String key) {
        if (data != null && data.containsKey(key) && data.get(key) instanceof String) {
            return (String) data.get(key);
        }
        return null;
    }

    public Integer getInt(String key) {
        if (data != null && data.containsKey(key)) {
            Object value = data.get(key);
            if (value instanceof Double) {
                return ((Double) value).intValue();
            } else if (value instanceof Integer) {
                return (Integer) value;
            }
        }
        return null;
    }

    // New helper methods to extract from a specific map
    public String getStringFromMap(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key) && map.get(key) instanceof String) {
            return (String) map.get(key);
        }
        return null;
    }

    public Integer getIntFromMap(Map<String, Object> map, String key) {
        if (map != null && map.containsKey(key)) {
            Object value = map.get(key);
            if (value instanceof Double) {
                return ((Double) value).intValue();
            } else if (value instanceof Integer) {
                return (Integer) value;
            }
        }
        return null;
    }


}