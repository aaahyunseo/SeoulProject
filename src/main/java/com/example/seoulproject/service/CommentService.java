package com.example.seoulproject.service;

import com.example.seoulproject.dto.request.comment.CommentRequestDto;
import com.example.seoulproject.dto.response.comment.CommentListData;
import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.Comment;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.exception.ForbiddenException;
import com.example.seoulproject.exception.NotFoundException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import com.example.seoulproject.repository.BoardRepository;
import com.example.seoulproject.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // 댓글 조회
    public CommentListData getComments(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
        List<Comment> comments = commentRepository.findAllByBoardOrderByCreatedAtDesc(board);
        return CommentListData.from(comments);
    }

    public void createComment(User user, UUID boardId, CommentRequestDto dto) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));

        Comment comment = Comment.builder()
                .content(dto.getContent())
                .user(user)
                .board(board)
                .build();

        commentRepository.save(comment);
    }

    public void deleteComment(User user, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ErrorCode.NO_ACCESS);
        }

        commentRepository.delete(comment);
    }

}
