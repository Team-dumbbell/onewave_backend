package com.onewave.backend.domain.word.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String model = "gemini-2.0-flash";

    public AiService(@Value("${google.ai.api-key}") String apiKey, ObjectMapper objectMapper) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
        this.objectMapper = objectMapper;
    }

    public WordExtractionResponse extractWords(String lyrics, String language) {
        // 사용자가 작성한 프롬프트 적용
        String prompt = String.format("""
            당신은 유능한 언어 튜터입니다. 
            제공된 가사에서 학습하기 좋은 주요 단어 5개를 추출하세요.
            언어 설정: %s
            
            출력은 반드시 마크다운 코드 블록 없이 순수 JSON 형식을 지켜주세요:
            {
              "words": [
                {
                  "word": "단어",
                  "meaning": "뜻(한국어)",
                  "example": "가사의 맥락을 반영한 예문",
                  "partOfSpeech": "품사",
                  "synonyms": ["유의어1", "유의어2"]
                }
              ]
            }
            
            가사 내용:
            %s
            """, language, lyrics);

        try {
            GenerateContentResponse response = client.models.generateContent(model, prompt, null);
            String jsonContent = response.text();

            // AI가 간혹 포함하는 ```json ... ``` 태그 제거 로직 (방어 코드)
            jsonContent = jsonContent.replaceAll("```json|```", "").trim();

            return objectMapper.readValue(jsonContent, WordExtractionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI 단어 추출 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}