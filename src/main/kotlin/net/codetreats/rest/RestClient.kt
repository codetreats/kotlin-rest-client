package net.codetreats.rest

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.IllegalStateException
import java.nio.file.Files
import java.time.Duration

/**
 * The response of a request
 * @param code the status code
 * @param message the message body if available
 */
data class Response(val code: Int, val message: String, val headers: Map<String, List<String>> = emptyMap())

/**
 * A status code range
 * @param fromInclusive from (including)
 * @param toExclusive to (not included)
 */
data class StatusCodeRange(val fromInclusive: Int, val toExclusive: Int)

/**
 * The Rest client itself
 * @param baseUrl the base url of the API it should connect to (aka the prefix which is added to all requests)
 * @param defaultHeaders headers which are added to each request (e.g. authorization)
 * @param allowedStatusCodes the code range for which the message should be returned. If the result is not in this range, an exception will be thrown
 * @param client the HttpClient instance to use for making requests
 */
class RestClient(
    val baseUrl: String,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val allowedStatusCodes: StatusCodeRange = StatusCodeRange(200, 300),
    val client: HttpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = Duration.ofSeconds(60).toMillis()
            connectTimeoutMillis = Duration.ofSeconds(30).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(60).toMillis()
        }
    },
) {
    fun get(url: String, params: Map<String, String> = emptyMap(), headers: Map<String, String> = emptyMap()) =
        request(HttpMethod.Get, url, params, headers, null)

    fun post(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: String?,
        contentType: ContentType = ContentType.Application.Json,
    ) = request(HttpMethod.Post, url, params, headers, body, contentType)

    fun put(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        body: String?,
        contentType: ContentType = ContentType.Application.Json,
    ) = request(HttpMethod.Put, url, params, headers, body, contentType)

    fun delete(url: String, params: Map<String, String> = emptyMap(), headers: Map<String, String> = emptyMap()) =
        request(HttpMethod.Delete, url, params, headers, null)

    fun postFile(
        url: String,
        params: Map<String, String> = emptyMap(),
        headers: Map<String, String> = emptyMap(),
        key: String,
        file: File,
    ): Response = runBlocking {
        val fullUrl = baseUrl + url
        try {
            val contentType = Files.probeContentType(file.toPath())
                ?.let { ContentType.parse(it) }
                ?: ContentType.Application.OctetStream
            val answer: HttpResponse = client.submitFormWithBinaryData(
                url = fullUrl,
                formData = formData {
                    append(
                        key = key,
                        value = file.readBytes(),
                        headers = Headers.build {
                            append(
                                HttpHeaders.ContentDisposition,
                                "form-data; name=\"file\"; filename=\"${file.name}\"",
                            )
                            append(HttpHeaders.ContentType, contentType.toString())
                        },
                    )
                },
            ) {
                val builder = this
                builder.method = method
                builder.headers {
                    defaultHeaders.forEach { (k, v) -> append(k, v) }
                    headers.forEach { (k, v) -> append(k, v) }
                }
                params.forEach { (k, v) -> builder.parameter(k, v) }
            }
            val statusCode = answer.status.value
            val text = answer.bodyAsText()
            val responseHeaders = answer.headers.entries().associate { it.key to it.value }
            if (allowedStatusCodes.fromInclusive <= statusCode && statusCode < allowedStatusCodes.toExclusive) {
                return@runBlocking Response(statusCode, text, responseHeaders)
            } else {
                throw IllegalStateException("Received unexpected status code: $statusCode, $text")
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Cannot connect to $fullUrl: ${e.message} " +
                    "(Params: $params, Headers: $headers, Body: ${file.absolutePath})",
            )
        }
    }

    private fun request(
        method: HttpMethod,
        url: String,
        params: Map<String, String>,
        headers: Map<String, String>,
        body: String?,
        contentType: ContentType = ContentType.Application.Json,
    ): Response = runBlocking {
        val fullUrl = baseUrl + url
        try {
            val answer = client.request {
                val builder = this
                builder.method = method
                builder.url(fullUrl)
                builder.headers {
                    defaultHeaders.forEach { (k, v) -> append(k, v) }
                    headers.forEach { (k, v) -> append(k, v) }
                    if (body != null) {
                        append(HttpHeaders.ContentType, contentType.toString())
                    }
                }
                params.forEach { (k, v) -> builder.parameter(k, v) }
                body?.let {
                    builder.setBody(TextContent(body, contentType))
                }
            }
            val statusCode = answer.status.value
            val text = answer.bodyAsText()
            val responseHeaders = answer.headers.entries().associate { it.key to it.value }
            if (allowedStatusCodes.fromInclusive <= statusCode && statusCode < allowedStatusCodes.toExclusive) {
                return@runBlocking Response(statusCode, text, responseHeaders)
            } else {
                throw IllegalStateException("Received unexpected status code: $statusCode, $text")
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Cannot connect to $fullUrl: ${e.message} " +
                    "(Params: $params, Headers: $headers, Body-Length: ${body?.length})",
            )
        }
    }
}
