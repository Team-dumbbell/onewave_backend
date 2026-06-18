package com.onewave.backend.domain.word.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.entity.Language;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private final Client client;
    private final ObjectMapper objectMapper;
    private final String model;

    public AiService(
            @Value("${google.ai.api-key}") String apiKey,
            @Value("${google.ai.model:gemini-2.0-flash}") String model,
            ObjectMapper objectMapper
    ) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
        this.model = model;
        this.objectMapper = objectMapper;
    }

    public WordExtractionResponse extractWords(String lyrics, Language language) {
        String prompt = String.format("""
            Extract 5 important vocabulary words from the following lyrics for language learning.
            Write each meaning in %s.
            Return only JSON matching this structure:
            {
              "words": [
                {
                  "word": "word",
                  "meaning": "meaning",
                  "examples": "example sentence",
                  "partOfSpeech": "part of speech",
                  "synonyms": ["synonym1", "synonym2"],
                  "language": "one of KOREAN, ENGLISH, JAPANESE"
                }
              ]
            }

            Lyrics:
            %s
            """, language, lyrics);

        try {
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseMimeType("application/json")
                    .build();

            GenerateContentResponse response = client.models.generateContent(model, prompt, config);
            String jsonContent = response.text();
            if (jsonContent == null || jsonContent.isBlank()) {
                throw new IllegalStateException("AI returned empty response");
            }

            return objectMapper.readValue(jsonContent, WordExtractionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("AI word extraction failed [" + model + "]: " + e.getMessage(), e);
        }
    }
}
