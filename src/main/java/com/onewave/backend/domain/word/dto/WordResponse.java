package com.onewave.backend.domain.word.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WordResponse {
    private Long id;
    private String word;
    private String meaning;      // 유저 선호 언어로 저장된 의미
    private String partOfSpeech;
    private String example;
    private String musicTitle;   // 어떤 노래에서 뽑았는지 알면 좋겠죠?
    private String artist;
}
