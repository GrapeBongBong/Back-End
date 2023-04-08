package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "trade_request")
public class TradeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_req_id")
    private Long tradeId;

    // 게시물 작성자 UID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acceptor_id")
    private UserEntity acceptor;

    // 신청자 UID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private UserEntity applicant;

    // 게시물 PID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Pid")
    private ExchangePost pid;

    // 거래 상태 (대기, 수락, 거절)
    @Enumerated(EnumType.STRING)
    private TradeStatus status;

    public enum TradeStatus {
        WAITING, // 대기
        ACCEPTED, // 수락
        REJECTED // 거절
    }
}
