package com.phunghv.god.handler;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.util.PsiUtil;
import com.phunghv.god.toogle_text.StringConversionFactory;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class GenerateJacksonHandler extends BaseGodHandler {
    private static final String JSON_IGNORE_PROPERTIES_CLASS = "com.fasterxml.jackson.annotation.JsonIgnoreProperties";
    private static final String JSON_INCLUDE_CLASS = "com.fasterxml.jackson.annotation.JsonInclude";
    private static final String JSON_PROPERTY_CLASS = "com.fasterxml.jackson.annotation.JsonProperty";

    public GenerateJacksonHandler() {

    }

    @Override
    protected void processClass(@NotNull PsiClass psiClass) {
        updateJacksonAnnotations(psiClass);
        addAnnotation(psiClass, JSON_IGNORE_PROPERTIES_CLASS, "ignoreUnknown = true");
        addAnnotation(psiClass, JSON_INCLUDE_CLASS, "value = JsonInclude.Include.NON_NULL");
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
                addAnnotation(psiField, psiField, JSON_PROPERTY_CLASS, "\"" + propertyName + "\"");
            }
        }
    }
}
