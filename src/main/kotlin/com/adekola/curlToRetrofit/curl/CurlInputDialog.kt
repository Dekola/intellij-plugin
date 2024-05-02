package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.validator.Validator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class TextUploaderAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        CurlInputDialog().show()
    }
}

class CurlInputDialog : DialogWrapper(true) {
    private var textArea: JTextArea = JTextArea(10, 50)
    private var submitButton: JButton = JButton("Submit")

    init {
        init()
        title = "CURL Upload"
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.add(textArea, BorderLayout.CENTER)
        panel.add(submitButton, BorderLayout.SOUTH)

        submitButton.addActionListener {
            val validationResult = Validator.validateCurlCommand(textArea.text)
            if (validationResult.isValid) {
                Messages.showMessageDialog(
                    panel,
                    "Success",
                    "Validation Result",
                    Messages.getInformationIcon(),
                )
            } else {
                Messages.showMessageDialog(
                    panel,
                    validationResult.errorMessage,
                    "Validation Result",
                    Messages.getInformationIcon(),
                )
            }
        }
        return panel
    }
}
