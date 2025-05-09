package com.example.seoulproject.repository;

import com.example.seoulproject.entity.LikeBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LikeBoardRepository extends JpaRepository<LikeBoard, UUID> {
}
