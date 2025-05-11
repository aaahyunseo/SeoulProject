package com.example.seoulproject.dto.response.comment;

import com.example.seoulproject.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@Builder
public class CommentDto {
    private UUID commentId;
    private String userName;
    private String content;
    private String createdAt;

    public static CommentDto from(Comment comment) {
        return CommentDto.builder()
                .commentId(comment.getId())
                .userName(comment.getUser().getName())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}
