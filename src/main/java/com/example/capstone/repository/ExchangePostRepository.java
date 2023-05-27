package com.example.capstone.repository;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangePostRepository extends JpaRepository<ExchangePost, Long> {
    List<ExchangePost> getAllByPostType(PostType postType);
    List<ExchangePost> getExchangePostsByGiveCate(String giveCategory);
    List<ExchangePost> getExchangePostsByTakeCate(String takeCategory);
    List<ExchangePost> getExchangePostsByGiveCateAndTakeCate(String giveCategory, String takeCategory);
}
