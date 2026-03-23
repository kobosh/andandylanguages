package com.nubianlanguages.pronounciationservice.service;




import com.nubianlanguages.pronounciationservice.dto.PhonemeFeedbackDto;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PhonemeService {

    private static final Map<String, List<String>> WORD_TO_PHONEMES = new HashMap<>();

    static {
        WORD_TO_PHONEMES.put("hello", List.of("HH", "AH", "L", "OW"));
        WORD_TO_PHONEMES.put("world", List.of("W", "ER", "L", "D"));
        WORD_TO_PHONEMES.put("cat", List.of("K", "AE", "T"));
        WORD_TO_PHONEMES.put("dog", List.of("D", "AO", "G"));
        WORD_TO_PHONEMES.put("water", List.of("W", "AO", "T", "ER"));
        WORD_TO_PHONEMES.put("name", List.of("N", "EY", "M"));
    }

    public List<PhonemeFeedbackDto> compare(String expectedText, String actualText) {
        List<String> expected = toPhonemes(expectedText);
        List<String> actual = toPhonemes(actualText);

        List<PhonemeFeedbackDto> feedback = new ArrayList<>();

        int max = Math.max(expected.size(), actual.size());

        for (int i = 0; i < max; i++) {
            String e = i < expected.size() ? expected.get(i) : null;
            String a = i < actual.size() ? actual.get(i) : null;

            if (Objects.equals(e, a)) {
                continue;
            }

            if (e != null && a == null) {
                feedback.add(new PhonemeFeedbackDto(e, null, "missing"));
            } else if (e == null) {
                feedback.add(new PhonemeFeedbackDto(null, a, "extra"));
            } else {
                feedback.add(new PhonemeFeedbackDto(e, a, "different"));
            }
        }

        return feedback;
    }

    public List<String> toPhonemes(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        List<String> phonemes = new ArrayList<>();
        String[] words = normalize(text).split("\\s+");

        for (String word : words) {
            List<String> mapped = WORD_TO_PHONEMES.get(word);
            if (mapped != null) {
                phonemes.addAll(mapped);
            } else {
                phonemes.addAll(guessPhonemes(word));
            }
        }

        return phonemes;
    }

    private String normalize(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
    }

    private List<String> guessPhonemes(String word) {
        List<String> guessed = new ArrayList<>();
        for (char c : word.toCharArray()) {
            guessed.add(String.valueOf(Character.toUpperCase(c)));
        }
        return guessed;
    }
}