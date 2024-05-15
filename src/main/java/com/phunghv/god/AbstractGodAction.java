package com.phunghv.god;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGodAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final Project project = event.getProject();
        if (project == null) {
            return;
        }

        final PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        psiDocumentManager.commitAllDocuments();

        final DataContext dataContext = event.getDataContext();
        final Editor editor = CommonDataKeys.EDITOR.getData(dataContext);

        if (null != editor) {
            final PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
            if (null != psiFile) {
                final PsiClass targetClass = getTargetClass(editor, psiFile);
                if (null != targetClass) {
                    process(project, psiFile, targetClass);
                }
            }
        } else {
            final VirtualFile[] files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(dataContext);
            if (null != files) {
                for (VirtualFile file : files) {
                    if (file.isDirectory()) {
                        processDirectory(project, file);
                    } else {
                        processFile(project, file);
                    }
                }
            }
        }
    }

    private void processDirectory(@NotNull final Project project, @NotNull VirtualFile vFile) {
        VfsUtilCore.visitChildrenRecursively(vFile, new VirtualFileVisitor<Void>() {
            @Override
            public boolean visitFile(@NotNull VirtualFile file) {
                if (!file.isDirectory()) {
                    processFile(project, file);
                }
                return true;
            }
        });
    }

    @Nullable
    private static PsiClass getTargetClass(Editor editor, PsiFile file) {
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = file.findElementAt(offset);
        if (element == null) {
            return null;
        }
        final PsiClass target = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        return target instanceof SyntheticElement ? null : target;
    }

    private void processFile(Project project, VirtualFile file) {
        if (JavaFileType.INSTANCE.equals(file.getFileType())) {
            final PsiManager psiManager = PsiManager.getInstance(project);
            PsiJavaFile psiFile = (PsiJavaFile) psiManager.findFile(file);
            if (psiFile != null) {
                process(project, psiFile);
            }
        }
    }

    protected void process(@NotNull final Project project, @NotNull final PsiJavaFile psiJavaFile) {

    }

    protected void process(@NotNull final Project project, @NotNull final PsiFile psiFile, @NotNull final PsiClass psiClass) {

    }
}
