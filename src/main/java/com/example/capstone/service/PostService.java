package com.example.capstone.service;

import com.example.capstone.data.AvailableTime;
import com.example.capstone.dto.ExchangePostDTO;
import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Post;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.PostRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.capstone.entity.ExchangePost.formatDate;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public PostService(PostRepository postRepository, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.modelMapper = modelMapper;
    }

    public void save(ExchangePostDTO exchangePostDTO, UserEntity userEntity) {
        ExchangePost exchangePost = ExchangePost.toExchangePost(exchangePostDTO);
        exchangePost.setUser(userEntity); // 받아온 사용자 정보를 이용해서 게시물 작성자 정보 저장

        postRepository.save(exchangePost);
    }

    public void delete(ExchangePost exchangePost) {
        postRepository.delete(exchangePost);
    }

    public void update(ExchangePostDTO exchangePostDTO, ExchangePost exchangePost) {
        exchangePost.setTitle(exchangePostDTO.getTitle());
        exchangePost.setDate(formatDate(LocalDateTime.now())); // yyyy-MM-dd HH:mm:ss
        exchangePost.setContent(exchangePostDTO.getContent());
        exchangePost.setGiveCate(exchangePostDTO.getGiveCate());
        exchangePost.setGiveTalent(exchangePostDTO.getGiveTalent());
        exchangePost.setTakeCate(exchangePostDTO.getTakeCate());
        exchangePost.setTakeTalent(exchangePostDTO.getTakeTalent());
        // 이미지 세팅 추가하기

        // 시간대 정보 저장
        AvailableTime availableTime = exchangePostDTO.getAvailableTime();
        exchangePost.setDays(availableTime.getDays());
        exchangePost.setTimezone(availableTime.getTimezone());
    }

    public Set<ExchangePostDTO> getPostList() {
        List<ExchangePost[]> postList = postRepository.findPosts(); // entity
        // entity -> dto
        Set<ExchangePostDTO> exchangePostDTOList = postList.stream()
                .map(post -> modelMapper.map(post, ExchangePostDTO.class))
                .collect(Collectors.toSet());

        System.out.println("exchangePostDTOList = " + exchangePostDTOList);
        return exchangePostDTOList;
    }
}
