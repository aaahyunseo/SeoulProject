package com.example.seoulproject.repository;

import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByBoardOrderByCreatedAtDesc(Board board);
}
