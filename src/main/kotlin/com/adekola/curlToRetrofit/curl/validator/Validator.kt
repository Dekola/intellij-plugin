package com.adekola.curlToRetrofit.curl.validator

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.ByteArrayInputStream
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

object Validator {
    fun validateCurlCommand(curlCommand: String): CurlValidatorResult {
        val parts = parseCommandLine(curlCommand)

        if (parts.isEmpty() || parts[0] != "curl") {
            return CurlValidatorResult(false, "Error: Command must start with 'curl'.")
        }

        var hasUrl = false
        var hasRequestType = false
        var hasDataType = false
        var hasHeader = false

        val supportedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD")

        var i = 1
        while (i < parts.size) {
            val part = parts[i]
            when {
                part.toBeIgnored() -> {
                }

                part.isHttpUrl() -> {
                    if (hasUrl) return CurlValidatorResult(false, "Error: Multiple URLs found.")
                    hasUrl = true
                }

                part.isRequestMethod() -> {
                    if (i + 1 >= parts.size) {
                        return CurlValidatorResult(false, "Error: No HTTP method specified.")
                    } else if (parts[i + 1].uppercase() !in supportedMethods) {
                        return CurlValidatorResult(false, "Error: Unsupported HTTP method '${parts[i + 1]}' specified.")
                    }
                    i++ // Skip next part as it's part of the current option

                    if (hasRequestType) return CurlValidatorResult(false, "Error: Multiple Request Types found.")
                    hasRequestType = true
                }

                part.isDataOption() -> {
                    // Checks if there is no next part or if the next part is another option.
                    if (i + 1 >= parts.size || parts[i + 1].startsWith("-")) {
                        return CurlValidatorResult(false, "Error: No data provided for the data option.")
                    } else {
                        val dataContent = parts[i + 1]
                        if (!(dataContent.isValidJson() || dataContent.isXmlData() || dataContent.isUrlEncoded())) {
                            return CurlValidatorResult(false, "Error: Unsupported data format.")
                        }
                    }

                    if (hasDataType) return CurlValidatorResult(false, "Error: Multiple Data Types found.")
                    hasDataType = true
                    i++
                }

                part.isHeaderOption() -> {
                    if (i + 1 >= parts.size) {
                        return CurlValidatorResult(false, "Error: No header found")
                    }
                    if (!parts[i + 1].contains(":")) {
                        return CurlValidatorResult(false, "Error: Invalid header format. Headers must include a colon.")
                    }

                    if (hasHeader) return CurlValidatorResult(false, "Error: Multiple Headers found.")
                    hasHeader = true
                    i++ // Skip next part as it's part of the current option
                }

                else -> {
                    return CurlValidatorResult(false, "Error: Unrecognized option '$part'.")
                }
            }
            i++
        }

        if (!hasUrl) return CurlValidatorResult(false, "Error: No URL found.")

        return CurlValidatorResult(true)
    }

    private fun String.toBeIgnored() = arrayListOf("--location").contains(this)

    private fun String.isHttpUrl() = this.lowercase().trim('\'').let { it.startsWith("http://") || it.startsWith("https://") }

    private fun String.isRequestMethod() = (this == "--request" || this == "-X")

    private fun String.isDataOption() = this == "-d" || this == "--data"

    private fun String.isHeaderOption() = (this == "-H" || this == "--header")

    private fun String.isValidJson(): Boolean {
        if (!this.startsWith("{") && !this.startsWith("[")) {
            return false
        }
        return try {
            val mapper = ObjectMapper()
            mapper.readTree(this)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun String.isXmlData(): Boolean {
        return try {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            builder.parse(ByteArrayInputStream(this.toByteArray())) // This will throw if the XML is bad
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun String.isUrlEncoded(): Boolean {
        return try {
            val decoded = URLDecoder.decode(this, StandardCharsets.UTF_8.name())
            this.contains('=') &&
                this.split('&').all { pair ->
                    pair.contains('=') && pair.split('=').size == 2
                }
        } catch (e: Exception) {
            false
        }
    }

    private fun parseCommandLine(command: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentToken = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '

        command.forEach { char ->
            when {
                char == ' ' && !inQuotes -> {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    }
                }

                char == '"' || char == '\'' -> {
                    if (inQuotes && char == quoteChar) {
                        inQuotes = false
                        tokens.add(currentToken.toString())
                        currentToken = StringBuilder()
                    } else if (!inQuotes) {
                        inQuotes = true
                        quoteChar = char
                    } else {
                        currentToken.append(char)
                    }
                }

                else -> currentToken.append(char)
            }
        }
        if (currentToken.isNotEmpty()) {
            tokens.add(currentToken.toString())
        }
        return tokens
    }
}
