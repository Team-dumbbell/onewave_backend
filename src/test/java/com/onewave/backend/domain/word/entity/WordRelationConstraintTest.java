package com.onewave.backend.domain.word.entity;

import com.onewave.backend.domain.music.entity.Music;
import com.onewave.backend.domain.music.repository.MusicRepository;
import com.onewave.backend.domain.user.UserRepository;
import com.onewave.backend.domain.user.entity.User;
import com.onewave.backend.domain.word.repository.MusicWordRepository;
import com.onewave.backend.domain.word.repository.UserWordRepository;
import com.onewave.backend.domain.word.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WordRelationConstraintTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordRepository wordRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private UserWordRepository userWordRepository;

    @Autowired
    private MusicWordRepository musicWordRepository;

    @Test
    void wordTextIsUnique() {
        wordRepository.saveAndFlush(newWord("wave"));

        assertThatThrownBy(() -> wordRepository.saveAndFlush(newWord("wave")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void lrclibIdIsUnique() {
        musicRepository.saveAndFlush(newMusic(100L));

        assertThatThrownBy(() -> musicRepository.saveAndFlush(newMusic(100L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void userWordPairIsUnique() {
        User user = userRepository.save(User.create("user@example.com", "google-sub-1", "User"));
        Word word = wordRepository.save(newWord("ocean"));
        userWordRepository.saveAndFlush(UserWord.builder().user(user).word(word).meaning("sea").build());

        assertThatThrownBy(() -> userWordRepository.saveAndFlush(UserWord.builder().user(user).word(word).meaning("sea").build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void musicWordPairIsUnique() {
        Music music = musicRepository.save(newMusic(200L));
        Word word = wordRepository.save(newWord("flow"));
        musicWordRepository.saveAndFlush(new MusicWord(music, word));

        assertThatThrownBy(() -> musicWordRepository.saveAndFlush(new MusicWord(music, word)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static Word newWord(String value) {
        return Word.builder()
                .word(value)
                .partOfSpeech("noun")
                .language(Language.ENGLISH)
                .build();
    }

    private static Music newMusic(Long lrclibId) {
        return Music.builder()
                .lrclibId(lrclibId)
                .trackName("Track " + lrclibId)
                .artistName("Artist")
                .content("Lyrics")
                .build();
    }
}
