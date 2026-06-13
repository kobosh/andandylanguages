package com.nubianlanguages.contentservice.repository;

import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordSentenceCollectionRepository extends JpaRepository<WordSentenceCollection,Long> {

    List<WordSentenceCollection> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}
