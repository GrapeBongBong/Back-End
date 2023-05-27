package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "matching")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "exchange_post_id")
    private ExchangePost exchangePost;

    @JoinColumn(name = "chat_room_id")
    private Long chatRoomId;

    @JoinColumn(name = "writer_id")
    private Long writerId;

    @JoinColumn(name = "applicant_id")
    private Long applicantId;

}
