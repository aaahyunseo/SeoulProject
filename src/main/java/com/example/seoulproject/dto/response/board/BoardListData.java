package com.example.seoulproject.dto.response.board;

import com.example.seoulproject.entity.Board;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class BoardListData {
    private List<BoardResponseDto> boardList;

    public static BoardListData from(List<Board> boards) {
        return BoardListData.builder()
                .boardList(boards.stream()
                        .map(BoardResponseDto::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
