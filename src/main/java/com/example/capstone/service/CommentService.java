package com.example.capstone.service;

import com.example.capstone.dto.CommentDTO;
import com.example.capstone.entity.Comment;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.CommentRepository;
import com.example.capstone.repository.PostRepository;
import com.example.capstone.repository.UserRepository;
import org.springframework.stereotype.Service;

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


    public void addComment(Long postId, CommentDTO commentDTO) {
        /*Optional<Post> post = postRepository.findById(postId);
        Optional<UserEntity> user = userRepository.findById(commentDTO.getUserId());

        if (post.isPresent() && user.isPresent()) {
            Comment comment = Comment.toEntity(commentDTO);
            System.out.println("COMMENT:" + comment);
            comment.setContent(commentDTO.getContent());
            comment.setUser(user.get());
            comment.setPost(post.get());
            comment.setDate(LocalDateTime.now());
            commentRepository.save(comment);
        } else {
            throw new IllegalArgumentException("Invalid post or user id");
        }*/

        Comment comment = Comment.toEntity(commentDTO);
        Optional<Post> optionalPost = postRepository.findById(postId);
        if (optionalPost.isEmpty()) {
            throw new IllegalArgumentException("해당 포스트가 존재하지 않습니다.");
        }
        Post post = optionalPost.get();
        comment.setPost(post);

        String writerId = String.valueOf(commentDTO.getUserId());
        Optional<UserEntity> optionalUserEntity = userRepository.findById(writerId);
        if (optionalUserEntity.isEmpty()) {
            throw new IllegalArgumentException("해당 작성자가 존재하지 않습니다.");
        }
        UserEntity writer = optionalUserEntity.get();
        comment.setUser(writer);

        commentRepository.save(comment);


    }

    //댓글 수정
    public CommentDTO updateComment(Long commentId, String content) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            System.out.println("존재하지 않는 댓글: " + commentId);
        }
        comment.setContent(content);
        commentRepository.save(comment);
        return CommentDTO.from(comment);
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
            commentDTOs.add(CommentDTO.from(comment));
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

