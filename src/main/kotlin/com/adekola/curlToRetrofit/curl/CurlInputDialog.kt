package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.validator.CurlCommand
import com.adekola.curlToRetrofit.curl.validator.Validator
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea

class TextUploaderAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        CurlInputDialog { curlCommand ->
            val project = event.project
            if (project != null) {
                createKotlinFile(project, curlCommand)
            }
        }.show()

    }

    private fun createKotlinFile(project: Project, curlCommand: CurlCommand) {
        val fileName = "ApiService"

//        val curlCommand = CurlCommand(
//            method = "GET",
//            url = "http://example.com/resource",
//            headers = mapOf("Authorization" to "Bearer token"),
//            data = null,
//            queryParams = mapOf("search" to "query"),
//            options = listOf("--verbose")
//        )

        val directory = PsiManager.getInstance(project)
            .findDirectory(project.baseDir)
        val fileContent = generateRetrofitInterface(curlCommand)

        WriteCommandAction.runWriteCommandAction(project) {
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension("kt")
            val psiFile = PsiFileFactory.getInstance(project).createFileFromText("$fileName.kt", fileType, fileContent)
            directory!!.add(psiFile)
        }
    }

    private fun generateRetrofitInterface(curlCommand: CurlCommand): String {
        val methodName =
            "performRequest"
        val baseUrl = curlCommand.url?.substringBefore('?') ?: ""
        val queryParams = curlCommand.queryParams?.entries?.joinToString("&") { "${it.key}=${it.value}" } ?: ""
        val fullPath = if (queryParams.isNotEmpty()) "$baseUrl?$queryParams" else baseUrl

        val httpMethodAnnotation = when (curlCommand.method?.toUpperCase()) {
            "GET" -> "@GET"
            "POST" -> "@POST"
            "PUT" -> "@PUT"
            "DELETE" -> "@DELETE"
            "HEAD" -> "@HEAD"
            else -> "@GET" // Default to GET if method is null or not recognized
        }

        val methodSignature = if (curlCommand.data != null && curlCommand.method in listOf("POST", "PUT", "DELETE")) {
            "fun $methodName(@Body body: RequestBody): Call<Void>"
        } else {
            "fun $methodName(): Call<Void>"
        }

        // Generate the complete interface
        return """
        import retrofit2.Call
        import retrofit2.http.$httpMethodAnnotation
        import retrofit2.http.Body
        import okhttp3.RequestBody

        interface ApiService {
            $httpMethodAnnotation("$fullPath")
            $methodSignature
        }
    """.trimIndent()
    }

}

class CurlInputDialog(private val curlCommandListener: (CurlCommand) -> Unit) : DialogWrapper(true) {
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
            val curl = textArea.text
            val validationResult = Validator.validateCurlCommand(curl)
            if (validationResult.isValid) {
                Messages.showMessageDialog(
                    panel,
                    "Success",
                    "Validation Result",
                    Messages.getInformationIcon(),
                )

                val curlCommand = Validator.groupCurlCommand(curl)
                curlCommandListener.invoke(curlCommand)
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
