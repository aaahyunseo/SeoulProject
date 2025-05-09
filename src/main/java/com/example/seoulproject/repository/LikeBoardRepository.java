package com.example.seoulproject.repository;

import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.LikeBoard;
import com.example.seoulproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LikeBoardRepository extends JpaRepository<LikeBoard, UUID> {
    boolean existsByUserAndBoard(User user, Board board);
    Optional<LikeBoard> findByUserAndBoard(User user, Board board);
}
