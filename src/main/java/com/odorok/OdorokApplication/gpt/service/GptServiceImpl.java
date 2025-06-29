package com.odorok.OdorokApplication.gpt.service;

import com.odorok.OdorokApplication.commons.exception.GptCommunicationException;
import com.odorok.OdorokApplication.gpt.dto.request.GptRequest;
import com.odorok.OdorokApplication.gpt.dto.response.GptResponse;
import com.odorok.OdorokApplication.gpt.dto.response.nested.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GptServiceImpl implements GptService{
    private final WebClient client;
    private final String MODEL;
    private final Double TEMPERATURE;
    public GptServiceImpl(@Autowired @Qualifier("gptClient") WebClient client, @Value("${gpt.model}") String model, @Value("${gpt.temperature}") double temperature) {
        this.client = client;
        this.MODEL = model;
        this.TEMPERATURE = temperature;
    }

    @Override
    public List<Prompt> sendPrompt(List<Prompt> context, Prompt prompt) {
        // context를 조각내어 여러 개의 Prompt로 만든다?
        // prompt 를 context에 붙인다.
        List<Prompt> fullCtx;
        if(context != null) fullCtx = new ArrayList<>(context);
        else fullCtx = new ArrayList<>();

        fullCtx.add(prompt);
        GptRequest requestObj = new GptRequest(MODEL, fullCtx, TEMPERATURE);
        Mono<GptResponse> responseMono = client.post().bodyValue(requestObj).retrieve().bodyToMono(GptResponse.class);
        GptResponse response = responseMono.block();
        if(response == null) {
            throw new GptCommunicationException("no response from gpt server");
//            throw new RuntimeException("no response from gpt server");
        }
        log.debug("gpt response: {}", response);
        Message message = response.getChoices().get(0).getMessage();
        fullCtx.add(new Prompt(message.getRole(), message.getContent()));

        return fullCtx;
    }
}
