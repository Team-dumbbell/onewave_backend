package com.onewave.backend.domain.word.dto;

import java.util.List;

public record WordExtractionResponse(
        List<WordItem> words
) {
    public record WordItem(
            String word,
            String meaning,
            List<String> examples,    // ✨ 단일 String에서 List로 변경
            String partOfSpeech,
            List<String> synonyms
    ) {}
}
