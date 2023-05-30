package com.example.capstone.repository;

import com.example.capstone.entity.Match;
import com.example.capstone.entity.Rating;
import com.example.capstone.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> getRatingsByMatch(Match match);
    Boolean existsRatingByMatchAndUid(Match match, Long uid);
}
