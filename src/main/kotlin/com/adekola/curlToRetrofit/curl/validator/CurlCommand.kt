package com.adekola.curlToRetrofit.curl.validator

data class CurlCommand(
    val method: String?,
    val url: String?,
    val headers: Map<String, String>,
    val data: String?,
    val queryParams: Map<String, String>?,
    val options: List<String>
)