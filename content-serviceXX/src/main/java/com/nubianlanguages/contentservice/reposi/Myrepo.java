package com.nubianlanguages.contentservice.reposi;

import com.nubianlanguages.contentservice.entity.WordSentenceCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Myrepo extends JpaRepository<WordSentenceCollection,Long> {
    Optional<WordSentenceCollection> findByEmail(String email);
}
