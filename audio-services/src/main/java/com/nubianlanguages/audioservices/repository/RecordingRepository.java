package com.nubianlanguages.audioservices.repository;

import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.entity.RecordingType;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


//package com.nubianlanguages.audioservices.repository;
//
//import com.nubianlanguages.audioservices.domain.Recording;
//import com.nubianlanguages.audioservices.domain.RecordingType;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
import java.util.Optional;
public interface
RecordingRepository extends JpaRepository<Recording, Long> {

    List<Recording> findByWordObjectKeyIsNotNull();
    Optional<Recording> findByIdAndUserId(Long id, Long userId);

    List<Recording> findAllByUserIdOrderByIdDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
    @NotNull List<Recording> findAll();

}


