package com.example.seoulproject.service;

import com.example.seoulproject.authentication.AuthenticatedUser;
import com.example.seoulproject.dto.request.board.CreateBoardDto;
import com.example.seoulproject.dto.request.board.UpdateBoardDto;
import com.example.seoulproject.dto.response.board.BoardData;
import com.example.seoulproject.dto.response.board.BoardListData;
import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.DislikeBoard;
import com.example.seoulproject.entity.LikeBoard;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.exception.NotFoundException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import com.example.seoulproject.repository.BoardRepository;
import com.example.seoulproject.repository.DislikeBoardRepository;
import com.example.seoulproject.repository.LikeBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final LikeBoardRepository likeBoardRepository;
    private final DislikeBoardRepository dislikeBoardRepository;

    // 게시글 전체 조회
    public BoardListData getBoardList() {
        List<Board> boards = boardRepository.findAll();
        return BoardListData.from(boards);
    }

    // 게시글 상세 조회
    public BoardData getBoardById(@AuthenticatedUser User user, @PathVariable UUID boardId) {
        Board board = findBoard(boardId);
        return BoardData.from(board);
    }

    // 게시글 작성
    public void createBoard(@AuthenticatedUser User user, CreateBoardDto createBoardDto) {
        Board board = new Board(user, createBoardDto.getTitle(), createBoardDto.getContent());
        boardRepository.save(board);
    }

    // 게시글 수정
    public void updateBoard(@AuthenticatedUser User user, UUID boardId, UpdateBoardDto updateBoardDto) {
        Board newBoard = findBoardByIdAndUser(user, boardId);
        newBoard.setTitle(updateBoardDto.getTitle())
                .setContent(updateBoardDto.getContent());
        boardRepository.save(newBoard);
    }

    // 게시글 삭제
    public void deleteBoard(@AuthenticatedUser User user, @PathVariable UUID boardId) {
        Board board = findBoardByIdAndUser(user, boardId);
        boardRepository.delete(board);
    }

    // 좋아요 등록
    public void likeBoard(User user, UUID boardId) {
        Board board = findBoard(boardId);

        // 이미 좋아요 했는지 확인
        if (likeBoardRepository.existsByUserAndBoard(user, board)) {
            return;
        }

        // 기존에 싫어요를 눌렀다면 삭제
        dislikeBoardRepository.findByUserAndBoard(user, board)
                .ifPresent(dislikeBoardRepository::delete);

        // 좋아요 추가
        LikeBoard likeBoard = LikeBoard.builder()
                .user(user)
                .board(board)
                .build();
        likeBoardRepository.save(likeBoard);
    }

    // 좋아요 취소
    public void unlikeBoard(User user, UUID boardId) {
        Board board = findBoard(boardId);
        likeBoardRepository.findByUserAndBoard(user, board)
                .ifPresent(likeBoardRepository::delete);
    }

    // 싫어요 등록
    public void dislikeBoard(User user, UUID boardId) {
        Board board = findBoard(boardId);

        if (dislikeBoardRepository.existsByUserAndBoard(user, board)) {
            return;
        }

        likeBoardRepository.findByUserAndBoard(user, board)
                .ifPresent(likeBoardRepository::delete);

        DislikeBoard dislikeBoard = DislikeBoard.builder()
                .user(user)
                .board(board)
                .build();
        dislikeBoardRepository.save(dislikeBoard);
    }

    // 싫어요 취소
    public void undislikeBoard(User user, UUID boardId) {
        Board board = findBoard(boardId);
        dislikeBoardRepository.findByUserAndBoard(user, board)
                .ifPresent(dislikeBoardRepository::delete);
    }

    private Board findBoard(UUID boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Board findBoardByIdAndUser(User user, UUID boardId) {
        return boardRepository.findByIdAndUser(boardId, user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
    }
}
