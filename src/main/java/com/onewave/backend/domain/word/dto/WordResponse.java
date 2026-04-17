package com.onewave.backend.domain.word.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class WordResponse {
    private Long id;
    private String word;
    private String meaning;
    private String partOfSpeech;
    private int frequency;           // ✨ 얼마나 자주 등장했는지 추가

    private List<String> examples;   // ✨ 여러 노래의 예문들
    private List<String> synonyms;   // ✨ 유의어들

    // ✨ 단일 곡 정보에서 곡 리스트 정보로 변경
    private List<MusicInfo> musicList;

    @Getter
    @Builder
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class MusicInfo {
        private String title;
        private String artist;
    }
}
