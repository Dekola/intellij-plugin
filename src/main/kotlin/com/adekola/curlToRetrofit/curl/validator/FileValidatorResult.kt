package com.adekola.curlToRetrofit.curl.validator

data class FileValidatorResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
)