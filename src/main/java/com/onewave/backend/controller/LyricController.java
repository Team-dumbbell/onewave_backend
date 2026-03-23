package com.onewave.backend.controller;


import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.dto.MusicSearchResponse;
import com.onewave.backend.domain.music.service.LyricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lyrics", description = "음악 검색 및 가사 조회 API")
@RestController
@RequestMapping("/api/v1/lyrics")
@RequiredArgsConstructor
public class LyricController {
    private final LyricService lyricsService;

    @Operation(summary = "노래 검색", description = "곡 제목이나 아티스트명으로 노래를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<MusicSearchResponse>> search(
            @Parameter(description = "검색어 (제목 또는 가수)", example = "Lemon")
            @RequestParam String q
    ) {
        return ResponseEntity.ok(lyricsService.searchSongs(q));
    }

    @Operation(summary = "가사 상세 조회", description = "곡의 고유 ID를 이용해 전체 가사와 곡 정보를 가져옵니다.")
    @GetMapping("/{id}")
    public ResponseEntity<LyricsResponse> getLyrics(
            @Parameter(description = "곡 ID", example = "101")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(lyricsService.getLyricsById(id));
    }
}
