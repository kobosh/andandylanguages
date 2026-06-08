package com.nubianlanguages.contentservice.repository;

import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordSentenceCollectionRepository extends JpaRepository<WordSentenceCollection,Long> {
}
