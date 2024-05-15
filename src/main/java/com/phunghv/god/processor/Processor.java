package com.phunghv.god.processor;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Processor {
    @NotNull
    String @NotNull [] getSupportedAnnotationClasses();

    @NotNull
    Class<? extends PsiElement> getSupportedClass();

    default boolean isSupportedAnnotationFQN(String annotationFQN) {
        return ContainerUtil.exists(getSupportedAnnotationClasses(), annotationFQN::equals);
    }

    default boolean isSupportedClass(Class<? extends PsiElement> someClass) {
        return getSupportedClass().equals(someClass);
    }

    @NotNull
    Collection<String> verifyAnnotation(@NotNull PsiAnnotation psiAnnotation);

    @NotNull
    default List<? super PsiElement> process(@NotNull PsiClass psiClass) {
        return process(psiClass, null);
    }

    @NotNull
    default List<? super PsiElement> process(@NotNull PsiClass psiClass, @Nullable String nameHint) {
        return Collections.emptyList();
    }

    LombokPsiElementUsage checkFieldUsage(@NotNull PsiField psiField, @NotNull PsiAnnotation psiAnnotation);
}
