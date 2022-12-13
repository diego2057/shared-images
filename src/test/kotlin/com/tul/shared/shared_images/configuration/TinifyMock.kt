package com.tul.shared.shared_images.configuration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.binaryEqualTo
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class TinifyMock(private val port: Int) : WireMockServer(port) {

    fun startMockServer() {
        start()

        stubFor(
            WireMock.post("/shrink")
                .withRequestBody(binaryEqualTo(ClassPathResource("test.png").file.readBytes()))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(ClassPathResource("mock/tinify-api-shrink-response.json").file.readText())
                )
        )

        stubFor(
            WireMock.post("/shrink")
                .withRequestBody(binaryEqualTo(ClassPathResource("default.jpeg").file.readBytes()))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(ClassPathResource("mock/tinify-api-shrink-default-response.json").file.readText())
                )
        )

        stubFor(
            WireMock.post("/output/Th1s1s4t35t")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.LOCATION, "https://backoffice.tul.com.co/assets/images/logo-tul.png")
                        .withBody("{ status : success }")
                )
        )

        stubFor(
            WireMock.post("/output/default")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withHeader(HttpHeaders.LOCATION, "https://s3.us-east-2.amazonaws.com/images/default.jpeg")
                        .withBody("{ status : success }")
                )
        )
    }
}
