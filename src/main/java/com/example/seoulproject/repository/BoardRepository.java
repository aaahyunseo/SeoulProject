package com.example.seoulproject.repository;

import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    Optional<Board> findByIdAndUser(UUID boardId, User user);
}
