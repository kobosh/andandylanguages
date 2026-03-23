package com.nubianlanguages.pronounciationservice.repository;

import com.nubianlanguages.pronounciationservice.entity.PronunciationAssessmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PronunciationAssessmentRepository
        extends JpaRepository<PronunciationAssessmentEntity, Long> {

    List<PronunciationAssessmentEntity> findByRecordingIdOrderByCreatedAtDesc(Long recordingId);
}