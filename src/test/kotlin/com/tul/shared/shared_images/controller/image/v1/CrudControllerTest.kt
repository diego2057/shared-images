package com.tul.shared.shared_images.controller.image.v1

import com.tul.shared.shared_images.dto.image.v1.ImageDto
import com.tul.shared.shared_images.dto.image.v1.ImageRequest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.*
import kotlin.collections.ArrayList


@SpringBootTest
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient(timeout = "600000")
class CrudControllerTest {

    @Autowired
    private lateinit var client: WebTestClient


    @Test
    fun showImagesTest() {
        client.get()
                .uri("/v1/images")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .json("[]")
    }

    @Test
    fun createImageTest() {
        val imageRequest = ImageRequest()
        imageRequest.uuid = UUID.randomUUID().toString()
        imageRequest.title = "test"
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("file", ClassPathResource("test.png"),MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("request",imageRequest, MediaType.APPLICATION_JSON)


        client.post()
                .uri("/v1/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("uuid").isEqualTo(imageRequest.uuid!!)
                .jsonPath("file_name").isEqualTo("test.png")
    }


}
