package com.onewave.backend.domain.music.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.dto.MusicSearchResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class LyricService {

    private static final Duration LRCLIB_TIMEOUT = Duration.ofSeconds(5);

    private final MusicRepository musicRepository;
    private final WebClient webClient;

    public LyricService(
            MusicRepository musicRepository,
            WebClient.Builder webClientBuilder,
            @Value("${external.lrclib.base-url:https://lrclib.net/api}") String lrclibBaseUrl
    ) {
        this.musicRepository = musicRepository;
        this.webClient = webClientBuilder
                .baseUrl(lrclibBaseUrl)
                .build();
    }

    public List<MusicSearchResponse> searchSongs(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> rawResults = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query.trim())
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                })
                .timeout(LRCLIB_TIMEOUT)
                .onErrorMap(e -> new IllegalStateException("LRCLIB 가사 검색에 실패했습니다.", e))
                .block();

        if (rawResults == null) return Collections.emptyList();

        return rawResults.stream()
                .map(map -> MusicSearchResponse.builder()
                        .id(toLong(map.get("id")))
                        .trackName((String) map.get("trackName"))
                        .artistName((String) map.get("artistName"))
                        .albumName((String) map.get("albumName"))
                        .duration(toInt(map.get("duration")))
                        .instrumental(String.valueOf(map.get("instrumental")))
                        .build())
                .toList();
    }

    public LyricsResponse getLyricsById(Long lrclibId) {
        return musicRepository.findByLrclibId(lrclibId)
                .map(music -> LyricsResponse.builder()
                        .id(music.getLrclibId())
                        .trackName(music.getTrackName())
                        .artistName(music.getArtistName())
                        .plainLyrics(music.getContent())
                        .build())
                .orElseGet(() -> fetchAndSaveLyrics(lrclibId));
    }

    private LyricsResponse fetchAndSaveLyrics(Long lrclibId) {
        Map<String, Object> response = webClient.get()
                .uri("/get/{id}", lrclibId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .timeout(LRCLIB_TIMEOUT)
                .onErrorMap(e -> new IllegalStateException("LRCLIB 가사 조회에 실패했습니다.", e))
                .block();

        if (response == null) {
            throw new EntityNotFoundException("가사를 찾을 수 없습니다.");
        }

        String plainLyrics = (String) response.get("plainLyrics");
        Music music = musicRepository.save(Music.builder()
                .lrclibId(lrclibId)
                .trackName((String) response.get("trackName"))
                .artistName((String) response.get("artistName"))
                .content(plainLyrics)
                .build());

        return LyricsResponse.builder()
                .id(music.getLrclibId())
                .trackName(music.getTrackName())
                .artistName(music.getArtistName())
                .plainLyrics(music.getContent())
                .build();
    }

    private static Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static Integer toInt(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }
}
