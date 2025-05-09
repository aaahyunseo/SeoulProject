package com.example.seoulproject.service;

import com.example.seoulproject.authentication.AuthenticatedUser;
import com.example.seoulproject.dto.request.board.CreateBoardDto;
import com.example.seoulproject.dto.request.board.UpdateBoardDto;
import com.example.seoulproject.dto.response.board.BoardData;
import com.example.seoulproject.dto.response.board.BoardListData;
import com.example.seoulproject.entity.Board;
import com.example.seoulproject.entity.User;
import com.example.seoulproject.exception.NotFoundException;
import com.example.seoulproject.exception.errorcode.ErrorCode;
import com.example.seoulproject.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;

    public BoardListData getBoardList() {
        List<Board> boards = boardRepository.findAll();
        return BoardListData.from(boards);
    }

    public BoardData getBoardById(@AuthenticatedUser User user, @PathVariable UUID boardId) {
        Board board = findBoard(boardId);
        return BoardData.from(board);
    }

    public void createBoard(@AuthenticatedUser User user, CreateBoardDto createBoardDto) {
        Board board = new Board(user, createBoardDto.getTitle(), createBoardDto.getContent());
        boardRepository.save(board);
    }

    public void updateBoard(@AuthenticatedUser User user, UUID boardId, UpdateBoardDto updateBoardDto) {
        Board newBoard = findBoardByIdAndUser(user, boardId);
        newBoard.setTitle(updateBoardDto.getTitle())
                .setContent(updateBoardDto.getContent());
        boardRepository.save(newBoard);
    }

    public void deleteBoard(@AuthenticatedUser User user, @PathVariable UUID boardId) {
        Board board = findBoardByIdAndUser(user, boardId);
        boardRepository.delete(board);
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
