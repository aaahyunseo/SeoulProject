package com.example.seoulproject.controller;

import com.example.seoulproject.authentication.AuthenticatedUser;
import com.example.seoulproject.dto.request.comment.CommentRequestDto;
import com.example.seoulproject.dto.response.ResponseDto;
import com.example.seoulproject.dto.response.comment.CommentListData;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {
    private final CommentService commentService;

    @GetMapping("/{boardId}")
    public ResponseEntity<ResponseDto<CommentListData>> getComments(@PathVariable UUID boardId) {
        CommentListData commandListData = commentService.getComments(boardId);
        return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "댓글 전체 조회 완료", commandListData), HttpStatus.OK);
    }

    @PostMapping("/{boardId}")
    public ResponseEntity<ResponseDto<Void>> createComment(@AuthenticatedUser User user, @PathVariable UUID boardId, @RequestBody CommentRequestDto dto) {
        commentService.createComment(user, boardId, dto);
        return ResponseEntity.ok(ResponseDto.res(HttpStatus.CREATED, "댓글 작성 완료", null));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ResponseDto<Void>> deleteComment(@AuthenticatedUser User user, @PathVariable UUID commentId) {
        commentService.deleteComment(user, commentId);
        return ResponseEntity.ok(ResponseDto.res(HttpStatus.OK, "댓글 삭제 완료", null));
    }

}
