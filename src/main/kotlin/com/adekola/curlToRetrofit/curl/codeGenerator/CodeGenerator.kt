package com.adekola.curlToRetrofit.curl.codeGenerator

import com.adekola.curlToRetrofit.curl.validator.CurlCommand

object CodeGenerator {

    fun generateRetrofitInterface(curlCommand: CurlCommand): CodeGeneratorResult {
        val methodName = "performRequest"
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

        val imports = """
            import retrofit2.Call
            import retrofit2.http.$httpMethodAnnotation
            import retrofit2.http.Body
            import okhttp3.RequestBody
        """

        // Generate the complete interface
        val fullCode = """
            $imports
            interface ApiService {
                $httpMethodAnnotation("$fullPath")
                $methodSignature
            }
        """.trimIndent()

        val codeFunction = """
            $httpMethodAnnotation("$fullPath")
            $methodSignature
            """.trimIndent()

        return CodeGeneratorResult(fullCode = fullCode, codeFunction = codeFunction)
    }
}
