package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGeneratorResult
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager

object KotlinClassCreator {

    fun createKotlinFile(project: Project, codeGeneratorResult: CodeGeneratorResult) {
        val fileName = "ApiService"

        val directory = PsiManager.getInstance(project)
            .findDirectory(project.baseDir)

        WriteCommandAction.runWriteCommandAction(project) {
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension("kt")
            val psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("$fileName.kt", fileType, codeGeneratorResult.fullCode)
            directory!!.add(psiFile)
        }
    }


}
