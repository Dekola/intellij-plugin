package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGenerator
import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGeneratorResult
import com.adekola.curlToRetrofit.curl.validator.CurlValidator
import com.adekola.curlToRetrofit.curl.validator.FileValidator
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.Box
import javax.swing.ButtonGroup
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField


class CurlInputDialog(private val event: AnActionEvent) : DialogWrapper(true) {

    private lateinit var panel: JPanel
    private var codeGeneratorResult: CodeGeneratorResult? = null

    private val inputTextArea = JTextArea(5, 50)
    private val resultTextArea = JTextArea(3, 50)
    private val submitButton = JButton("Submit")

    private val classNameText: JLabel = JLabel()
    private val classNameTextField: JTextField = JTextField()

    private val methodNameText: JLabel = JLabel()
    private val methodNameTextField: JTextField = JTextField()

    private val javaRadioButton: JRadioButton = JRadioButton("Java")
    private val kotlinRadioButton: JRadioButton = JRadioButton("Kotlin", true)
    private val languageGroup: ButtonGroup = ButtonGroup()

    private val copyButton = JButton("Copy to Clipboard")
    private val addToExistingButton = JButton("Add to existing class")
    private val createClassButton = JButton("Create new class")

    init {
        init()
        title = "CURL To Retrofit Converter"
        isResizable()
//        isResizable = true
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


        constraints.gridy += 1
        constraints.weighty = 0.0
        panel.add(JScrollPane(inputTextArea), constraints)

        methodNameText.text = "Method Name"
        constraints.gridy += 1
        panel.add(methodNameText, constraints)

        methodNameTextField.text = "performRequest"
        methodNameTextField.preferredSize = Dimension(250, 30)
        constraints.gridy += 1
        panel.add(methodNameTextField, constraints)

        constraints.gridy += 1
        panel.add(submitButton, constraints)

        constraints.gridy += 1

        resultTextArea.isEditable = false
        panel.add(JScrollPane(resultTextArea), constraints)

        classNameText.text = "Class Name"
        constraints.gridy += 1
        panel.add(classNameText, constraints)

        classNameTextField.text = "ApiService"
        classNameTextField.preferredSize = Dimension(250, 30)
        constraints.gridy += 1
        panel.add(classNameTextField, constraints)

        languageGroup.add(kotlinRadioButton);
        languageGroup.add(javaRadioButton);

        constraints.gridy += 1
        panel.add(javaRadioButton, constraints)
        constraints.gridy += 1
        panel.add(kotlinRadioButton, constraints)


        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(copyButton)
        buttonPanel.add(addToExistingButton)
        buttonPanel.add(createClassButton)
        constraints.gridy += 1
        constraints.weighty = 0.0
        panel.add(buttonPanel, constraints)

        constraints.gridy += 1
        constraints.weighty = 1.0
        panel.add(Box.createVerticalGlue(), constraints)

        addToExistingButton.addActionListener {
            try {
                addCodeToExistingClass()
            } catch (e: Exception) {
                showErrorDialog(e.message, title = "Error Adding Code")
            }
        }

        createClassButton.addActionListener {
            try {
                createNewClass(getSelectedLanguage())
            } catch (e: Exception) {
                showErrorDialog(e.message, title = "Error Creating Class")
            }
        }

        copyButton.addActionListener {
            copyCode()
        }

        submitButton.addActionListener {
            generateCodeFromCurl(getSelectedLanguage())
        }

        javaRadioButton.addActionListener { e: ActionEvent? ->
            generateCodeFromCurl(getSelectedLanguage())
        }
        kotlinRadioButton.addActionListener { e: ActionEvent? ->
            generateCodeFromCurl(getSelectedLanguage())
        }


        return panel
    }

    fun getSelectedLanguage(): LanguageSelection {
        return if (javaRadioButton.isSelected) {
            LanguageSelection.JAVA
        } else {
            LanguageSelection.KOTLIN
        }
    }

    private fun generateCodeFromCurl(selectedLanguage: LanguageSelection) {
        val curl = inputTextArea.text
        val validationResult = CurlValidator.validateCurlCommand(curl)
        if (validationResult.isValid) {
            processResult(curl, selectedLanguage)
        } else {
            showErrorDialog(validationResult.errorMessage)
        }
    }

