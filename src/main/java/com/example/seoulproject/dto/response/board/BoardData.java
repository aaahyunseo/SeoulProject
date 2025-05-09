package com.example.seoulproject.dto.response.board;

import com.example.seoulproject.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Builder
public class BoardData {
    private UUID boardId;
    private String title;
    private String content;
    private String createdAt;
    private int likeCount;
    private int dislikeCount;

    public static BoardData from(Board board) {
        return BoardData.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                .likeCount(board.getLikes() != null ? board.getLikes().size() : 0)
                .dislikeCount(board.getDislikes() != null ? board.getDislikes().size() : 0)
                .build();
    }
}
