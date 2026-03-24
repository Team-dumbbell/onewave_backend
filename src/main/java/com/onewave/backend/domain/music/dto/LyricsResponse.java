package com.onewave.backend.domain.music.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LyricsResponse {
    private Long id;
    private String trackName;
    private String artistName;
    private String plainLyrics;  // 가사 본문
    private String syncedLyrics; // (선택사항) 싱크 가사
}