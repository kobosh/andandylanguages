package com.nubianlanguages.audioservices.service;

import org.springframework.stereotype.Service;

@Service
public class TextComparisonService {

    public String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^\\p{L}\\p{Nd}\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public double similarityPercent(String expected, String actual) {
        expected = normalize(expected);
        actual = normalize(actual);

        if (expected.isBlank() && actual.isBlank()) return 100.0;
        if (expected.isBlank() || actual.isBlank()) return 0.0;

        int distance = levenshtein(expected, actual);
        int maxLen = Math.max(expected.length(), actual.length());

        return Math.max(0.0, (1.0 - ((double) distance / maxLen)) * 100.0);
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[a.length()][b.length()];
    }

    public String rating(double score) {
        if (score >= 90) return "Excellent";
        if (score >= 75) return "Good";
        if (score >= 55) return "Fair";
        return "Needs practice";
    }
}