package com.phunghv.god;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.phunghv.god.handler.GenerateEntityHandler;

public class GenerateEntityAction extends BaseGenerateAction {
    protected GenerateEntityAction() {
        super(new GenerateEntityHandler());
    }
}
