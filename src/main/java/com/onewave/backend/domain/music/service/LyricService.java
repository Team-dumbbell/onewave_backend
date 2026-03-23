package com.onewave.backend.domain.music.service;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.dto.MusicSearchResponse;
import com.onewave.backend.domain.music.repository.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LyricService {

    private final MusicRepository musicRepository;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://lrclib.net/api")
            .build();


    public List<MusicSearchResponse> searchSongs(String query) {
        List<Map<String, Object>> rawResults = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .block();

        if (rawResults == null) return Collections.emptyList();

        // 2. 가사를 제외하고 DTO로 변환
        return rawResults.stream()
                .map(map -> MusicSearchResponse.builder()
                        .id(((Number) map.get("id")).longValue())
                        .trackName((String) map.get("trackName"))
                        .artistName((String) map.get("artistName"))
                        .albumName((String) map.get("albumName"))
                        .duration(map.get("duration") != null ? ((Number) map.get("duration")).intValue() : 0)
                        .instrumental(String.valueOf(map.get("instrumental")))
                        .build())
                .collect(Collectors.toList());
    }

    public LyricsResponse getLyricsById(Long lrclibId) {
        // 1. 먼저 DB에 해당 lrclibId로 저장된 곡이 있는지 확인
        return musicRepository.findByLrclibId(lrclibId)
                .map(music -> LyricsResponse.builder()
                        .id(music.getLrclibId())
                        .trackName(music.getTrackName())
                        .artistName(music.getArtistName())
                        .plainLyrics(music.getContent())
                        .build())
                // 2. DB에 없으면 외부 API 호출
                .orElseGet(() -> {
                    Map<String, Object> response = webClient.get()
                            .uri("/get/{id}", lrclibId)
                            .retrieve()
                            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                            .block();

                    if (response == null) throw new RuntimeException("가사를 찾을 수 없습니다.");

                    String plainLyrics = (String) response.get("plainLyrics");

                    // 3. API 결과를 DB에 저장 (영속화)
                    musicRepository.save(Music.builder()
                            .lrclibId(lrclibId)
                            .trackName((String) response.get("trackName"))
                            .artistName((String) response.get("artistName"))
                            .content(plainLyrics)
                            .build());

                    return LyricsResponse.builder()
                            .id(lrclibId)
                            .trackName((String) response.get("trackName"))
                            .artistName((String) response.get("artistName"))
                            .plainLyrics(plainLyrics)
                            .build();
                });
    }
}
