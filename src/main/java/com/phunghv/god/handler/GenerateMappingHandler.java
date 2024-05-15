package com.phunghv.god.handler;

import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class GenerateMappingHandler extends BaseGodHandler {

    public GenerateMappingHandler() {

    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (file.isWritable()) {
            PsiClass psiClass = OverrideImplementUtil.getContextClass(project, editor, file, false);
            if (null != psiClass) {
                processClass(psiClass);

                UndoUtil.markPsiFileForUndo(file);
            }
        }
    }

    @Override
    protected void processClass(@NotNull PsiClass psiClass) {

    }

    private void updateJacksonAnnotations(PsiClass psiClass) {

    }

}
