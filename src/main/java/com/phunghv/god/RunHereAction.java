package com.phunghv.god;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalToolWindowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunHereAction extends AnAction {
    private static final Logger log = LoggerFactory.getLogger(RunHereAction.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var project = event.getProject();
        if (project == null) {
            return;
        }


        var psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        VirtualFile currentFile = null;
        if (psiElement != null && ClassUtils.isAssignable(psiElement.getClass(), PsiFileSystemItem.class)) {
            var file = (PsiFileSystemItem) psiElement;
            currentFile = file.getVirtualFile();
        }
        if (currentFile == null) {
            var editor = event.getData(CommonDataKeys.EDITOR);
            if (editor != null) {
                currentFile = editor.getVirtualFile();
            }
        }

        if (currentFile != null) {
            var pomFile = findPomFile(currentFile);
            if (pomFile == null) {
                log.warn("Không có POM thì chạy cái gì");
                return;
            }
            var projectDir = pomFile.getParent();
            createNewTerminal(projectDir, project);
        }
    }

    private void createNewTerminal(VirtualFile projectDir, Project project) {
        var terminalManager = TerminalToolWindowManager.getInstance(project);
        var state = new TerminalTabState();
        state.myTabName = projectDir.getName();
        state.myWorkingDirectory = projectDir.getCanonicalPath();
        terminalManager.createNewSession(terminalManager.getTerminalRunner(), state);
    }

    private VirtualFile findPomFile(VirtualFile file) {
        var targetDir = file;
        if (!file.isDirectory()) {
            targetDir = file.getParent();
        }
        if (targetDir == null) {
            return null;
        }
        var pomFile = findPomFileCurrentDir(targetDir);
        if (pomFile != null) {
            return pomFile;
        }
        if (targetDir.getParent() == null) {
            return null;
        }
        return findPomFile(targetDir.getParent());
    }

    private VirtualFile findPomFileCurrentDir(VirtualFile directory) {
        if (isPomFile(directory)) {
            return directory;
        }
        return findChildPomFile(directory);
    }

    private boolean isPomFile(VirtualFile element) {
        return StringUtils.equalsAnyIgnoreCase(element.getName(), "pom.xml");
    }

    private boolean isIgnoreFile(VirtualFile file) {
        return StringUtils.startsWith(file.getName(), ".") || !file.isDirectory();
    }

    private VirtualFile findChildPomFile(VirtualFile directory) {
        var children = directory.getChildren();
        for (var child : children) {
            if (isPomFile(child)) {
                return child;
            }
        }

        for (var child : children) {
            if (isIgnoreFile(child)) {
                continue;
            }
            var xmlFile = findChildPomFile(child);
            if (xmlFile != null) {
                return xmlFile;
            }
        }
        return null;
    }
}
