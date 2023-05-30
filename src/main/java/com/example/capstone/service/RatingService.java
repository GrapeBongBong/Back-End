package com.example.capstone.service;

import com.example.capstone.entity.Match;
import com.example.capstone.entity.Rating;
import com.example.capstone.entity.UserEntity;
import com.example.capstone.repository.RatingRepository;
import com.example.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;

    public void rate(Match match, int score, Long uid) {
        Rating rating = new Rating();
        rating.setMatch(match);
        rating.setScore(score);
        rating.setUid(uid);
        ratingRepository.save(rating);
    }

    public void rateTemperature(Long opponentId, int score) {
        Optional<UserEntity> user = userRepository.findById(opponentId);

        if (user.isPresent()) {
            UserEntity opponent = user.get();
            // score 이 1, 2 일 때는 신뢰온도 낮추기
            // score 이 3 일 때는 신뢰온도 그대로
            // score 이 4, 5 일 때는 신뢰온도 높이기
            if (score == 1) {
                opponent.setTemperature(opponent.getTemperature() - 2);
            } else if (score == 2) {
                opponent.setTemperature(opponent.getTemperature() - 1);
            } else if (score == 4) {
                opponent.setTemperature(opponent.getTemperature() + 4);
            } else if (score == 5) {
                opponent.setTemperature(opponent.getTemperature() + 5);
            }
            userRepository.save(opponent);
        }
    }

    public boolean isExist(Match match, Long userId) {
        boolean isUserExists = ratingRepository.existsRatingByMatchAndUid(match, userId);
        if (isUserExists) return true;
        else return false;
    }
}
