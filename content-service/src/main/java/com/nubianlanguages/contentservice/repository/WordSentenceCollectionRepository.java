package com.nubianlanguages.contentservice.repository;

import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WordSentenceCollectionRepository extends JpaRepository<WordSentenceCollection,Long> {
    @Query("""
    SELECT w
    FROM WordSentenceCollection w
    WHERE w.id BETWEEN :start AND :end
    ORDER BY w.id
""")
    List<WordSentenceCollection> findByIdRange(
            @Param("start") Long start,
            @Param("end") Long end
    );
    List<WordSentenceCollection> findByIdGreaterThanOrderByIdAsc(Long id, Pageable pageable);
}
