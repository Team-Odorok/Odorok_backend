package com.odorok.OdorokApplication.diary.dto.request;

import com.odorok.OdorokApplication.gpt.service.GptService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiaryRegenerationRequest {
    private String feedback;
    private List<GptService.Prompt> chatLog;
}
