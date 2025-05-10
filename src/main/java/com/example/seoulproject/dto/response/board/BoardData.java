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
    private boolean liked;
    private boolean disliked;

    public static BoardData from(Board board, boolean liked, boolean disliked) {
        return BoardData.builder()
                .boardId(board.getId())
                .title(board.getTitle())
                .content(board.getContent())
                .createdAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .likeCount(board.getLikes() != null ? board.getLikes().size() : 0)
                .dislikeCount(board.getDislikes() != null ? board.getDislikes().size() : 0)
                .liked(liked)
                .disliked(disliked)
                .build();
    }

}

