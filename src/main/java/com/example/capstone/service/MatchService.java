package com.example.capstone.service;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Match;
import com.example.capstone.entity.Post;
import com.example.capstone.repository.MatchRepository;
import com.example.capstone.repository.PostRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class MatchService {
    private final PostRepository postRepository;
    private final MatchRepository matchRepository;

    public MatchService(PostRepository postRepository, MatchRepository matchRepository) {
        this.postRepository = postRepository;
        this.matchRepository = matchRepository;
    }

    public Match processMatch(Long exchangePostId) {
        Optional<Post> exchangePostOptional = postRepository.findById(exchangePostId);
        if (exchangePostOptional.isPresent()) {
            ExchangePost exchangePost = (ExchangePost) exchangePostOptional.get();

            // 이미 매칭이 진행 중인 경우
            if (!exchangePost.getStatus()) {
                throw new RuntimeException("이미 매칭이 진행 중인 게시물입니다.");
            }

            // 매칭 진행
            Match match = new Match();
            match.setExchangePost(exchangePost);

            matchRepository.save(match);

            // 재능 교환 게시물의 상태를 0으로 변경
            exchangePost.setStatus(false);
            postRepository.save(exchangePost);

            return match;
        } else {
            throw new EntityNotFoundException("매칭을 진행할 게시물을 찾을 수 없습니다.");
        }
    }

}
