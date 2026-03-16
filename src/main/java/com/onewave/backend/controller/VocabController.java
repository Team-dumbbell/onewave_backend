package com.onewave.backend.controller;

import com.onewave.backend.domain.word.entity.Language;
import com.onewave.backend.domain.word.service.VocabService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vocab")
@RequiredArgsConstructor
public class VocabController {
    private final VocabService vocabService;

    @PostMapping("/generate/{musicId}")
    public ResponseEntity<String> generateVocab(@PathVariable Long musicId, @RequestParam Language lang) {
        // 임시로 userId 1번 사용 (시큐리티 적용 전)
        vocabService.extractAndSaveWords(musicId, 1L, lang);
        return ResponseEntity.ok("단어장이 생성되었습니다.");
    }
}
