package com.nubianlanguages.contentservice.repository;

import com.nubianlanguages.contentservice.entity.LearnerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnerProgressRepository
        extends JpaRepository<LearnerProgress, Long> {

    Optional<LearnerProgress> findByLearnerEmailAndDialect(
            String learnerEmail,
            String dialect
    );
}
