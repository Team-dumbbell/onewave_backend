package com.onewave.backend.controller;


import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.dto.MusicSearchResponse;
import com.onewave.backend.domain.music.service.LyricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/lyrics")
@RequiredArgsConstructor
public class LyricController {
    private final LyricService lyricsService;

    @GetMapping("/search")
    public ResponseEntity<List<MusicSearchResponse>> search(@RequestParam String q) {
        return ResponseEntity.ok(lyricsService.searchSongs(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LyricsResponse> getLyrics(@PathVariable Long id) {
        return ResponseEntity.ok(lyricsService.getLyricsById(id));
    }
}
