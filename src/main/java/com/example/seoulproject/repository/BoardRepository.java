package com.example.seoulproject.repository;

import com.example.seoulproject.dto.response.board.BoardWithReactionDto;
import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    Optional<Board> findByIdAndUser(UUID boardId, User user);

    @Query(value = """
    SELECT b.* FROM boards b
    LEFT JOIN `like-boards` l ON b.id = l.board_id
    LEFT JOIN `dislike-boards` d ON b.id = d.board_id
    GROUP BY b.id
    ORDER BY (COUNT(DISTINCT l.id) + COUNT(DISTINCT d.id)) DESC
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Board> findAllOrderByReactionCountDescNative(@Param("limit") int limit, @Param("offset") int offset);


    @Query("""
    SELECT new com.example.seoulproject.dto.response.board.BoardWithReactionDto(
        b.id, b.title, b.content, b.createdAt,
        SIZE(b.likes), SIZE(b.dislikes)
    )
    FROM Board b
    ORDER BY (SIZE(b.likes) + SIZE(b.dislikes)) DESC
    """)
    List<BoardWithReactionDto> findTop3ByReactionCount(Pageable pageable);
}
