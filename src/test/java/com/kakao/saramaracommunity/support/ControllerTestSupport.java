package com.kakao.saramaracommunity.support;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.kakao.saramaracommunity.board.controller.BoardController;
import com.kakao.saramaracommunity.board.service.BoardService;
import com.kakao.saramaracommunity.bucket.controller.BucketController;
import com.kakao.saramaracommunity.bucket.service.BucketService;
import com.kakao.saramaracommunity.comment.controller.CommentController;
import com.kakao.saramaracommunity.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WithMockUser(roles = "USER")
@WebMvcTest(controllers = {
        BucketController.class,
        BoardController.class,
        CommentController.class
})
public class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected BucketService bucketService;

    @MockBean
    protected BoardService boardService;

    @MockBean
    protected CommentService commentService;

}
