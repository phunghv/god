package com.phunghv.god.maven;

import com.phunghv.god.dto.MavenProperty;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class MavenParser {
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("<(.*?)>(.*?)</(.*?)>");

    private static final Pattern GROUP_ID_PATTERN = Pattern.compile("<groupId>(.*?)</groupId>");
    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("<artifactId>(.*?)</artifactId>");
    private static final Pattern VERSION_ID_PATTERN = Pattern.compile("<version>(.*?)</version>");

    public boolean startPropertyTag(String line) {
        return StringUtils.equalsIgnoreCase(line, "<properties>");
    }

    public boolean endPropertyTag(String line) {
        return StringUtils.equalsIgnoreCase(line, "</properties>");
    }

    public MavenProperty parseProperty(@NotNull String line) {
        var match = PROPERTY_PATTERN.matcher(line);
        if (match.find()) {
            return new MavenProperty(match.group(1), match.group(2));
        }
        return null;
    }

    public boolean startDependencyTag(String line) {
        return StringUtils.equalsIgnoreCase(line, "<dependency>") || StringUtils.equalsIgnoreCase(line, "<plugin>");
    }

    public boolean endDependencyTag(String line) {
        return StringUtils.equalsIgnoreCase(line, "</dependency>") || StringUtils.equalsIgnoreCase(line, "</plugin>");
    }

    public String parseGroupId(String line) {
        return parseWithPattern(line, GROUP_ID_PATTERN);
    }

    public String parseArtifactId(String line) {
        return parseWithPattern(line, ARTIFACT_ID_PATTERN);
    }

    public String parseVersionId(String line) {
        return parseWithPattern(line, VERSION_ID_PATTERN);
    }

    private String parseWithPattern(String line, Pattern pattern) {
        var match = pattern.matcher(line);
        if (match.find()) {
            return match.group(1);
        }
        return null;
    }
}
