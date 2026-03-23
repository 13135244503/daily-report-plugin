package com.dailyreport.action;

import com.dailyreport.ui.TranslationDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class TranslateAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        TranslationDialog dialog = new TranslationDialog();
        dialog.show();
    }
    
    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(true);
    }
}