package com.phunghv.god.dto;

public class MavenDependencyInfo {
    String groupId;
    String artifactId;
    String version;
    Integer versionLineNumber = null;

    public MavenDependencyInfo(String groupId, String artifactId, String version, Integer versionLineNumber) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.versionLineNumber = versionLineNumber;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }


    public String getVersion() {
        return version;
    }


    public Integer getVersionLineNumber() {
        return versionLineNumber;
    }
}
