package com.example.capstone.dto;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.PostType;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExchangePostDTO extends PostDTO{

    @NotBlank
    private String giveCate; // 주는 카테고리

    @NotBlank
    private String giveTalent; // 주는 재능

    @NotBlank
    private String takeCate; // 받는 카테고리

    @NotBlank
    private String takeTalent; // 받는 재능

    private AvailableTime availableTime; // 가능한 시간대

    private final PostType postType = PostType.T; // 포스트 타입

    private Boolean status; // 재능 거래 상태

    public static List<ExchangePostDTO> toExchangePostDTOList(List<ExchangePost> exchangePostList) {
        List<ExchangePostDTO> exchangePostDTOList = new ArrayList<>();

        for (ExchangePost exchangePost: exchangePostList) {
            exchangePostDTOList.add(toExchangePostDTO(exchangePost));
        }

        return exchangePostDTOList;
    }

    public static ExchangePostDTO toExchangePostDTO(ExchangePost exchangePost) { // entity -> dto
        ExchangePostDTO exchangePostDTO = new ExchangePostDTO();
        exchangePostDTO.setPid(exchangePost.getPid()); // 게시글 아이디
        exchangePostDTO.setTitle(exchangePost.getTitle());
        exchangePostDTO.setContent(exchangePost.getContent());
        exchangePostDTO.setDate(exchangePost.getDate());
        exchangePostDTO.setWriterNick(exchangePost.getUser().getNickName()); // 작성자 닉네임
        exchangePostDTO.setWriterId(exchangePost.getUser().getId()); // 작성자 아이디
        exchangePostDTO.setUid(exchangePost.getUser().getUid()); // 작성자 uid
        exchangePostDTO.setWriterImageURL(exchangePost.getUser().getProfile_img());

        exchangePostDTO.setGiveCate(exchangePost.getGiveCate());
        exchangePostDTO.setGiveTalent(exchangePost.getGiveTalent());
        exchangePostDTO.setTakeCate(exchangePost.getTakeCate());
        exchangePostDTO.setTakeTalent(exchangePost.getTakeTalent());
        exchangePostDTO.setPostType(PostType.T);
        exchangePostDTO.setStatus(exchangePost.getStatus());

        AvailableTime availableTimeDTO = new AvailableTime();
        availableTimeDTO.setDays(exchangePost.getDays());
        availableTimeDTO.setTimezone(exchangePost.getTimezone());
        exchangePostDTO.setAvailableTime(availableTimeDTO);

        // 이미지 관련 로직 추가
        exchangePostDTO.setImages(exchangePost.getPostImages());

        return exchangePostDTO;
    }
}