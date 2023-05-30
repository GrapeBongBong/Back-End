package com.example.capstone.repository;

import com.example.capstone.entity.ExchangePost;
import com.example.capstone.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    Boolean existsMatchByWriterIdAndExchangePost(Long writerId, ExchangePost exchangePost);
    Boolean existsMatchByApplicantIdAndExchangePost(Long applicantId, ExchangePost exchangePost);
    Match getMatchByWriterIdAndExchangePost(Long writerId, ExchangePost exchangePost);
    Match getMatchByApplicantIdAndExchangePost(Long applicantId, ExchangePost exchangePost);
    List<Match> getMatchesByExchangePost(ExchangePost exchangePost);
    List<Match> findByWriterIdOrApplicantId(Long writerId, Long applicantId);
    List<Match> findByWriterId(Long writerId);
    List<Match> findByApplicantId(Long applicantId);
}
