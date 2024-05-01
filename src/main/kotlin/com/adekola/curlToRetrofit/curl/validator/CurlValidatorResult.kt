package com.adekola.curlToRetrofit.curl.validator

data class CurlValidatorResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
