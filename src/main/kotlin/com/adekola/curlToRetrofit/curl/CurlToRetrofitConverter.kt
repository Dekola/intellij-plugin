package com.adekola.curlToRetrofit.curl

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CurlToRetrofitConverter : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        CurlInputDialog(event).show()
    }

}