package com.phunghv.god.handler;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiUtil;
import com.phunghv.god.toogle_text.StringConversionFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class GenerateEntityHandler extends BaseGodHandler {
    private static final String V3_TABLE_CLASS = "jakarta.persistence.Table";
    private static final String V3_ENTITY_CLASS = "jakarta.persistence.Entity";
    private static final String V3_COLUMN_CLASS = "jakarta.persistence.Column";


    private static final String V2_TABLE_CLASS = "javax.persistence.Table";
    private static final String V2_ENTITY_CLASS = "javax.persistence.Entity";
    private static final String V2_COLUMN_CLASS = "javax.persistence.Column";


    public GenerateEntityHandler() {

    }


    @Override
    protected void processClass(@NotNull PsiClass psiClass) {

    }

    @Override
    protected void processClass(@NotNull PsiClass psiClass, Project project) {
        var manager = PsiManager.getInstance(project);
        var clazz = ClassUtil.findPsiClass(manager, V3_TABLE_CLASS);
        boolean jakartaVersion = clazz != null;

        updateJacksonAnnotations(psiClass, jakartaVersion);
        var tableName = StringConversionFactory.convertSnakeCase(psiClass.getName()) + "s";

        addAnnotation(psiClass, jakartaVersion ? V3_TABLE_CLASS : V2_TABLE_CLASS, "name = \"" + tableName + "\"");
        addAnnotation(psiClass, jakartaVersion ? V3_ENTITY_CLASS : V2_ENTITY_CLASS);
        addAnnotation(psiClass, "lombok.Setter");
        addAnnotation(psiClass, "lombok.Getter");
    }

    private void updateJacksonAnnotations(PsiClass psiClass, boolean jakartaVersion) {
        for (PsiField psiField : psiClass.getFields()) {
            if (checkIgnoreField(psiField)) {
                continue;
            }
            PsiUtil.setModifierProperty(psiField, PsiModifier.PRIVATE, true);
            var fieldName = StringConversionFactory.convertCamelCase(psiField.getName());
            if (!StringUtils.equals(fieldName, psiField.getName())) {
                psiField.setName(fieldName);
            }

            var propertyName = StringConversionFactory.convertSnakeCase(psiField.getName());
            if (!StringUtils.equals(propertyName, fieldName)) {
                addAnnotation(psiField, psiField, jakartaVersion ? V3_COLUMN_CLASS : V2_COLUMN_CLASS, "name = \"" + propertyName + "\"");
            }
        }
    }

}
