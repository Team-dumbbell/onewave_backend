package com.onewave.backend.controller;

import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.word.dto.WordResponse;
import com.onewave.backend.domain.word.service.VocabService;
import com.onewave.backend.exception.ApiResponse;
import com.onewave.backend.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Vocabulary", description = "단어장 API")
@RestController
@RequestMapping("/api/v1/vocab")
@RequiredArgsConstructor
public class VocabController {

    private final VocabService vocabService;
    private final UserRepository userRepository;

    @Operation(summary = "단어장 생성", description = "곡 ID를 기반으로 AI가 단어를 추출하여 저장합니다. 현재 토큰 이슈로 5개만 추출함")
    @PostMapping("/generate/{musicId}")
    public ResponseEntity<ApiResponse<Void>> generateVocab(
            @PathVariable Long musicId,
            Authentication authentication
    ) {
        String googleSub = authentication.getName();
        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        vocabService.extractAndSaveWords(musicId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("단어장이 생성되었습니다."));
    }

    @Operation(summary = "나의 단어장 조회", description = "로그인한 유저의 구글 ID를 기반으로 단어 목록을 가져옵니다.")
    @GetMapping("/list")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<WordResponse>>> getWordsByGoogleSub(Authentication authentication) {
        String googleSub = authentication.getName();
        User user = userRepository.findByGoogleSub(googleSub)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(ApiResponse.ok(vocabService.getWordsByUserId(user.getId())));
    }

    @Operation(summary = "단어 삭제", description = "단어 ID를 기반으로 내 단어장에서 단어를 삭제합니다.")
    @DeleteMapping("/{wordId}")
    public ResponseEntity<ApiResponse<Void>> deleteWord(
            @PathVariable Long wordId,
            Authentication authentication
    ) {
        String googleSub = authentication.getName();
        vocabService.deleteWord(wordId, googleSub);
        return ResponseEntity.ok(ApiResponse.ok("단어가 삭제되었습니다."));
    }
}
