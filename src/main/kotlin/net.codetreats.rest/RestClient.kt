package net.codetreats.rest

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.lang.IllegalStateException

data class Response(val code: Int, val message: String?)

data class StatusCodeRange(val fromInclusive: Int, val toExclusive: Int)

class RestClient(
    private val baseUrl: String,
    private val defaultHeaders: Map<String, String> = emptyMap(),
    private val allowedStatusCodes: StatusCodeRange = StatusCodeRange(200, 300)
) {
    fun get(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ) = request(HttpMethod.Get, url, params, headers, null)

    fun post(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: String?
    ) = request(HttpMethod.Post, url, params, headers, body)

    fun put(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: String?
    ) = request(HttpMethod.Put, url, params, headers, body)

    fun delete(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap()
    ) = request(HttpMethod.Delete, url, params, headers, null)

    private fun request(
        method: HttpMethod,
        url: String,
        params: Map<String, String>,
        headers: Map<String, String>,
        body: String?
    ): Response = runBlocking {
        val client = HttpClient(CIO)
        val answer = client.request {
            val builder = this
            builder.method = method
            builder.url(baseUrl + url)
            builder.headers {
                defaultHeaders.forEach { (k, v) -> append(k, v) }
                headers.forEach { (k, v) -> append(k, v) }
                if (body != null) {
                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                }
            }
            params.forEach { (k, v) -> builder.parameter(k, v) }
            body?.let {
                builder.setBody(TextContent(body, ContentType.Application.Json))
            }
        }
        val statusCode = answer.status.value
        val text = answer.bodyAsText()
        if (allowedStatusCodes.fromInclusive <= statusCode && statusCode < allowedStatusCodes.toExclusive) {
            return@runBlocking Response(statusCode, text)
        } else {
            throw IllegalStateException("Received unexpected status code: $statusCode, $text")
        }
    }
}
