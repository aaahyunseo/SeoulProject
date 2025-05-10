package com.example.seoulproject.repository;

import com.example.seoulproject.dto.response.board.BoardWithReactionDto;
import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    Optional<Board> findByIdAndUser(UUID boardId, User user);

    @Query("""
    SELECT new com.example.seoulproject.dto.response.board.BoardWithReactionDto(
        b.id, b.title, b.content,
        SIZE(b.likes), SIZE(b.dislikes)
    )
    FROM Board b
    ORDER BY (SIZE(b.likes) + SIZE(b.dislikes)) DESC
    """)
    List<BoardWithReactionDto> findTop3ByReactionCount(Pageable pageable);
}
