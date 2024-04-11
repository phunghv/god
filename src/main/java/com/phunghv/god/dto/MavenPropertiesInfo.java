package com.phunghv.god.dto;

import java.util.HashMap;
import java.util.Map;

public class MavenPropertiesInfo {
    Integer startLineProperties = null;
    Integer endLineProperties = null;
    Map<String, MavenProperty> properties = new HashMap<>();

    public Integer getStartLineProperties() {
        return startLineProperties;
    }

    public void setStartLineProperties(int startLineProperties) {
        this.startLineProperties = startLineProperties;
    }

    public Integer getEndLineProperties() {
        return endLineProperties;
    }

    public void setEndLineProperties(int endLineProperties) {
        this.endLineProperties = endLineProperties;
    }

    public Map<String, MavenProperty> getProperties() {
        return properties;
    }
}
