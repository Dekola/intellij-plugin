package com.adekola.curlToRetrofit.curl.validator

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

object FileValidator {

    fun isSelectedFileValid(file: VirtualFile, project: Project?): FileValidatorResult {
        val fileIsWithinProjectFolder = file.path.startsWith(project?.guessProjectDir()?.path!!)

        if (!fileIsWithinProjectFolder) {
            return FileValidatorResult(false, "Selected file Isn't withing project folder")
        }

        val fileIsAnInterfaceClass = isInterface(file)

        if (!fileIsAnInterfaceClass) {
            return FileValidatorResult(false, "Selected file Isn't an interface class")
        }
        return FileValidatorResult(isValid = true)
    }


    private fun isInterface(file: VirtualFile): Boolean {
        val contents = String(file.contentsToByteArray())

        val indexOfBrace = contents.indexOf('{')
        if (indexOfBrace == -1) return false // Return false if no brace is found

        val textBeforeBrace = contents.substring(0, indexOfBrace).trim()
        val words = textBeforeBrace.split(Regex("\\s+"))

        return words.getOrNull(words.size - 2)?.equals("interface", ignoreCase = true) == true

//        val psiFile = PsiManager.getInstance(event.project!!).findFile(file) ?: return false
//        // For Java
//        psiFile.elementType
//        val javaInterfaces = PsiTreeUtil.findChildrenOfType(psiFile, PsiClass::class.java)
//            .any { it.isInterface }
//
//        // For Kotlin
//        val kotlinInterfaces = PsiTreeUtil.findChildrenOfType(psiFile, KtClass::class.java)
//            .any { it.isInterface() }
//
//        return javaInterfaces || kotlinInterfaces
    }

}