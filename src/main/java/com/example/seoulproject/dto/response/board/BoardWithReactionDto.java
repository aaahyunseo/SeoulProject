package com.example.seoulproject.dto.response.board;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class BoardWithReactionDto {
    private UUID boardId;
    private String title;
    private String content;
    private int likeCount;
    private int dislikeCount;
}

