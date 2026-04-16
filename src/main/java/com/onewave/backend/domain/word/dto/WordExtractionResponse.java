package com.onewave.backend.domain.word.dto;

import com.onewave.backend.domain.word.entity.Language;

import java.util.List;

public record WordExtractionResponse(
        List<WordItem> words
) {
    public record WordItem(
            String word,
            String meaning,
            String examples,
            String partOfSpeech,
            List<String> synonyms,
            Language language
    ) {}
}
