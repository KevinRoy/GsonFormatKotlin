package com.kevin.plugin.gsonformatkotlin.action;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBDimension;
import com.kevin.plugin.gsonformatkotlin.utils.KotlinUtils;
import org.apache.http.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;

public class KotlinFormatAction extends AnAction {

    private boolean isData = false;

    @Override
    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        final Caret caret = event.getData(PlatformDataKeys.CARET);
        final Editor editor = event.getData(PlatformDataKeys.EDITOR_EVEN_IF_INACTIVE);

        if (editor == null) {
            Messages.showWarningDialog("Please open a file in editor state for insert Kotlin code!", "No Editor File");
            return;
        }

        final Messages.InputDialog inputDialog = new Messages.InputDialog(project, "Please input the Json Data", "Input Json"
                , Messages.getInformationIcon(), "", new InputValidator() {
            private final Gson gson = new Gson();

            @Override
            public boolean checkInput(String inputString) {
                try {
                    JsonElement jsonElement = gson.fromJson(inputString, JsonObject.class);
                    return true;
                } catch (JsonSyntaxException e) {
                    return false;
                }
            }

            @Override
            public boolean canClose(String inputString) {
                return true;
            }
        }) {
            @NotNull
            protected JPanel createMessagePanel() {
                JPanel messagePanel = new JPanel(new BorderLayout());
                if (myMessage != null) {
                    JComponent textComponent = createTextComponent();
                    messagePanel.add(textComponent, BorderLayout.NORTH);
                }

                myField = createTextFieldComponent();
                messagePanel.add(createScrollableTextComponent(), BorderLayout.CENTER);

                JCheckBox jCheckBox = new JCheckBox("is data?");
                jCheckBox.addActionListener(e -> {
                    if (jCheckBox.isSelected()) {
                        isData = true;
                    } else {
                        isData = false;
                    }
                });
                messagePanel.add(jCheckBox, BorderLayout.SOUTH);

                return messagePanel;
            }

            @Override
            protected JTextComponent createTextFieldComponent() {
                JTextArea jTextArea = new JTextArea(15, 100);
                jTextArea.setMinimumSize(new JBDimension(800, 500));
                jTextArea.setMaximumSize(new JBDimension(1000, 700));
                jTextArea.setLineWrap(true);
                jTextArea.setWrapStyleWord(true);
                jTextArea.setAutoscrolls(true);
                return jTextArea;
            }

            protected JComponent createScrollableTextComponent() {
                return new JBScrollPane(myField);
            }
        };
        inputDialog.show();

        String jsonString = inputDialog.getInputString();

        if (TextUtils.isEmpty(jsonString)) {
            return;
        }

        final KotlinUtils maker = new KotlinUtils(jsonString, isData);
        final Document document = editor.getDocument();

        CommandProcessor.getInstance().executeCommand(project, () -> ApplicationManager.getApplication().runWriteAction(() -> {

            int offset = 0;

            if (caret != null) {
                offset = caret.getOffset();
            } else {
                offset = document.getTextLength() - 1;
            }
            document.insertString(offset, maker.makeKotlinData());

        }), "insertKotlin", null);
    }
}
