package com.onewave.backend.domain.word.dto;

import java.util.List;

public record WordExtractionResponse(
        List<WordItem> words
) {
    public record WordItem(
            String word,
            String meaning,
            String example,
            String partOfSpeech,
            List<String> synonyms
    ) {}
}
