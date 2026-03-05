package com.onewave.backend.domain.music.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MusicSearchResponse {
    private Long id;            // LRCLIB 내부 ID
    private String trackName;   // 곡 제목
    private String artistName;  // 아티스트
    private String albumName;   // 앨범명
    private Integer duration;   // 재생 시간 (초)
    private String instrumental; // 연주곡 여부
}