    private fun copyCode() {
        val resultText = resultTextArea.text
        if (resultText.isNotEmpty()) {
            Toolkit.getDefaultToolkit().systemClipboard.setContents(
                StringSelection(resultText),
                null
            )
        } else {
            showErrorDialog("No text to copy")
        }
    }

    private fun createNewClass(selectedLanguage: LanguageSelection) {
        event.project?.let { projectEvent ->
//            getClassName(projectEvent)?.let { newClassName ->
            chooseFolder(projectEvent)?.let { selectedFolder ->
                codeGeneratorResult?.let { codeGeneratorResult ->

                    KotlinClassCreator.createKotlinFile(
                        projectEvent,
                        codeGeneratorResult,
                        selectedFolder,
                        classNameTextField.text,
                        selectedLanguage
                    )

                }
            }
//            }
        }
    }


    private fun chooseFolder(project: Project): PsiDirectory? {
        val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        descriptor.title = "Select Folder"
        descriptor.description = "Choose a folder:"

        val folder:VirtualFile? = FileChooser.chooseFile(descriptor, project, null)

        return if (folder != null && folder.isDirectory) {
            PsiManager.getInstance(project).findDirectory(folder)
        } else {
            null
        }

    }

    private fun addCodeToExistingClass() {
        val descriptor = FileChooserDescriptor(
            true, false, false, false, false, false
        ).withFileFilter { vf -> vf.extension in listOf("java", "kt") }
            .withRoots(event.project?.guessProjectDir())

        FileChooser.chooseFile(descriptor, event.project, event.project?.guessProjectDir())
            ?.let { file ->
                val fileValidatorResult = FileValidator.isSelectedFileValid(file, event.project)
                if (fileValidatorResult.isValid) {
                    appendBeforeLastCurlyBrace(file)
                } else {
                    showErrorDialog(fileValidatorResult.errorMessage)
                }
            }
    }

    private fun appendBeforeLastCurlyBrace(file: VirtualFile) {
        val curl = inputTextArea.text

        val extension =
            if (file.extension == "kt") LanguageSelection.KOTLIN else LanguageSelection.JAVA
        getCode(curl, extension)?.let { resultText ->

            WriteCommandAction.runWriteCommandAction(event.project) {
                val content = String(file.contentsToByteArray())
                val lastCurlyIndex = content.lastIndexOf("}")

                if (lastCurlyIndex != -1) {
                    val newContent =
                        content.substring(
                            0,
                            lastCurlyIndex
                        ) + resultText + "\n" + content.substring(
                            lastCurlyIndex
                        )

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
    }

    private fun processResult(curl: String, selectedLanguage: LanguageSelection) {
        val groupedCurlCommand = CurlValidator.groupCurlCommand(curl)

        codeGeneratorResult =
            if (selectedLanguage == LanguageSelection.JAVA) CodeGenerator.generateJavaRetrofitInterface(
                groupedCurlCommand,
                methodName = methodNameTextField.text,
                className = classNameTextField.text
            ) else CodeGenerator.generateKotlinRetrofitInterface(
                groupedCurlCommand,
                methodName = methodNameTextField.text,
                className = classNameTextField.text
            )

        resultTextArea.text = codeGeneratorResult?.codeFunction
    }

    private fun getCode(curl: String, selectedLanguage: LanguageSelection): String? {
        val groupedCurlCommand = CurlValidator.groupCurlCommand(curl)

        codeGeneratorResult =
            if (selectedLanguage == LanguageSelection.JAVA) CodeGenerator.generateJavaRetrofitInterface(
                groupedCurlCommand,
                methodName = methodNameTextField.text,
                className = classNameTextField.text
            ) else CodeGenerator.generateKotlinRetrofitInterface(
                groupedCurlCommand,
                methodName = methodNameTextField.text,
                className = classNameTextField.text
            )

        return codeGeneratorResult?.codeFunction
    }

    private fun showErrorDialog(errorMessage: String?, title: String = "Validation Result") {
        Messages.showMessageDialog(
            panel,
            errorMessage,
            title,
            Messages.getInformationIcon(),
        )
    }

}