package com.kakao.saramaracommunity.board.service;

import com.kakao.saramaracommunity.board.dto.business.reqeust.BoardCreateServiceRequest;
import com.kakao.saramaracommunity.board.dto.business.reqeust.BoardUpdateServiceRequest;
import com.kakao.saramaracommunity.board.dto.business.response.BoardCreateResponse;
import com.kakao.saramaracommunity.board.dto.business.response.BoardGetResponse;
import com.kakao.saramaracommunity.board.dto.business.response.BoardSearchResponse;
import com.kakao.saramaracommunity.board.entity.Board;
import com.kakao.saramaracommunity.board.entity.SortType;
import com.kakao.saramaracommunity.board.exception.BoardBusinessException;
import com.kakao.saramaracommunity.board.exception.BoardErrorCode;
import com.kakao.saramaracommunity.board.repository.BoardRepository;
import com.kakao.saramaracommunity.member.entity.Member;
import com.kakao.saramaracommunity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Override
    public BoardCreateResponse createBoard(BoardCreateServiceRequest request) {
        Board createdBoard = boardRepository.save(
                request.toEntity(
                        getMemberEntity(request.memberId())
                )
        );
        return BoardCreateResponse.of(createdBoard);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardGetResponse getBoard(Long boardId) {
        Board board = getBoardEntity(boardId);
        return BoardGetResponse.of(board);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardSearchResponse searchBoards(Long cursorId, Pageable page, SortType sort) {
        List<Board> boards = getBoards(cursorId, page, sort);
        Long nextCursorId = getNextCursorId(sort, boards);
        Boolean hasNext = boards.size() >= page.getPageSize();
        return BoardSearchResponse.of(
                boards.stream()
                        .map(BoardGetResponse::of)
                        .collect(Collectors.toList()),
                hasNext,
                nextCursorId
        );
    }

    @Override
    public void updateBoard(Long boardId, BoardUpdateServiceRequest request) {
        Board savedBoard = getBoardEntity(boardId);
        verifyBoardOwner(savedBoard, request.memberId());
        log.info("[BoardServiceImpl] 게시글을 수정합니다. 게시글 번호: {}", savedBoard.getId());
        savedBoard.update(
                request.memberId(),
                request.title(),
                request.content(),
                request.categoryBoard(),
                request.deadLine(),
                request.boardImages()
        );
    }

    /**
     * 추후 삭제 로직 내에서 verifyBoardOwner 메소드를 활용해 게시글 작성자 여부를 함께 검사하도록 수정해야 함.
     */
    @Override
    public void deleteBoard(Long boardId) {
        Board savedBoard = getBoardEntity(boardId);
        // 추후 작성자 검증 필요
        log.info("[BoardServiceImpl] 게시글을 삭제합니다. 게시글 번호: {}", savedBoard.getId());
        boardRepository.delete(savedBoard);
    }

    private void verifyBoardOwner(Board board, Long memberId) {
        if (!board.getMember().getId().equals(memberId)) throw new BoardBusinessException(BoardErrorCode.UNAUTHORIZED_TO_UPDATE_BOARD);
    }

    /**
     * 추후 UNAUTHORIZED_TO_UPDATE_BOARD 예외가 아닌 MemberErrorCode.MEMBER_NOT_FOUND로 수정해야 함.
     */
    private Member getMemberEntity(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BoardBusinessException(BoardErrorCode.UNAUTHORIZED_TO_UPDATE_BOARD));
    }

    private Board getBoardEntity(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardBusinessException(BoardErrorCode.BOARD_NOT_FOUND));
    }

    /**
     * @param sort 현재는 최신순 정렬(SortType - LATEST(DEFAULT))만 사용되어 파라미터의 sort는 사용되지 않음.
     */
    private Long getNextCursorId(SortType sort, List<Board> boards) {
        return boards.isEmpty() ? null : boards.get(boards.size() - 1).getId();
    }

    /**
     * @param sort 현재는 최신순 정렬(SortType - LATEST(DEFAULT))만 사용되어 파라미터의 sort는 사용되지 않음.
     */
    private List<Board> getBoards(Long cursorId, Pageable page, SortType sort) {
        log.info("[BoardServiceImpl] 최신순으로 게시글을 조회합니다.(Reading all boards by latest)");
        return cursorId == null ?
                boardRepository.findAllByOrderByCreatedAtDesc(page) :
                boardRepository.findByIdLessThanOrderByCreatedAtDesc(cursorId, page);
    }

}
