package com.example.seoulproject.repository;

import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.DislikeBoard;
import com.example.seoulproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DislikeBoardRepository extends JpaRepository<DislikeBoard, UUID> {
    boolean existsByUserAndBoard(User user, Board board);
    Optional<DislikeBoard> findByUserAndBoard(User user, Board board);
}
