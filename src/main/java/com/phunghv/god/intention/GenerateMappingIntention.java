package com.phunghv.god.intention;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@NonNls
public class GenerateMappingIntention extends PsiElementBaseIntentionAction implements IntentionAction {
    private static final String[] PREFIX_NAMES = {"map", "convert", "to"};

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        var method = findCurrentMethod(element);
        if (method == null || method.getBody() == null) {
            return;
        }
        var params = method.getParameterList().getParameters();
        var returnType = method.getReturnType();
        if (StringUtils.equalsIgnoreCase(returnType.getCanonicalText(), "void")) {
            if (params.length < 1) {
                return;
            }
            generateMapping(project, method, params[0], params[1]);
        } else {
            generateMappings(project, method, returnType, params[0]);
        }
    }

    private void generateMappings(@NotNull Project project, PsiMethod method, PsiType returnType, PsiParameter source) {
        var sourceName = source.getName();
        var targetType = PsiTypesUtil.getPsiClass(returnType);
        var targetName = StringUtils.uncapitalize(targetType.getName());
        if (sourceName.equals(targetName)) {
            targetName = "result";
        }
        var sourceType = PsiTypesUtil.getPsiClass(source.getType());
        generateMapping(project, method, targetType, targetName, sourceType, sourceName, true);
    }

    private void generateMapping(@NotNull Project project, PsiMethod method, PsiParameter target, PsiParameter source) {
        var targetType = PsiTypesUtil.getPsiClass(target.getType());
        var sourceType = PsiTypesUtil.getPsiClass(source.getType());
        var targetName = target.getName();
        var sourceName = source.getName();
        generateMapping(project, method, targetType, targetName, sourceType, sourceName, false);
    }

    private void generateMapping(@NotNull Project project, PsiMethod method, PsiClass targetType, String targetName, PsiClass sourceType, String sourceName, boolean init) {
        if (targetType == null || sourceType == null) {
            return;
        }
        var fields = targetType.getAllFields();
        Set<String> validMethodsOnSource = new HashSet<>();
        var methods = sourceType.getAllMethods();
        for (var m : methods) {
            if (m.getParameterList().getParametersCount() == 1) {
                continue;
            }
            validMethodsOnSource.add(m.getName());
        }
        var allFieldOnSources = sourceType.getAllFields();
        var fieldsOnSources = new HashSet<String>();
        Arrays.stream(allFieldOnSources).forEach(i -> fieldsOnSources.add(i.getName()));

        var elementFactory = JavaPsiFacade.getElementFactory(project);
        var block = elementFactory.createCodeBlock();
        if (init) {
            var initStatement = elementFactory.createStatementFromText(String.format("var %s = new %s();", targetName, targetType.getName()), method.getBody());
            block.add(initStatement);
        }
        for (var field : fields) {
            fieldsOnSources.remove(field.getName());
            var fieldName = StringUtils.capitalize(field.getName());
            var getter = "is" + fieldName;
            if (!validMethodsOnSource.contains(getter)) {
                getter = "get" + fieldName;
            }
            if (validMethodsOnSource.contains(getter)) {
                var element = elementFactory.createStatementFromText(String.format("%s.set%s(%s.%s());", targetName, fieldName, sourceName, getter), block);
                block.add(element);
            } else {
                var parserFacade = PsiParserFacade.getInstance(project);
                var space = " ".repeat(8);
                var lineBreakTab = parserFacade.createWhiteSpaceFromText("\n");
                if (block.getLastBodyElement() != null) {
                    block.addAfter(lineBreakTab, block.getLastBodyElement());
                } else {
                    block.add(lineBreakTab);
                }

                var element = elementFactory.createCommentFromText(String.format("//%s%s.set%s(%s.%s());", space, targetName, fieldName, sourceName, getter), block);
                if (block.getLastBodyElement() != null) {
                    block.addAfter(element, block.getLastBodyElement());
                } else {
                    block.add(element);
                }
            }
        }
        if (!fieldsOnSources.isEmpty()) {
            var parserFacade = PsiParserFacade.getInstance(project);
            var lineBreakTab = parserFacade.createWhiteSpaceFromText("\n");
            if (block.getLastBodyElement() != null) {
                block.addAfter(lineBreakTab, block.getLastBodyElement());
            } else {
                block.add(lineBreakTab);
            }

            var element = elementFactory.createCommentFromText(String.format("// Missing target fields: %s", String.join(", ", fieldsOnSources)), block);
            block.addAfter(element, block.getLastBodyElement());
        }
        if (init) {
            var returnStatement = elementFactory.createStatementFromText(String.format("return %s;", targetName), method.getBody());
            block.add(returnStatement);
        }
        method.getBody().replace(block);
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        var method = findCurrentMethod(element);
        if (method == null) {
            return false;
        }
        for (var name : PREFIX_NAMES) {
            if (StringUtils.containsIgnoreCase(method.getName(), name)) {
                return isEmptyMethod(method);
            }
        }
        return false;
    }

    private boolean isEmptyMethod(PsiMethod method) {
        if (method.getBody() == null || method.getBody().getStatements().length == 0) {
            return true;
        }
        return false;
    }

    private PsiMethod findCurrentMethod(PsiElement element) {
        var currentElement = element;

        for (int i = 0; i < 100_000; i++) {
            if (currentElement == null) {
                return null;
            }
            if (currentElement instanceof PsiMethod method) {
                return method;
            }
            currentElement = currentElement.getParent();
        }
        return null;
    }


    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return "Generate mapping";
    }

    @Override
    public @NotNull @IntentionName String getText() {
        return getFamilyName();
    }
}
