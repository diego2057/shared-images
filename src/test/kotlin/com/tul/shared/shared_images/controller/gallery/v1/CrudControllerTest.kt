package com.tul.shared.shared_images.controller.gallery.v1

import com.tul.shared.shared_images.configuration.TestConfiguration
import com.tul.shared.shared_images.configuration.TinifyMock
import com.tul.shared.shared_images.dto.gallery.v1.GalleryDto
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import java.util.UUID

@SpringBootTest(classes = [TestConfiguration::class])
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CrudControllerTest {
    @Autowired
    private lateinit var client: WebTestClient

    private var tinifyMock = TinifyMock(8090)

    @BeforeAll
    fun loadMock() {
        tinifyMock.startMockServer()
    }

    @AfterAll
    fun shutDownMock() {
        tinifyMock.stop()
    }

    @Test
    fun showGalleryTest() {
        var galleryDtoArray = client.get()
            .uri("/v1/galleries")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<GalleryDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(galleryDtoArray.size, 0)

        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", ClassPathResource("test.png"), MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[0].title", "test")
        bodyBuilder.part("images[0].uuid", UUID.randomUUID().toString())
        bodyBuilder.part("uuid", UUID.randomUUID().toString())

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        galleryDtoArray = client.get()
            .uri("/v1/galleries")
            .exchange()
            .expectStatus().isOk
            .expectBody(Array<GalleryDto>::class.java)
            .returnResult().responseBody!!

        Assertions.assertEquals(galleryDtoArray.size, 1)
    }

    @Test
    fun createGalleryTest() {
        val uuid = UUID.randomUUID().toString()
        val imageTitle = "test"
        val imageUuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[0].title", imageTitle)
        bodyBuilder.part("images[0].uuid", imageUuid)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isEqualTo(imageUuid)
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }

    @Test
    fun findGalleryByIdTest() {
        val uuid = UUID.randomUUID().toString()
        val imageTitle = "test"
        val imageUuid = UUID.randomUUID().toString()
        val file = ClassPathResource("test.png")
        val bodyBuilder = MultipartBodyBuilder()
        bodyBuilder.part("images[0].image", file, MediaType.MULTIPART_FORM_DATA)
        bodyBuilder.part("images[0].title", imageTitle)
        bodyBuilder.part("images[0].uuid", imageUuid)
        bodyBuilder.part("uuid", uuid)

        client.post()
            .uri("/v1/galleries")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
            .exchange()
            .expectStatus().isOk

        client.get()
            .uri("/v1/galleries/$uuid")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("uuid").isEqualTo(uuid)
            .jsonPath("images[0].uuid").isEqualTo(imageUuid)
            .jsonPath("images[0].file_name").isEqualTo(file.filename!!)
    }
}
