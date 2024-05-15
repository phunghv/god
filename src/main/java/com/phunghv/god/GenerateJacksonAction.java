package com.phunghv.god;

import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.phunghv.god.handler.GenerateJacksonHandler;

public class GenerateJacksonAction extends BaseGenerateAction {
    protected GenerateJacksonAction() {
        super(new GenerateJacksonHandler());
    }
}
