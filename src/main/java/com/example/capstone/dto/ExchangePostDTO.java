package com.example.capstone.dto;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.entity.ExchangePost;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    // 이미지 필드 추가

    public static List<ExchangePostDTO> toExchangePostDTOList(List<ExchangePost> exchangePostList) {
        List<ExchangePostDTO> exchangePostDTOList = new ArrayList<>();

        for (ExchangePost exchangePost: exchangePostList) {
            exchangePostDTOList.add(toExchangePostDTO(exchangePost));
        }

        return exchangePostDTOList;
    }

    public static ExchangePostDTO toExchangePostDTO(ExchangePost exchangePost) {
        ExchangePostDTO exchangePostDTO = new ExchangePostDTO();
        exchangePostDTO.setPid(exchangePost.getPid());
        exchangePostDTO.setWriterNick(exchangePost.getUser().getNickName()); // 작성자 닉네임
        exchangePostDTO.setWriterId(exchangePost.getUser().getId()); // 작성자 아이디
        exchangePostDTO.setDate(exchangePost.getDate());
        exchangePostDTO.setTitle(exchangePost.getTitle());
        exchangePostDTO.setContent(exchangePost.getContent());
        exchangePostDTO.setGiveCate(exchangePost.getGiveCate());
        exchangePostDTO.setGiveTalent(exchangePost.getGiveTalent());
        exchangePostDTO.setTakeCate(exchangePost.getTakeCate());
        exchangePostDTO.setTakeTalent(exchangePost.getTakeTalent());

        AvailableTime availableTimeDTO = new AvailableTime();
        availableTimeDTO.setDays(exchangePost.getDays());
        availableTimeDTO.setTimezone(exchangePost.getTimezone());
        exchangePostDTO.setAvailableTime(availableTimeDTO);

        return exchangePostDTO;
    }
}