package com.adekola.curlToRetrofit

import com.adekola.curlToRetrofit.curl.validator.Validator
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ValidatorTest {

    @Test
    fun testValidateCurlCommand_correctCommand() {
        val curlCommand =
            "curl --location --request POST 'http://localhost:8080/students/saveStudent/232342' --header 'Content-Type: application/json' --data '{\"name\": \"Kola\",\"age\": 50}'"
        assertThat(Validator.validateCurlCommand(curlCommand).isValid).isTrue()
        assertThat(Validator.validateCurlCommand(curlCommand).errorMessage).isNull()
    }

    @Test
    fun testValidateCurlCommand_missingUrl() {
        val curlCommand = "curl --request POST --data '{\"name\": \"Kola\",\"age\": 50}'"
        assertTrue(Validator.validateCurlCommand(curlCommand).errorMessage.equals("Error: No URL found."))
    }

    @Test
    fun testValidateCurlCommand_invalidMethod() {
        val curlCommand = "curl --request PAST 'http://example.com'"
        assertTrue(Validator.validateCurlCommand(curlCommand).errorMessage.equals("Error: Unsupported HTTP method 'PAST' specified."))
    }

    @Test
    fun testValidateCurlCommand_missingData() {
        val curlCommand =
            "curl --location --request POST 'http://localhost:8080/update' --header 'Content-Type: application/json' --data"
        assertTrue(Validator.validateCurlCommand(curlCommand).errorMessage.equals("Error: No data provided for the data option."))
    }

    @Test
    fun testValidateCurlCommand_invalidHeaderFormat() {
        val curlCommand = "curl -X POST 'http://example.com' -H 'Content-Type'"
        assertTrue(Validator.validateCurlCommand(curlCommand).errorMessage.equals("Error: Invalid header format. Headers must include a colon."))
    }
}
