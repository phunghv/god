package com.phunghv.god.handler;

import com.intellij.codeInsight.CodeInsightActionHandler;
import com.intellij.codeInsight.generation.OverrideImplementUtil;
import com.intellij.openapi.command.undo.UndoUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiUtil;
import com.phunghv.god.util.LombokProcessorUtil;
import com.phunghv.god.util.PsiAnnotationSearchUtil;
import com.phunghv.god.util.PsiAnnotationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BaseGodHandler implements CodeInsightActionHandler {
    protected static void processIntern(@NotNull Map<PsiField, PsiMethod> fieldMethodMap, @NotNull PsiClass psiClass, String annotationClassName) {
        if (fieldMethodMap.isEmpty()) {
            return;
        }

        final PsiMethod firstPropertyMethod = fieldMethodMap.values().iterator().next();

        final boolean useAnnotationOnClass = haveAllMethodsSameAccessLevel(fieldMethodMap.values()) && isNotAnnotatedWithOrSameAccessLevelAs(psiClass, firstPropertyMethod,
                annotationClassName);

        if (useAnnotationOnClass) {
            addAnnotation(psiClass, firstPropertyMethod, annotationClassName);
        }

        for (Map.Entry<PsiField, PsiMethod> fieldMethodEntry : fieldMethodMap.entrySet()) {
            final PsiField propertyField = fieldMethodEntry.getKey();
            final PsiMethod propertyMethod = fieldMethodEntry.getValue();

            if (null != propertyField) {
                boolean isStatic = propertyField.hasModifierProperty(PsiModifier.STATIC);
                if (isStatic || !useAnnotationOnClass) {
                    addAnnotation(propertyField, propertyMethod, annotationClassName);
                }

                // Move all annotations to field declaration
                for (PsiAnnotation psiMethodAnnotation : propertyMethod.getModifierList().getAnnotations()) {
                    psiClass.addBefore(psiMethodAnnotation, propertyField);
                }

                propertyMethod.delete();
            }
        }
    }

    private static boolean isNotAnnotatedWithOrSameAccessLevelAs(PsiClass psiClass, PsiMethod firstPropertyMethod, String annotationClassName) {
        final PsiAnnotation presentAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, annotationClassName);
        if (null != presentAnnotation) {

            final String presentAccessModifier = LombokProcessorUtil.getMethodModifier(presentAnnotation);
            final String currentAccessModifier = PsiUtil.getAccessModifier(PsiUtil.getAccessLevel(firstPropertyMethod.getModifierList()));

            return presentAccessModifier != null && presentAccessModifier.equals(currentAccessModifier);
        }
        return true;
    }

    private static boolean haveAllMethodsSameAccessLevel(Collection<PsiMethod> psiMethods) {
        final Set<Integer> accessLevelSet = new HashSet<>();
        for (PsiMethod psiMethod : psiMethods) {
            accessLevelSet.add(PsiUtil.getAccessLevel(psiMethod.getModifierList()));
        }
        return accessLevelSet.size() <= 1;
    }

    protected static void addAnnotation(@NotNull PsiModifierListOwner targetElement, @NotNull PsiModifierListOwner sourceElement, String annotationClassName) {
        addAnnotation(targetElement, sourceElement, annotationClassName, null);
    }

    protected static void addAnnotation(@NotNull PsiModifierListOwner targetElement, @NotNull PsiModifierListOwner sourceElement, String annotationClassName, String value) {
        final PsiAnnotation newPsiAnnotation = LombokProcessorUtil.createAnnotationWithAccessLevel(sourceElement, annotationClassName, value);

        addAnnotation(targetElement, newPsiAnnotation, annotationClassName);
    }

    protected static void addAnnotation(@NotNull PsiClass targetElement, String annotationClassName) {
        addAnnotation(targetElement, annotationClassName, null);
    }

    protected static void addAnnotation(@NotNull PsiClass targetElement, String annotationClassName, String value) {
        final PsiAnnotation newPsiAnnotation = PsiAnnotationUtil.createPsiAnnotation(targetElement, value, annotationClassName);

        addAnnotation(targetElement, newPsiAnnotation, annotationClassName);
    }

    private static void addAnnotation(@NotNull PsiModifierListOwner targetElement, @NotNull PsiAnnotation newPsiAnnotation, String annotationClassName) {
        final PsiAnnotation presentAnnotation = PsiAnnotationSearchUtil.findAnnotation(targetElement, annotationClassName);

        final Project project = targetElement.getProject();
        final JavaCodeStyleManager javaCodeStyleManager = JavaCodeStyleManager.getInstance(project);
        javaCodeStyleManager.shortenClassReferences(newPsiAnnotation);

        if (null == presentAnnotation) {
            PsiModifierList modifierList = targetElement.getModifierList();
            if (null != modifierList) {
                modifierList.addAfter(newPsiAnnotation, null);
            }
        } else {
            presentAnnotation.setDeclaredAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME,
                    newPsiAnnotation.findDeclaredAttributeValue(PsiAnnotation.DEFAULT_REFERENCED_METHOD_NAME));
        }
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (file.isWritable()) {
            PsiClass psiClass = OverrideImplementUtil.getContextClass(project, editor, file, false);
            if (null != psiClass) {
                processClass(psiClass, project);

                UndoUtil.markPsiFileForUndo(file);
            }
        }
    }

    protected boolean checkIgnoreField(PsiField psiField) {
        var modifiers = psiField.getModifierList();
        if (modifiers == null) {
            return false;
        }
        var ignoreFields = List.of(PsiModifier.FINAL, PsiModifier.STATIC, PsiModifier.TRANSIENT);
        for (var field : ignoreFields) {
            if (modifiers.hasModifierProperty(field)) {
                return true;
            }
        }
        return false;
    }

    protected abstract void processClass(@NotNull PsiClass psiClass);

    protected void processClass(@NotNull PsiClass psiClass, Project project) {
        processClass(psiClass);
    }

    protected void removeDefaultAnnotation(@NotNull PsiModifierListOwner targetElement, String annotationClassName) {
        final PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(targetElement, annotationClassName);
        if (null != psiAnnotation) {
            boolean hasOnlyDefaultValues = true;

            final PsiAnnotationParameterList psiAnnotationParameterList = psiAnnotation.getParameterList();
            for (PsiNameValuePair nameValuePair : psiAnnotationParameterList.getAttributes()) {
                if (null != psiAnnotation.findDeclaredAttributeValue(nameValuePair.getName())) {
                    hasOnlyDefaultValues = false;
                    break;
                }
            }

            if (hasOnlyDefaultValues) {
                psiAnnotation.delete();
            }
        }
    }

    @Nullable
    protected PsiMethod findPublicNonStaticMethod(@NotNull PsiClass psiClass, @NotNull String methodName, @NotNull PsiType returnType, PsiType... params) {
        final PsiMethod[] toStringMethods = psiClass.findMethodsByName(methodName, false);
        for (PsiMethod method : toStringMethods) {
            if (method.hasModifierProperty(PsiModifier.PUBLIC) && !method.hasModifierProperty(PsiModifier.STATIC) && returnType.equals(method.getReturnType())) {

                final PsiParameterList parameterList = method.getParameterList();
                final PsiParameter[] psiParameters = parameterList.getParameters();
                final int paramsCount = params.length;

                if (psiParameters.length == paramsCount) {
                    boolean allParametersFound = true;
                    for (int i = 0; i < paramsCount; i++) {

                        if (!psiParameters[i].getType().equals(params[i])) {
                            allParametersFound = false;
                            break;
                        }
                    }
                    if (allParametersFound) {
                        return method;
                    }
                }
            }
        }
        return null;
    }
}
