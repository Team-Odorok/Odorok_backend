package com.odorok.OdorokApplication.commons.webflux.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
@Slf4j
public class WebClientConfig {
    private static final String GPT_URL = "https://api.openai.com/v1/chat/completions";
    private static final String DURUNUBI_URL = "https://apis.data.go.kr";
    private static final String KAKAO_URL = "https://dapi.kakao.com";
//    private static final int DURUNUBI_CODE_IN_MEMORY_SIZE = 10 * 1024 * 1024;
//    private static final int KAKAO_CODE_IN_MEMORY_SIZE = 10 * 1024 * 1024;
    private static final int GPT_CODE_IN_MEMORY_SIZE = 20 * 1024;


    @Value("${external.keys.gpt}")
    private String GPT_KEY;

    // 나중에 builder 리팩토링 -> 키를 config 클래스로 옮겨서 세팅하도록 하자.
//    @Bean("durunubiClient")
//    public WebClient durunubiClient(WebClient.Builder builder) {
//        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(DURUNUBI_URL);
//        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY); // 또는 VALUES_ONLY
//        return builder.uriBuilderFactory(factory).codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(DURUNUBI_CODE_IN_MEMORY_SIZE)).build();
//    }

//    @Bean("kakaoClient")
//    public WebClient kakaoClient(WebClient.Builder builder) {
//        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(KAKAO_URL);
//        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY); // 또는 VALUES_ONLY
//        return builder.uriBuilderFactory(factory).codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(KAKAO_CODE_IN_MEMORY_SIZE)).build();
//    }

    @Bean("gptClient")
    public WebClient gptClient(WebClient.Builder builder) {
        return builder.baseUrl(GPT_URL).defaultHeaders(customizer -> {
            customizer.add("Content-Type", "application/json");
            customizer.add("Authorization", "Bearer "+GPT_KEY);
        }).codecs(config -> config.defaultCodecs().maxInMemorySize(GPT_CODE_IN_MEMORY_SIZE)).build(); // 최대 2메가
    }

    @Bean("normalClient")
    public WebClient normalClient(WebClient.Builder builder) {
        return builder.build(); // 최대 2메가
    }
}
