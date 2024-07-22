package com.adekola.curlToRetrofit.curl.codeGenerator

import com.adekola.curlToRetrofit.curl.validator.CurlCommand
import java.util.Locale
import java.util.stream.Collectors


object CodeGenerator {

    fun generateKotlinRetrofitInterface(
        curlCommand: CurlCommand,
        methodName: String = "performRequest",
        className: String = "ApiService"
    ): CodeGeneratorResult {

        val baseUrl = curlCommand.url?.substringBefore('?') ?: ""
        val queryParams =
            curlCommand.queryParams?.entries?.joinToString("&") { "${it.key}=${it.value}" } ?: ""
        val fullPath = if (queryParams.isNotEmpty()) "$baseUrl?$queryParams" else baseUrl

        val httpMethodAnnotation = when (curlCommand.method?.toUpperCase()) {
            "GET" -> "@GET"
            "POST" -> "@POST"
            "PUT" -> "@PUT"
            "DELETE" -> "@DELETE"
            "HEAD" -> "@HEAD"
            else -> "@GET" // Default to GET if method is null or not recognized
        }

        val methodSignature =
            if (curlCommand.data != null && curlCommand.method in listOf("POST", "PUT", "DELETE")) {
                "fun $methodName(@Body body: RequestBody): Call<Void>"
            } else {
                "fun $methodName(): Call<Void>"
            }

        val imports = """
            import retrofit2.Call
            import retrofit2.http.${curlCommand.method?.toUpperCase()}
            import retrofit2.http.Body
            import okhttp3.RequestBody
        """

        // Generate the complete interface
        val fullCode = """
            $imports
            interface $className {
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

    fun generateJavaRetrofitInterface(
        curlCommand: CurlCommand,
        methodName: String = "performRequest",
        className: String = "ApiService"
    ): CodeGeneratorResult {

        val baseUrl = if (curlCommand.url != null) curlCommand.url.split("\\?".toRegex())
            .dropLastWhile { it.isEmpty() }
            .toTypedArray()[0] else ""
        val queryParams =
            if (curlCommand.queryParams != null) curlCommand.queryParams.entries.stream()
                .map { entry: Map.Entry<String, String> -> entry.key + "=" + entry.value }
                .collect(Collectors.joining("&")) else ""
        val fullPath = if (queryParams.isNotEmpty()) "$baseUrl?$queryParams" else baseUrl

        var httpMethodAnnotation = "GET" // Default
        if (curlCommand.method != null) {
            httpMethodAnnotation =
                when (curlCommand.method.uppercase(Locale.getDefault())) {
                    "POST" -> "POST"
                    "PUT" -> "PUT"
                    "DELETE" -> "DELETE"
                    "HEAD" -> "HEAD"
                    else -> "GET"
                }
        }
        val methodSignature =
            if (curlCommand.data != null && listOf("POST", "PUT", "DELETE").contains(
                    curlCommand.method!!.uppercase(
                        Locale.getDefault()
                    )
                )
            ) {
                "Call<Void> $methodName(@Body RequestBody body);"
            } else {
                "Call<Void> $methodName();"
            }

        val imports = """
              import retrofit2.Call;
              import retrofit2.http.$httpMethodAnnotation;
              import retrofit2.http.Body;
              import okhttp3.RequestBody;
              
              """.trimIndent()

        val fullCode = """${imports}interface $className {
    @$httpMethodAnnotation("$fullPath")
    $methodSignature
}"""

        val codeFunction = "@$httpMethodAnnotation(\"$fullPath\")\n$methodSignature"

        return CodeGeneratorResult(fullCode, codeFunction)
    }
}
