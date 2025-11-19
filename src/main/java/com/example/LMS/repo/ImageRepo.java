package com.example.LMS.repo;

import com.example.LMS.entity.Image;
import com.example.LMS.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepo extends JpaRepository<Image, Long> {
    List<Image> findAllByIdInAndStatus(List<Long> ids, Status status);
}
