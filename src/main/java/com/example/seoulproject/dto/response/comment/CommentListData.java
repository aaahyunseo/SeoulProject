package com.example.seoulproject.dto.response.comment;

import com.example.seoulproject.entity.Comment;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class CommentListData {
    private List<CommentDto> commentList;

    public static CommentListData from(List<Comment> comments) {
        return CommentListData.builder()
                .commentList(comments.stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
