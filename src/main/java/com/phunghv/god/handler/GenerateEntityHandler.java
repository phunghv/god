package com.phunghv.god.handler;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;
import com.phunghv.god.toogle_text.StringConversionFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class GenerateEntityHandler extends BaseGodHandler {
    private static final String TABLE_CLASS = "jakarta.persistence.Table";
    private static final String ENTITY_CLASS = "jakarta.persistence.Entity";
    private static final String COLUMN_CLASS = "jakarta.persistence.Column";

    public GenerateEntityHandler() {

    }

    @Override
    protected void processClass(@NotNull PsiClass psiClass) {
        updateJacksonAnnotations(psiClass);
        var tableName = StringConversionFactory.convertSnakeCase(psiClass.getName()) + "s";

        addAnnotation(psiClass, TABLE_CLASS, "name = \"" + tableName + "\"");
        addAnnotation(psiClass, ENTITY_CLASS);
        addAnnotation(psiClass, "lombok.Setter");
        addAnnotation(psiClass, "lombok.Getter");
    }

    private void updateJacksonAnnotations(PsiClass psiClass) {
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
                addAnnotation(psiField, psiField, COLUMN_CLASS, "name = \"" + propertyName + "\"");
            }
        }
    }

}
