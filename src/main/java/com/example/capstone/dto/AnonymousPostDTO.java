package com.example.capstone.dto;

import com.example.capstone.entity.AnonymousPost;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.PostType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class AnonymousPostDTO extends PostDTO {

    private final PostType postType = PostType.A; // 포스트 타입

    public static List<AnonymousPostDTO> toAnonymousPostDTOList(List<Post> postList, List<Boolean> isLikedList) {
        List<AnonymousPostDTO> anonymousPostDTOList = new ArrayList<>();

        for (int i=0; i<postList.size(); i++) {
            AnonymousPost anonymousPost = (AnonymousPost) postList.get(i);
            anonymousPostDTOList.add(toAnonymousPostDTO(anonymousPost, isLikedList.get(i)));
        }

        return anonymousPostDTOList;
    }

    public static AnonymousPostDTO toAnonymousPostDTO(AnonymousPost anonymousPost, Boolean isLiked) {
        AnonymousPostDTO anonymousPostDTO = new AnonymousPostDTO();
        anonymousPostDTO.setPid(anonymousPost.getPid());
        anonymousPostDTO.setTitle(anonymousPost.getTitle());
        anonymousPostDTO.setContent(anonymousPost.getContent());
        anonymousPostDTO.setDate(anonymousPost.getDate());
        anonymousPostDTO.setWriterNick(anonymousPost.getUser().getNickName()); // 작성자 닉네임
        anonymousPostDTO.setWriterId(anonymousPost.getUser().getId()); // 작성자 아이디
        anonymousPostDTO.setUid(anonymousPost.getUser().getUid());
        anonymousPostDTO.setWriterImageURL(anonymousPost.getUser().getProfile_img());
        anonymousPostDTO.setPostType(PostType.A);
        anonymousPostDTO.setLikeCount(anonymousPost.getLikes().size());
        anonymousPostDTO.setLiked(isLiked);

        // 이미지 관련 로직 추가
        anonymousPostDTO.setImages(anonymousPost.getPostImages());

        return anonymousPostDTO;
    }

}
