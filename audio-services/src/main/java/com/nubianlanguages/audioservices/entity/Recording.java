package com.nubianlanguages.audioservices.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
/*public class Recording {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private String word;
    private String meaning;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordingType type;   // WORD or USAGE
    // 🔗 link USAGE → WORD
    private Integer parentRecordingId;
    @Column(nullable = false)
    private String objectKey;     // MinIO key

    //private Instant createdAt = Instant.now();
}*/

public class Recording {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        private Integer userId;

        private String word;

        private String meaning;


    @Column(nullable = false)
    private String objectKey;
    }