package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGenerator
import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGeneratorResult
import com.adekola.curlToRetrofit.curl.validator.CurlValidator
import com.adekola.curlToRetrofit.curl.validator.FileValidator
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.ui.JBUI
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import javax.swing.Box
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

class CurlInputDialog(private val event: AnActionEvent) : DialogWrapper(true) {

    private lateinit var panel: JPanel
    private var codeGeneratorResult: CodeGeneratorResult? = null

    private val inputTextArea = JTextArea(5, 50)
    private val resultTextArea = JTextArea(3, 50)
    private val submitButton = JButton("Submit")
    private val copyButton = JButton("Copy to Clipboard")
    private val addToExistingButton = JButton("Add to existing class")
    private val createClassButton = JButton("Create new class")

    init {
        init()
        title = "CURL Upload"
        isResizable = true
    }

    override fun createCenterPanel(): JComponent {
        panel = JPanel(GridBagLayout())
        val constraints = GridBagConstraints()

        inputTextArea.text = "curl --request HEAD 'http://example.com'"

        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.gridx = 0
        constraints.weightx = 1.0
        constraints.insets = JBUI.insets(4)
        constraints.anchor = GridBagConstraints.NORTH

        constraints.gridy = 0
        constraints.weighty = 0.0
        panel.add(JScrollPane(inputTextArea), constraints)

        constraints.gridy = 1
        panel.add(submitButton, constraints)

        constraints.gridy = 2

        resultTextArea.isEditable = false
        panel.add(JScrollPane(resultTextArea), constraints)

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(copyButton)
        buttonPanel.add(addToExistingButton)
        buttonPanel.add(createClassButton)
        constraints.gridy = 3
        constraints.weighty = 0.0
        panel.add(buttonPanel, constraints)

        constraints.gridy = 4
        constraints.weighty = 1.0
        panel.add(Box.createVerticalGlue(), constraints)

        addToExistingButton.addActionListener {
            addCodeToExistingClass()
        }

        createClassButton.addActionListener {
            createNewClass()
        }

        copyButton.addActionListener {
            copyCode()
        }

        submitButton.addActionListener {
            generateCodeFromCurl()
        }

        return panel
    }

    private fun generateCodeFromCurl() {
        val curl = inputTextArea.text
        val validationResult = CurlValidator.validateCurlCommand(curl)
        if (validationResult.isValid) {
            processResult(curl)
        } else {
            showErrorDialog(validationResult.errorMessage)
        }
    }

    private fun copyCode() {
        val resultText = resultTextArea.text
        if (resultText.isNotEmpty()) {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(resultText), null)
        } else {
            showErrorDialog("No text to copy")
        }
    }

    private fun createNewClass() {
        event.project?.let { projectEvent ->
            codeGeneratorResult?.let { codeGeneratorResult ->
                KotlinClassCreator.createKotlinFile(projectEvent, codeGeneratorResult)
            }
        }
    }

    private fun addCodeToExistingClass() {
        val descriptor = FileChooserDescriptor(
            true, false, false, false, false, false
        ).withFileFilter { vf -> vf.extension in listOf("java", "kt") }.withRoots(event.project?.guessProjectDir())

        FileChooser.chooseFile(descriptor, event.project, event.project?.guessProjectDir())?.let { file ->
            val fileValidatorResult = FileValidator.isSelectedFileValid(file, event.project)
            if (fileValidatorResult.isValid) {
                appendBeforeLastCurlyBrace(file)
            } else {
                showErrorDialog(fileValidatorResult.errorMessage)
            }
        }
    }

    private fun appendBeforeLastCurlyBrace(file: VirtualFile) {
        val resultText = resultTextArea.text
        WriteCommandAction.runWriteCommandAction(event.project) {
            val content = String(file.contentsToByteArray())
            val lastCurlyIndex = content.lastIndexOf("}")

            if (lastCurlyIndex != -1) {
                val newContent =
                    content.substring(0, lastCurlyIndex) + resultText + "\n" + content.substring(lastCurlyIndex)

                file.setBinaryContent(newContent.toByteArray(Charsets.UTF_8))

                // Format the newly changed part of the file
                val psiFile = PsiManager.getInstance(event.project!!).findFile(file)
                psiFile?.let {
                    CodeStyleManager.getInstance(event.project!!)
                        .reformatText(it, lastCurlyIndex, lastCurlyIndex + resultText.length)
                }
            }
        }
    }

    private fun processResult(curl: String) {
        val groupedCurlCommand = CurlValidator.groupCurlCommand(curl)
        codeGeneratorResult = CodeGenerator.generateRetrofitInterface(groupedCurlCommand)

        resultTextArea.text = codeGeneratorResult?.codeFunction
    }

    private fun showErrorDialog(errorMessage: String?) {
        Messages.showMessageDialog(
            panel,
            errorMessage,
            "Validation Result",
            Messages.getInformationIcon(),
        )
    }

}