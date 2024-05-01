package com.adekola.curlToRetrofit.curl.validator

object Validator {

    fun validateCurlCommand(curlCommand: String): CurlValidatorResult {

        val processedCommand = curlCommand.trim().replace("\\", "")

        val parts = processedCommand.split(Regex("\\s+"))
        if (parts.isEmpty() || parts[0] != "curl") {
            return CurlValidatorResult(false, "Error: Command must start with 'curl'.")
        }

        var hasUrl = false
        var hasValidMethod = false
        var errorMessage = ""

        var i = 1
        while (i < parts.size) {
            when {
                parts[i].startsWith("'http://") || parts[i].startsWith("'https://") || parts[i].startsWith("http://") || parts[i].startsWith(
                    "https://"
                ) -> {
                    val url = parts[i].trim('\'')
                    if (hasUrl) {
                        errorMessage += "Error: Multiple URLs found.\n"
                    }
                    hasUrl = true
                }

                (parts[i] == "--request" || parts[i] == "-X") && i + 1 < parts.size -> {
                    val method = parts[i + 1]
                    if (method !in listOf("GET", "POST", "PUT", "DELETE", "PATCH")) {
                        errorMessage += "Error: Unsupported HTTP method '$method' specified.\n"
                    } else {
                        hasValidMethod = true
                    }
                    i++
                }

                parts[i].startsWith("-d") || parts[i].startsWith("--data") -> {
                    if (i + 1 >= parts.size || (!parts[i + 1].startsWith("'") && !parts[i + 1].startsWith("\""))) {
                        errorMessage += "Error: No data provided for the data option.\n"
                    }
                    i++
                }

                parts[i].startsWith("-H") || parts[i].startsWith("--header") -> {
                    if (i + 1 >= parts.size || !parts[i + 1].contains(":")) {
                        errorMessage += "Error: Invalid header format. Headers must include a colon.\n"
                    }
                    i++
                }
            }
            i++
        }

        if (!hasUrl) {
            errorMessage += "Error: No URL found.\n"
        }

        if (!hasValidMethod) {
            errorMessage += "Error: No valid HTTP method specified.\n"
        }

        return if (errorMessage.isEmpty()) CurlValidatorResult(true) else CurlValidatorResult(false, errorMessage)
    }
}
