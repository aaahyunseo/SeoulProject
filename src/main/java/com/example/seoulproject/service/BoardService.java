package com.example.seoulproject.service;

import com.example.seoulproject.authentication.AuthenticatedUser;
import com.example.seoulproject.dto.request.board.CreateBoardDto;
import com.example.seoulproject.dto.request.board.UpdateBoardDto;
import com.example.seoulproject.dto.response.board.BoardData;
import com.example.seoulproject.dto.response.board.BoardListData;
import com.example.seoulproject.dto.response.board.BoardWithReactionDto;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    private final LikeBoardRepository likeBoardRepository;
    private final DislikeBoardRepository dislikeBoardRepository;

    // 게시글 전체 조회
    public BoardListData getBoardList(int page, String sort) {
        int pageSize = 10;
        int adjustedPage = Math.max(page - 1, 0);

        List<Board> boards;

        if (sort.equalsIgnoreCase("popular")) {
            boards = boardRepository.findAllOrderByReactionCountDescNative(pageSize, adjustedPage * pageSize);
        } else {
            Pageable pageable = PageRequest.of(adjustedPage, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            boards = boardRepository.findAll(pageable).getContent();
        }

        return BoardListData.from(boards);
    }

    // 게시글 상세 조회
    public BoardData getBoardById(UUID boardId) {
        Board board = findBoard(boardId);

        boolean liked = false;
        boolean disliked = false;

        return BoardData.from(board, liked, disliked);
    }

    // 게시글 상세 조회
    public BoardData getBoardByIdOnLogin(@AuthenticatedUser User user, UUID boardId) {
        Board board = findBoard(boardId);

        boolean liked = likeBoardRepository.existsByUserAndBoard(user, board);
        boolean disliked = dislikeBoardRepository.existsByUserAndBoard(user, board);

        return BoardData.from(board, liked, disliked);
    }

    // 게시글 작성
    public void createBoard(@AuthenticatedUser User user, CreateBoardDto createBoardDto) {
        Board board = new Board(user, createBoardDto.getTitle(), createBoardDto.getContent());
        boardRepository.save(board);
    }

    // 게시글 수정
    public void updateBoard(@AuthenticatedUser User user, UUID boardId, UpdateBoardDto updateBoardDto) {
        Board newBoard = updateFindBoardByIdAndUser(user, boardId);
        newBoard.setTitle(updateBoardDto.getTitle())
                .setContent(updateBoardDto.getContent());
        boardRepository.save(newBoard);
    }

    // 게시글 삭제
    public void deleteBoard(@AuthenticatedUser User user, @PathVariable UUID boardId) {
        Board board = deleteFindBoardByIdAndUser(user, boardId);
        boardRepository.delete(board);
    }

    // 인기글 Top3 조회
    public List<BoardWithReactionDto> getTop3Boards() {
        Pageable pageable = PageRequest.of(0, 3);
        return boardRepository.findTop3ByReactionCount(pageable);
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

    private Board updateFindBoardByIdAndUser(User user, UUID boardId) {
        return boardRepository.findByIdAndUser(boardId, user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BOARD_NOT_FOUND));
    }

    private Board deleteFindBoardByIdAndUser(User user, UUID boardId) {
        return boardRepository.findByIdAndUser(boardId, user)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NO_ACCESS));
    }
}
