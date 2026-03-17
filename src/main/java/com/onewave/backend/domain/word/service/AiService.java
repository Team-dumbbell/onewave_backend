package com.onewave.backend.domain.word.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private final Client client;
    private final ObjectMapper objectMapper;
    // 1. 모델명을 2.0으로 변경 (혹은 1.5-flash 유지 시 models/ 접두어 확인)
    private final String model = "gemini-2.5-flash";

    public AiService(@Value("${google.ai.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
        this.objectMapper = objectMapper;
    }

    public WordExtractionResponse extractWords(String lyrics, String language) {
        String prompt = String.format("""
            제공된 가사에서 %s 학습을 위한 주요 단어 5개를 추출하세요.
            반드시 다음 JSON 구조를 따르세요:
            {
              "words": [
                {
                  "word": "단어",
                  "meaning": "뜻(한국어)",
                  "example": "예문",
                  "partOfSpeech": "품사",
                  "synonyms": ["유의어1"]
                }
              ]
            }
            
            가사: %s
            """, language, lyrics);

        try {
            // 2. JSON 응답을 강제하는 Config 설정
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();

            // 3. 모델 호출 (config 추가)
            GenerateContentResponse response = client.models.generateContent(model, prompt, config);
            String jsonContent = response.text();

            // JSON 직렬화 테스트 및 반환
            return objectMapper.readValue(jsonContent, WordExtractionResponse.class);
        } catch (Exception e) {
            // 404 에러 발생 시 로그를 통해 모델명을 한 번 더 확인하세요.
            throw new RuntimeException("AI 단어 추출 오류 [" + model + "]: " + e.getMessage());
        }
    }
}