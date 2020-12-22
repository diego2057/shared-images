package com.tul.shared.shared_images.service.tinify

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class TinifyClient(
    @Value("\${tinify.key}")
    private val TINIFY_KEY: String,
    @Value("\${aws.key-id}")
    private val AWS_KEY_ID: String,
    @Value("\${aws.secret-key}")
    private val AWS_SECRET_KEY: String,
    @Value("\${aws.region}")
    private val REGION: String,
    @Value("\${aws.bucket}")
    private val BUCKET_PATH: String
) {

    private val webClient = WebClient.builder()
        .baseUrl("https://api.tinify.com")
        .build()

    fun storeImage(url: String, fileName: String): Mono<String> {
        val options = mapOf(
            "service" to "s3",
            "aws_access_key_id" to AWS_KEY_ID,
            "aws_secret_access_key" to AWS_SECRET_KEY,
            "region" to REGION,
            "path" to "$BUCKET_PATH/$fileName"
        )

        return webClient
            .post()
            .uri(url)
            .header(HttpHeaders.AUTHORIZATION, getAuthHeader())
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(mapOf("store" to options)))
            .exchangeToMono {
                if (it.statusCode() == HttpStatus.OK) {
                    return@exchangeToMono Mono.just(it.headers().header("location")[0])
                } else {
                    return@exchangeToMono it.bodyToMono(String::class.java)
                }
            }
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
        return "basic " + Base64Utils.encodeToString(("api:$TINIFY_KEY").encodeToByteArray())
    }
}
