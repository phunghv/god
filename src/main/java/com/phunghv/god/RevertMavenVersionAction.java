package com.phunghv.god;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.phunghv.god.dto.MavenDependencyInfo;
import com.phunghv.god.dto.MavenPropertiesInfo;
import com.phunghv.god.maven.MavenParser;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class RevertMavenVersionAction extends AnAction {
    private final MavenParser parser = new MavenParser();
    private static final String SUFFIX_VERSION = "";


    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project == null) {
            return;
        }
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        final var document = editor.getDocument();
        var offset = editor.getCaretModel().getOffset();
        var line = document.getLineNumber(offset);
        var mavenDependencyInfo = getCurrentMavenDependencyInfo(document, line);
        if (mavenDependencyInfo == null || !StringUtils.startsWith(mavenDependencyInfo.getVersion(), "${")) {
            return;
        }
        var variable = mavenDependencyInfo.getVersion().replace("${", "").replace("}", "");
        var propertiesInfo = parseCurrentMavenProperty(document);

        if (!propertiesInfo.getProperties().containsKey(variable)) {
            return;
        }
        var realVersion = propertiesInfo.getProperties().get(variable).getPropertyValue();


        var currentVersionLine = mavenDependencyInfo.getVersionLineNumber();
        var startVersionOffset = document.getLineStartOffset(currentVersionLine) + getPreviousOffset(document, currentVersionLine + 1);
        var endVersionOffset = document.getLineEndOffset(currentVersionLine);
        var versionText = String.format("<version>%s</version>", realVersion);
        WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(startVersionOffset, endVersionOffset, versionText));
    }

    private int getPreviousOffset(Document document, int currentLine) {
        for (int i = currentLine - 1; i > 0; i--) {
            var text = getText(document, i);
            if (StringUtils.isNotEmpty(text)) {
                return document.getLineEndOffset(i) - document.getLineStartOffset(i) - text.length();
            }
        }
        return 0;
    }

    private String buildPropertyDefinition(MavenDependencyInfo dependency) {
        var name = buildVariable(dependency);
        return String.format("<%s>%s</%s>", name, dependency.getVersion(), name);
    }

    private String buildVariable(MavenDependencyInfo dependency) {
        if (StringUtils.isBlank(SUFFIX_VERSION)) {
            return StringUtils.lowerCase(String.format("%s.version", dependency.getArtifactId()));
        }
        return StringUtils.lowerCase(String.format("%s.%s.version", dependency.getArtifactId(), SUFFIX_VERSION));
    }

    private MavenDependencyInfo getCurrentMavenDependencyInfo(Document document, int lineNumber) {
        String groupId = null;
        String artifactId = null;
        String version = null;
        Integer versionLineNumber = null;
        for (int i = lineNumber; i < document.getLineCount(); i++) {
            var line = getText(document, i);
            if (parser.endDependencyTag(line)) {
                break;
            }
            if (version == null) {
                version = parser.parseVersionId(line);
                versionLineNumber = i;
            }
            if (groupId == null) {
                groupId = parser.parseGroupId(line);
            }
            if (artifactId == null) {
                artifactId = parser.parseArtifactId(line);
            }
        }
        if (version != null && artifactId != null) {
            return new MavenDependencyInfo(groupId, artifactId, version, versionLineNumber);
        }
        for (int i = lineNumber; i > 0; i--) {
            var line = getText(document, i);
            if (parser.startDependencyTag(line)) {
                break;
            }
            if (version == null) {
                version = parser.parseVersionId(line);
                versionLineNumber = i;
            }
            if (groupId == null) {
                groupId = parser.parseGroupId(line);
            }
            if (artifactId == null) {
                artifactId = parser.parseArtifactId(line);
            }
        }
        if (version != null && artifactId != null) {
            return new MavenDependencyInfo(groupId, artifactId, version, versionLineNumber);
        }
        return null;
    }

    private MavenPropertiesInfo parseCurrentMavenProperty(Document document) {
        MavenPropertiesInfo info = new MavenPropertiesInfo();
        var totalLine = document.getLineCount();

        boolean startPropertyTag = false;
        for (int i = 0; i < totalLine; i++) {
            var line = getText(document, i);
            if (!startPropertyTag && parser.startPropertyTag(line)) {
                startPropertyTag = true;
                info.setStartLineProperties(i);
                continue;
            }

            if (!startPropertyTag || StringUtils.isBlank(line)) {
                continue;
            }
            var property = parser.parseProperty(line);
            if (property != null) {
                info.getProperties().put(property.getPropertyName(), property);
                continue;
            }

            if (parser.endPropertyTag(line)) {
                info.setEndLineProperties(i);
                return info;
            }
        }
        return info;
    }

    private String getText(Document document, int line) {
        var text = document.getText(new TextRange(document.getLineStartOffset(line), document.getLineEndOffset(line)));
        return StringUtils.trim(text);
    }
}
