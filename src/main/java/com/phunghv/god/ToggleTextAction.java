package com.phunghv.god;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.phunghv.god.toogle_text.Conversion;
import com.phunghv.god.toogle_text.StringConversionFactory;
import org.jetbrains.annotations.NotNull;

public class ToggleTextAction extends AnAction {

    private static final String CONVERSION_SPACE_CASE = "space case";
    private static final String CONVERSION_KEBAB_CASE = "kebab-case";
    private static final String CONVERSION_UPPER_SNAKE_CASE = "SNAKE_CASE";
    private static final String CONVERSION_PASCAL_CASE = "CamelCase";
    private static final String CONVERSION_CAMEL_CASE = "camelCase";
    private static final String CONVERSION_PASCAL_CASE_SPACE = "Camel Case";
    private static final String CONVERSION_LOWER_SNAKE_CASE = "snake_case";
    private static final String[] CONVERSIONS = {CONVERSION_UPPER_SNAKE_CASE, CONVERSION_PASCAL_CASE, CONVERSION_PASCAL_CASE_SPACE, CONVERSION_SPACE_CASE, CONVERSION_CAMEL_CASE, CONVERSION_LOWER_SNAKE_CASE, CONVERSION_KEBAB_CASE};

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        var editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        final var document = editor.getDocument();
        var selected = editor.getSelectionModel();
        if (selected.getSelectionStart() == selected.getSelectionEnd()) {
            findAndConvertText(editor, selected);
        } else {
            var currentText = selected.getSelectedText();
            var startTextOffset = selected.getSelectionStart();
            var endTextOffset = selected.getSelectionEnd();
            replaceText(currentText, document, event.getProject(), startTextOffset, endTextOffset);
        }

    }

    private void findAndConvertText(Editor editor, SelectionModel selected) {
        var offset = editor.getCaretModel().getOffset();
        var document = editor.getDocument();
        var line = document.getLineNumber(offset);

        var currentStartOffset = selected.getSelectionStart();
        var currentEndOffset = selected.getSelectionEnd();
        var selectedStartIndex = currentStartOffset;
        var selectedEndIndex = currentEndOffset;
        boolean notfound = true;
        for (var i = currentStartOffset; i > document.getLineStartOffset(line); i--) {
            if (document.getText(new TextRange(i, i + 1)).equals("\"")) {
                selectedStartIndex = i + 1;
                notfound = false;
                break;
            }
        }
        if (notfound) {
            return;
        }
        notfound = true;
        for (var i = currentEndOffset; i < document.getLineEndOffset(line); i++) {
            if (document.getText(new TextRange(i, i + 1)).equals("\"")) {
                selectedEndIndex = i;
                notfound = false;
                break;
            }
        }
        if (notfound) {
            return;
        }
        var currentText = document.getText(new TextRange(selectedStartIndex, selectedEndIndex));
        replaceText(currentText, document, editor.getProject(), selectedStartIndex, selectedEndIndex);
    }

    private void replaceText(String currentText, Document document, Project project, int startTextOffset, int endTextOffset) {
//        var convertedText = Conversion.transform(currentText, false, true, false, true, true, true, true, CONVERSIONS);
        var convertedText = StringConversionFactory.getNext(currentText);
        WriteCommandAction.runWriteCommandAction(project, () -> document.replaceString(startTextOffset, endTextOffset, convertedText));
    }
}
