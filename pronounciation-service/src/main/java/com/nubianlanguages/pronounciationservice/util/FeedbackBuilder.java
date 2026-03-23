package com.nubianlanguages.pronounciationservice.util;

import java.util.ArrayList;
import java.util.List;

public final class FeedbackBuilder {

    private FeedbackBuilder() {
    }

    public static List<String> build(
            Double overall,
            Double accuracy,
            Double fluency,
            Double completeness,
            Double prosody
    ) {
        List<String> feedback = new ArrayList<>();

        if (overall != null) {
            if (overall >= 90) {
                feedback.add("Excellent pronunciation.");
            } else if (overall >= 75) {
                feedback.add("Good pronunciation overall.");
            } else if (overall >= 60) {
                feedback.add("Fair pronunciation. More practice will help.");
            } else {
                feedback.add("Needs improvement. Try listening and repeating more slowly.");
            }
        }

        if (accuracy != null && accuracy < 70) {
            feedback.add("Some sounds were not pronounced clearly.");
        }

        if (fluency != null && fluency < 70) {
            feedback.add("Try saying the word or sentence more smoothly.");
        }

        if (completeness != null && completeness < 100) {
            feedback.add("Some expected words may be missing.");
        }

        if (prosody != null && prosody < 70) {
            feedback.add("Work on rhythm, stress, and natural speech flow.");
        }

        if (feedback.isEmpty()) {
            feedback.add("Keep practicing.");
        }

        return feedback;
    }
}