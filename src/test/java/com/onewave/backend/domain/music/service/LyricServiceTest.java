package com.onewave.backend.domain.music.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.dto.MusicSearchResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LyricServiceTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void searchSongsReturnsEmptyListForBlankQuery() {
        MusicRepository musicRepository = mock(MusicRepository.class);
        LyricService lyricService = new LyricService(
                musicRepository,
                WebClient.builder(),
                "http://localhost:1"
        );

        List<MusicSearchResponse> result = lyricService.searchSongs("   ");

        assertThat(result).isEmpty();
        verifyNoInteractions(musicRepository);
    }

    @Test
    void searchSongsMapsLrclibResponse() throws IOException {
        AtomicReference<String> query = new AtomicReference<>();
        startServer("/api/search", """
                [
                  {
                    "id": 123,
                    "trackName": "Lemon",
                    "artistName": "Kenshi Yonezu",
                    "albumName": "Lemon",
                    "duration": 256,
                    "instrumental": false
                  }
                ]
                """, query);

        LyricService lyricService = new LyricService(
                mock(MusicRepository.class),
                WebClient.builder(),
                baseUrl()
        );

        List<MusicSearchResponse> result = lyricService.searchSongs(" Lemon ");

        assertThat(query.get()).isEqualTo("q=Lemon");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(123L);
        assertThat(result.get(0).getTrackName()).isEqualTo("Lemon");
        assertThat(result.get(0).getArtistName()).isEqualTo("Kenshi Yonezu");
        assertThat(result.get(0).getDuration()).isEqualTo(256);
    }

    @Test
    void getLyricsByIdFetchesAndSavesWhenNotCached() throws IOException {
        startServer("/api/get/777", """
                {
                  "trackName": "Song",
                  "artistName": "Artist",
                  "plainLyrics": "lyrics"
                }
                """, new AtomicReference<>());

        MusicRepository musicRepository = mock(MusicRepository.class);
        when(musicRepository.findByLrclibId(777L)).thenReturn(Optional.empty());
        when(musicRepository.save(any(Music.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LyricService lyricService = new LyricService(
                musicRepository,
                WebClient.builder(),
                baseUrl()
        );

        LyricsResponse result = lyricService.getLyricsById(777L);

        assertThat(result.getId()).isEqualTo(777L);
        assertThat(result.getTrackName()).isEqualTo("Song");
        assertThat(result.getArtistName()).isEqualTo("Artist");
        assertThat(result.getPlainLyrics()).isEqualTo("lyrics");

        ArgumentCaptor<Music> captor = ArgumentCaptor.forClass(Music.class);
        verify(musicRepository).save(captor.capture());
        assertThat(captor.getValue().getLrclibId()).isEqualTo(777L);
        assertThat(captor.getValue().getContent()).isEqualTo("lyrics");
    }

    private void startServer(String path, String body, AtomicReference<String> query) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext(path, exchange -> {
            query.set(exchange.getRequestURI().getQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/api";
    }
}
