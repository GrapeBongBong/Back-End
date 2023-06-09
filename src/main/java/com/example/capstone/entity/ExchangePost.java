package com.example.capstone.entity;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.data.StringArrayConverter;
import com.example.capstone.dto.ExchangePostDTO;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "exchange_post")
public class ExchangePost extends Post {

    @Column(name = "give_cate")
    @NonNull
    private String giveCate;

    @Column(name = "take_cate")
    @NonNull
    private String takeCate;

    @Column(name = "give_talent")
    @NonNull
    private String giveTalent;

    @Column(name = "take_talent")
    @NonNull
    private String takeTalent;

    // 재능 거래 중 : 1, 마감(완료) : 0
    @Column(nullable = false)
    @NonNull
    private Boolean status;

    @NonNull
    @Convert(converter = StringArrayConverter.class)
    private String[] days;

    @NonNull
    private String timezone;

    public static ExchangePost toExchangePost(ExchangePostDTO exchangePostDTO) { // dto -> entity

        ExchangePost exchangePost = new ExchangePost();
        exchangePost.setTitle(exchangePostDTO.getTitle());
        exchangePost.setDate(formatDate(LocalDateTime.now())); // yyyy-MM-dd HH:mm:ss
        exchangePost.setContent(exchangePostDTO.getContent());
        exchangePost.setGiveCate(exchangePostDTO.getGiveCate());
        exchangePost.setGiveTalent(exchangePostDTO.getGiveTalent());
        exchangePost.setTakeCate(exchangePostDTO.getTakeCate());
        exchangePost.setTakeTalent(exchangePostDTO.getTakeTalent());
        exchangePost.setStatus(true); // 재능거래 중
        exchangePost.setPostType(exchangePostDTO.getPostType());

        // 시간대 정보 저장
        AvailableTime availableTime = exchangePostDTO.getAvailableTime();
        exchangePost.setDays(availableTime.getDays());
        exchangePost.setTimezone(availableTime.getTimezone());

        return exchangePost;
    }
}
