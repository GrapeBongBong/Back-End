package com.example.capstone.entity;

import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
@Table(name = "exchange_post")
public class ExchangePost extends Post {

    @Column(name = "give_cate", nullable = false)
    private String giveCate;

    @Column(name = "take_cate", nullable = false)
    private String takeCate;

    @Column(name = "give_talent", nullable = false)
    private String giveTalent;

    @Column(name = "take_talent", nullable = false)
    private String takeTalent;

    // 재능 거래 중 : 1, 마감(완료) : 0
    @Column(nullable = false)
    private Boolean status;


    public static ExchangePost toExchangePost(ExchangePostDTO exchangePostDTO, User user) {

        ExchangePost exchangePost = new ExchangePost();
        exchangePost.setTitle(exchangePostDTO.getTitle());
        exchangePost.setDate(formatDate(LocalDateTime.now())); // yyyy-MM-dd HH:mm:ss
        exchangePost.setContent(exchangePostDTO.getContent());
        exchangePost.setGiveCate(exchangePostDTO.getGiveCate());
        exchangePost.setGiveTalent(exchangePostDTO.getGiveTalent());
        exchangePost.setTakeCate(exchangePostDTO.getTakeCate());
        exchangePost.setTakeTalent(exchangePostDTO.getTakeTalent());
        // 이미지 세팅 추가하기
        exchangePost.setStatus(true); // 재능거래 중
        exchangePost.setPostType(PostType.T);
//        exchangePost.setUser(user);

        return exchangePost;
    }

    private static String formatDate(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = localDateTime.format(dateTimeFormatter);
        return formattedTime;
    }
}
