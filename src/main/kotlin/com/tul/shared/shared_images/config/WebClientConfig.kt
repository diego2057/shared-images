package com.tul.shared.shared_images.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    fun getWebClientBuilder(): WebClient {
        return WebClient.builder().exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs { configurer: ClientCodecConfigurer ->
                    configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024)
                }
                .build()
        )
            .build()
    }
}
