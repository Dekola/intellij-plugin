package com.adekola.curlToRetrofit.curl

import com.adekola.curlToRetrofit.curl.codeGenerator.CodeGeneratorResult
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory

object KotlinClassCreator {

    fun createKotlinFile(
        project: Project,
        codeGeneratorResult: CodeGeneratorResult,
        selectedFolder: PsiDirectory,
        newClassName: String,
        selectedLanguage: LanguageSelection
    ) {

//        val directory = PsiManager.getInstance(project)
//            .findDirectory(project.baseDir)

        val suffix = if (selectedLanguage == LanguageSelection.JAVA) ".java" else ".kt"

        WriteCommandAction.runWriteCommandAction(project) {
            val fileType = FileTypeManager.getInstance().getFileTypeByExtension("kt")
            val psiFile = PsiFileFactory.getInstance(project)
                .createFileFromText("$newClassName$suffix", fileType, codeGeneratorResult.fullCode)
            selectedFolder.add(psiFile)

        }

    }
}
