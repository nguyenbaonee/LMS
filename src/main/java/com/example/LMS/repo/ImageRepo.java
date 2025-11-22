package com.example.LMS.repo;

import com.example.LMS.entity.Image;
import com.example.LMS.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepo extends JpaRepository<Image, Long> {

    List<Image> findAllByIdInAndObjectIdAndStatus(List<Long> deleteAvatarsId, Long id, Status status);


    List<Image> findByObjectIdAndStatus(Long id, Status status);
}
