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

                    part.lowercase().trim('\'').split('?').let { urlParts ->
                        // Invalid URL if it contains more than one '?' character
                        if (urlParts.size > 2) {
                            return CurlValidatorResult(false, "Error: Multiple URLs found.")
                        }

                        val baseUrl = urlParts[0]
                        if (!isValidBaseUrl(baseUrl)) {
                            return CurlValidatorResult(false, "Error: Invalid URL.")
                        }

                        //Query param present
                        if (urlParts.size == 2 && !isValidQueryParams(urlParts[1])) {
                            return CurlValidatorResult(false, "Error: Invalid query parameter format.")
                        }
                    }

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

    private fun String.extractQueryParams(): Map<String, String> {
        return this.substringAfter("?", "").split("&")
            .filter { it.contains("=") }.associate {
                val (key, value) = it.split("=", limit = 2)
                key to URLDecoder.decode(value, StandardCharsets.UTF_8.name())
            }
    }

    private fun String.toBeIgnored() = arrayListOf("--location").contains(this)

    private fun String.isHttpUrl() =
        this.lowercase().trim('\'').let { it.startsWith("http://") || it.startsWith("https://") }

    private fun isValidBaseUrl(baseUrl: String): Boolean {
        // This function should include validation for the base URL structure
        // Here, you might use a regex or a URI parser to validate the base part of the URL
        return try {
            val uri = java.net.URI(baseUrl)
            (uri.host != null && uri.scheme != null) // Ensures the scheme and host are properly defined
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidQueryParams(query: String): Boolean {
        query.split('&').all { param ->
            val index = param.indexOf('=')
            if (index == -1 || index == 0) return false

            val key = param.substring(0, index)
            val value = param.substring(index + 1)

            // Check for illegal characters in key and ensure percent encoding is valid
            if (!key.matches(Regex("^[a-zA-Z0-9._%+-]+$"))) return false
            if (!isValidPercentEncoded(key)) return false

            if (!value.matches(Regex("^[a-zA-Z0-9._%+-]*$"))) return false
            if (!isValidPercentEncoded(value)) return false
            if (value.contains('#')) return false // Ensure no fragment identifiers are part of the value

            return true
        }
        return true
    }

    // Check if a string contains only valid percent-encoded sequences
    private fun isValidPercentEncoded(input: String): Boolean {
        return input.windowed(3, 1)  // Create substrings of length 3
            .filter { it.startsWith('%') }  // Focus on encoded parts
            .all { Regex("%[0-9a-fA-F]{2}").matches(it) }  // Check if all percent-encoded parts are valid
    }


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

    fun groupCurlCommand(curlCommand: String): CurlCommand {
        val parts = parseCommandLine(curlCommand)
        val headers = mutableMapOf<String, String>()
        val options = mutableListOf<String>()
        var method = "GET"
        var url: String? = null
        var data: String? = null
        var queryParams = emptyMap<String, String>()

        var i = 1
        while (i < parts.size) {
            when {
                parts[i].isRequestMethod() -> {
                    method = if (i + 1 < parts.size) parts[++i] else "GET"
                }

                parts[i].isHttpUrl() -> {
                    val urlPart = parts[i].substringBefore('?')
                    queryParams = parts[i].extractQueryParams()
                    url = urlPart
                }

                parts[i].isHeaderOption() -> {
                    if (i + 1 < parts.size) {
                        parts[++i].split(':').also {
                            if (it.size >= 2) headers[it[0].trim()] = it[1].trim()
                        }
                    }
                }

                parts[i].isDataOption() -> {
                    data = if (i + 1 < parts.size) parts[++i] else null
                }

                else -> {
                    options.add(parts[i])
                }
            }
            i++
        }

        return CurlCommand(method, url, headers, data, queryParams = queryParams, options)
    }

}

