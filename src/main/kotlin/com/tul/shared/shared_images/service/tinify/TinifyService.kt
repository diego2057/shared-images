package com.tul.shared.shared_images.service.tinify

import com.fasterxml.jackson.databind.JsonNode
import io.netty.handler.logging.LogLevel
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Component
class TinifyService(
    @Value("\${app.tinify.key}")
    private val tinifyKey: String,
    @Value("\${app.tinify.url}")
    private val tinifyUrl: String,
    @Value("\${app.aws.key-id}")
    private val awsKeyId: String,
    @Value("\${app.aws.secret-key}")
    private val awsSecretKey: String,
    @Value("\${app.aws.region}")
    private val region: String,
    @Value("\${app.aws.bucket}")
    private val bucketPath: String
) {

    private val webClient = WebClient.builder()
        .clientConnector(
            ReactorClientHttpConnector(
                HttpClient.create().wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
            )
        )
        .baseUrl(tinifyUrl)
        .build()

    fun storeImage(url: String, s3Name: String): Mono<String> {
        val options = mapOf(
            "service" to "s3",
            "aws_access_key_id" to awsKeyId,
            "aws_secret_access_key" to awsSecretKey,
            "region" to region,
            "path" to "$bucketPath/$s3Name"
        )

        return webClient
            .post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(mapOf("store" to options)))
            .exchangeToMono { Mono.just(it.headers().header("location")[0]) }
    }

    fun compressImage(byteArray: ByteArray): Mono<JsonNode> {
        return webClient
            .post()
            .uri("/shrink")
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .bodyValue(byteArray)
            .retrieve()
            .bodyToMono(JsonNode::class.java)
    }

    private fun getAuthHeader(): String {
        return "basic " + Base64Utils.encodeToString(("api:$tinifyKey").encodeToByteArray())
    }
}
