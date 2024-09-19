package com.adekola.curlToRetrofit.curl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;

import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClassNameDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField classNameTextField;

    public ClassNameDialog(@Nullable Project project) {
        super(project);
        setTitle("Enter Class Name");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        contentPane = new JPanel(new BorderLayout());
        classNameTextField = new JTextField();
        classNameTextField.setPreferredSize(new Dimension(250, 30));
        contentPane.add(classNameTextField, BorderLayout.CENTER);

        return contentPane;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        String inputText = classNameTextField.getText();
        if (inputText == null || inputText.trim().isEmpty()) {
            return new ValidationInfo("Class name cannot be empty", classNameTextField);
        }
        return null;
    }

    @Override
    protected void doOKAction() {
        if (!getOKAction().isEnabled()) {
            return;
        }
        super.doOKAction();
    }

    public String getClassName() {
        return classNameTextField.getText();
    }
}
