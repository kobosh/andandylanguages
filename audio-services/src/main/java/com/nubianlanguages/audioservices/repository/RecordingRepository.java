package com.nubianlanguages.audioservices.repository;

import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.entity.RecordingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*public interface RecordingRepository extends JpaRepository<Recording, Long> {
    List<Recording> findByUserIdOrderByIdDesc(Integer userId);
    //(Long userId);
}*/
//package com.nubianlanguages.audioservices.repository;
//
//import com.nubianlanguages.audioservices.domain.Recording;
//import com.nubianlanguages.audioservices.domain.RecordingType;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.List;
import java.util.Optional;

public interface RecordingRepository extends JpaRepository<Recording, Long> {

    // Find the original WORD recording
  //  Optional<Recording> findByEmaile(String email);



    // Optional: all recordings by a user
   // List<Recording> findByUserId(Integer userId);
}



