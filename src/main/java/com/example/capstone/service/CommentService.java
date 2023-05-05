package com.example.capstone.service;

import com.example.capstone.dto.CommentDTO;
import com.example.capstone.dto.CommentRequestDTO;
import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.CommentRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public CommentDTO createComment(CommentRequestDTO requestDTO, Long postId, Long userId) {
        // 댓글을 등록할 게시물과 사용자 정보를 조회합니다.
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post id: " + postId));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user id: " + userId));

        // CommentRequestDTO를 Comment 엔티티로 변환
        Comment comment = Comment.from(requestDTO, post, user);

        // 변환된 Comment 엔티티를 저장
        Comment savedComment = commentRepository.save(comment);

        // 저장된 Comment 엔티티를 CommentDTO로 변환하여 반환
        return CommentDTO.tocommentDTO(savedComment);
    }

/*
    public Comment addComment(Long postId, CommentDTO commentDTO) {
        Post post = postRepository.findByPid(postId);
        UserEntity userEntity = userRepository.findById(commen);
        Comment comment = Comment.toEntity(commentDTO);

        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(commentDTO.getContent());
        comment.setDate(LocalDateTime.now());

        return commentRepository.save(comment);


    }
*/

    //댓글 수정
    public CommentDTO updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            System.out.println("존재하지 않는 댓글: " + commentId);
        }
        comment.setContent(content);
        commentRepository.save(comment);
        return CommentDTO.tocommentDTO(comment);
    }

    //댓글 삭제
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            System.out.println("존재하지 않는 댓글: " + commentId);
        }
        commentRepository.delete(comment);
    }

    // 댓글 목록을 조회
    public List<CommentDTO> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostPid(postId);
        List<CommentDTO> commentDTOs = new ArrayList<>();
        for (Comment comment : comments) {
            commentDTOs.add(CommentDTO.tocommentDTO(comment));
        }
        return commentDTOs;
    }

    // 게시물 존재 여부
    public boolean isPostExists(Long postId) {
        return postRepository.existsById(postId);
    }

    // 댓글 작성 사용자 일치 여부
    public boolean isCommentUser(Long commentId, String username) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        if (commentOptional.isPresent()) {
            Comment comment = commentOptional.get();
            return comment.getUser().getUid().equals(username);
        }
        return false;
    }

}

