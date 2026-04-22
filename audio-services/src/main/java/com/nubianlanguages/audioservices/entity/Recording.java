package com.nubianlanguages.audioservices.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recording")
public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;
    @Column(nullable = false)
    private String authorName="some user";
    @Column(nullable = false)
    private String word;

    @Column(nullable = false)
    private String meaning;

    private String sentence;
    private String sentenceMeaning;

    // Must be nullable because first save happens before upload
    @Column(nullable = true)
    private String wordObjectKey;

    @Column(nullable = true)
    private String sentenceObjectKey;

    @Column(nullable = false)
    private boolean wordUploaded = false;

    @Column(nullable = false)
    private boolean sentenceUploaded = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Dialect dialect = Dialect.DONGOLAWI;
}