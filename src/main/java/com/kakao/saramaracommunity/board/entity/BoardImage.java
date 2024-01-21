package com.kakao.saramaracommunity.board.entity;

import com.kakao.saramaracommunity.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "BOARD_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoardImage extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Column(nullable = false)
    private String path;

    @Builder
    public BoardImage(Board board, String path) {
        this.board = board;
        this.path = path;
    }

    public void update(String path) {
        this.path = path;
    }

}
