package com.onewave.backend.domain.word.service;

import com.onewave.backend.domain.music.dto.LyricsResponse;
import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.music.service.LyricService;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.word.dto.WordExtractionResponse;
import com.onewave.backend.domain.word.entity.Language;
import com.onewave.backend.domain.word.entity.Word;
import com.onewave.backend.domain.word.repository.MusicWordRepository;
import com.onewave.backend.domain.word.repository.UserWordRepository;
import com.onewave.backend.domain.word.repository.WordRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VocabServiceTest {

    @Test
    void extractAndSaveWordsSkipsInvalidAiWordItems() {
        AiService aiService = mock(AiService.class);
        LyricService lyricService = mock(LyricService.class);
        WordRepository wordRepository = mock(WordRepository.class);
        MusicRepository musicRepository = mock(MusicRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MusicWordRepository musicWordRepository = mock(MusicWordRepository.class);
        UserWordRepository userWordRepository = mock(UserWordRepository.class);
        VocabService service = new VocabService(
                aiService,
                lyricService,
                wordRepository,
                musicRepository,
                userRepository,
                musicWordRepository,
                userWordRepository
        );

        when(lyricService.getLyricsById(1L)).thenReturn(LyricsResponse.builder()
                .id(1L)
                .plainLyrics("lyrics")
                .build());
        when(musicRepository.findByLrclibId(1L)).thenReturn(Optional.of(Music.builder()
                .lrclibId(1L)
                .trackName("Track")
                .artistName("Artist")
                .content("lyrics")
                .build()));
        when(userRepository.findById(10L)).thenReturn(Optional.of(User.create("user@example.com", "sub", "User")));
        when(aiService.extractWords("lyrics", Language.KOREAN)).thenReturn(new WordExtractionResponse(List.of(
                new WordExtractionResponse.WordItem("   ", "meaning", null, "noun", null, Language.ENGLISH)
        )));

        List<Word> result = service.extractAndSaveWords(1L, 10L);

        assertThat(result).isEmpty();
        verify(wordRepository, never()).save(org.mockito.ArgumentMatchers.any(Word.class));
    }
}
