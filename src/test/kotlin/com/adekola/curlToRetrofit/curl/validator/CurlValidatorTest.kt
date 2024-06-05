package com.adekola.curlToRetrofit.curl.validator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class CurlValidatorTest {
    @Test
    fun testValidateCurlCommand_correctCommand() {
        val curlCommand =
            "curl --location --request POST 'http://localhost:8080/students/saveStudent/232342' --header 'Content-Type: application/json' --data '{\"name\": \"Kola\",\"age\": 50}'"

        assertNull(CurlValidator.validateCurlCommand(curlCommand).errorMessage)
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).isValid)
    }

    @Test
    fun testValidateCurlCommand_missingUrl() {
        val curlCommand = "curl --request POST --data '{\"name\": \"Kola\",\"age\": 50}'"
        assertEquals("Error: No URL found.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_invalidMethod() {
        val curlCommand = "curl --request PAST 'http://example.com'"
        assertEquals(
            "Error: Unsupported HTTP method 'PAST' specified.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_missingData() {
        val curlCommand =
            "curl --location --request POST 'http://localhost:8080/update' --header 'Content-Type: application/json' --data"
        assertEquals(
            "Error: No data provided for the data option.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_invalidHeaderFormat() {
        val curlCommand = "curl -X POST 'http://example.com' -H 'Content-Type'"
        assertEquals(
            "Error: Invalid header format. Headers must include a colon.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_noHeaderProvided() {
        val curlCommand = "curl -X POST 'http://example.com' -H"
        assertEquals(
            "Error: No header found",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_unsupportedMethod() {
        val curlCommand = "curl --request PAST 'http://example.com'"
        assertEquals(
            "Error: Unsupported HTTP method 'PAST' specified.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_noDataProvided() {
        val curlCommand = "curl --request POST 'http://localhost:8080/update' --data"
        assertEquals(
            "Error: No data provided for the data option.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_multipleUrls() {
        val curlCommand =
            "curl --request POST 'http://example.com' 'http://example2.com' --data '{\"name\": \"Kola\",\"age\": 50}'"
        assertEquals("Error: Multiple URLs found.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_missingHeaderColon() {
        val curlCommand = "curl --request POST 'http://example.com' -H 'Authorization Bearer token'"
        assertEquals(
            "Error: Invalid header format. Headers must include a colon.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_validGetMethod() {
        val curlCommand = "curl --request GET 'http://example.com'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).isValid)
        assertNull(CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_missingDataForPost() {
        val curlCommand = "curl --request POST 'http://example.com' --data"
        assertEquals(
            "Error: No data provided for the data option.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_extraSpaces() {
        val curlCommand = "curl  --request  POST  'http://example.com'  --data  '{\"name\": \"Kola\"}'"
        assert(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).isValid)
    }

    @Test
    fun testValidateCurlCommand_noRequestMethod() {
        val curlCommand = "curl 'http://example.com'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
    }

    // Provide more specific error message
    @Test
    fun testValidateCurlCommand_malformedUrl() {
        val curlCommand = "curl --request POST 'http//example.com' --data '{\"name\": \"Kola\"}'"
        assertEquals(
            "Error: Unrecognized option 'http//example.com'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage
        )
    }

    @Test
    fun testValidateCurlCommand_incorrectOptionSpacing() {
        val curlCommand = "curl --requestPOST 'http://example.com'"
        assertEquals(
            "Error: Unrecognized option '--requestPOST'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_doubleMethod() {
        val curlCommand = "curl --request POST --request GET 'http://example.com'"
        assertEquals(
            "Error: Multiple Request Types found.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_dataWithNoValue() {
        val curlCommand = "curl --request POST 'http://example.com' --data"
        assertEquals(
            "Error: No data provided for the data option.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_headerWithNoValue() {
        val curlCommand = "curl --request POST 'http://example.com' -H"
        assertEquals(
            "Error: No header found",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_invalidProtocol() {
        val curlCommand = "curl --request POST 'ftp://example.com' --data '{\"name\": \"Kola\"}'"
        assertEquals(
            "Error: Unrecognized option 'ftp://example.com'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage
        )
    }

    @Test
    fun testValidateCurlCommand_escapedQuotesInData() {
        val curlCommand = "curl --request POST 'http://example.com' --data '{\"name\": \"Ko\\\"la\"}'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
    }

    @Test
    fun testValidateCurlCommand_singleQuotesAroundUrl() {
        val curlCommand = "curl --request POST 'http://example.com' --data '{\"name\": \"Kola\"}'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
    }

    // Remove extra dash
    @Test
    fun testValidateCurlCommand_extraDashInOption() {
        val curlCommand = "curl ---request POST 'http://example.com' --data '{\"name\": \"Kola\"}'"
        assertEquals(
            "Error: Unrecognized option '---request'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_queryParamsInUrl() {
        val curlCommand = "curl --request POST 'http://example.com?param=value' --data '{\"name\": \"Kola\"}'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
    }

    @Test
    fun testValidateCurlCommand_httpsUrlWithPort() {
        val curlCommand = "curl --request POST 'https://example.com:8080' --data '{\"name\": \"Kola\"}'"
        assertTrue(CurlValidator.validateCurlCommand(curlCommand).errorMessage.isNullOrEmpty())
    }

    @Test
    fun testValidateCurlCommand_incompleteEscapeSequence() {
        val curlCommand = "curl --request POST 'http://example.com' --data '{\"name\": \"Ko\\la\"}'"
        assertEquals("Error: Unsupported data format.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_unnecessaryEscapeInUrl() {
        val curlCommand = "curl --request POST 'http:\\/\\/example.com' --data '{\"name\": \"Kola\"}'"
        assertEquals(
            "Error: Unrecognized option 'http:\\/\\/example.com'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_correctSimpleGetRequest() {
        val curlCommand = "curl 'http://example.com'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_postWithJsonBody() {
        val curlCommand = "curl --request POST 'http://example.com' --data '{\"name\":\"John\", \"age\":30}'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_postWithMalformedJson() {
        val curlCommand = "curl --request POST 'http://example.com' --data '{name:\"John\", age:30}'"
        assertEquals("Error: Unsupported data format.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_postWithXmlBody() {
        val curlCommand =
            "curl --request POST 'http://example.com' --data '<user><name>John</name><age>30</age></user>'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_putRequestWithUrlEncodedData() {
        val curlCommand = "curl --request PUT 'http://example.com' --data 'name=John&age=30'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_deleteRequest() {
        val curlCommand = "curl --request DELETE 'http://example.com/resource'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_headRequest() {
        val curlCommand = "curl --request HEAD 'http://example.com'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_withUserAgentHeader() {
        val curlCommand = "curl --header 'User-Agent: CustomAgent' 'http://example.com'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_withMultipleHeaders() {
        val curlCommand =
            "curl --header 'Accept: application/json' --header 'Content-Type: application/json' 'http://example.com'"
        assertEquals("Error: Multiple Headers found.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_withIncorrectPortNumber() {
        val curlCommand = "curl 'http://example.com:999999'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_withUnsupportedProtocol() {
        val curlCommand = "curl 'gopher://example.com'"
        assertEquals(
            "Error: Unrecognized option 'gopher://example.com'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_missingClosingQuoteInUrl() {
        val curlCommand = "curl 'http://example.com"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_incorrectUseOfSingleQuotes() {
        val curlCommand = "curl --request POST 'http://example.com' --data ''{'name':'John', 'age':30}''"
        assertEquals("Error: Unsupported data format.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_extraParameterAfterUrl() {
        val curlCommand = "curl 'http://example.com' unexpected"
        assertEquals(
            "Error: Unrecognized option 'unexpected'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_wrongOrderOfOptions() {
        val curlCommand = "curl --data '{\"name\": \"Kola\",\"age\": 50}' --request POST 'http://example.com'"
        assertEquals(null, CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    // Add feature to correct little mistakes like this in the input
    @Test
    fun testValidateCurlCommand_noSpaceBetweenOptions() {
        val curlCommand = "curl --data'{\"name\": \"Kola\",\"age\": 50}' --request POST 'http://example.com'"
        assertEquals(
            "Error: Unrecognized option '--data{\"name\": \"Kola\",\"age\": 50}'.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_dataOptionWithoutDash() {
        val curlCommand = "curl data 'data' --request POST 'http://example.com'"
        assertEquals("Error: Unrecognized option 'data'.", CurlValidator.validateCurlCommand(curlCommand).errorMessage)
    }

    @Test
    fun testValidateCurlCommand_incompleteCommand() {
        val curlCommand = "curl --request"
        assertEquals(
            "Error: No HTTP method specified.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withSingleQueryParam() {
        val curlCommand = "curl 'http://example.com?name=John'"
        assertEquals(
            null,
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withMultipleQueryParams() {
        val curlCommand = "curl 'http://example.com?name=John&age=30'"
        assertEquals(
            null,
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withEncodedQueryParams() {
        val curlCommand = "curl 'http://example.com?name=John%20Doe&age=30'"
        assertEquals(
            null,
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withEmptyQueryParamValue() {
        val curlCommand = "curl 'http://example.com?name='"
        assertEquals(
            null,
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withNoQueryParamValue() {
        val curlCommand = "curl 'http://example.com?name'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withSpecialCharactersInQueryParam() {
        val curlCommand = "curl 'http://example.com?name=John@Doe!&age=30'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withQueryParamsIncludingHash() {
        val curlCommand = "curl 'http://example.com?name=John#Doe&age=30'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withUnsupportedEncodedQueryParam() {
        val curlCommand = "curl 'http://example.com?name=John%ZZ'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    // Help user to remove space in the future
    @Test
    fun testValidateCurlCommand_queryParamWithSpace() {
        val curlCommand = "curl 'http://example.com?name=John Doe'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }

    @Test
    fun testValidateCurlCommand_withIllegalQueryParamKey() {
        val curlCommand = "curl 'http://example.com?na/me=John'"
        assertEquals(
            "Error: Invalid query parameter format.",
            CurlValidator.validateCurlCommand(curlCommand).errorMessage,
        )
    }


//    @Test
//    fun testValidateCurlCommand_withCookies() {
//        val curlCommand = "curl --cookie 'sessionToken=abc123' 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }
//
//    @Test
//    fun testValidateCurlCommand_withAuthentication() {
//        val curlCommand = "curl --user 'username:password' 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }
//
//    @Test
//    fun testValidateCurlCommand_withVerboseOption() {
//        val curlCommand = "curl --verbose 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }
//
//    @Test
//    fun testValidateCurlCommand_withSilentOption() {
//        val curlCommand = "curl --silent 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }

//    @Test
//    fun testValidateCurlCommand_withProxyOption() {
//        val curlCommand = "curl --proxy '192.168.1.1:8080' 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }
//
//    @Test
//    fun testValidateCurlCommand_withMaxTimeOption() {
//        val curlCommand = "curl --max-time 120 'http://example.com'"
//        assertEquals(null, Validator.validateCurlCommand(curlCommand).errorMessage)
//    }
}
