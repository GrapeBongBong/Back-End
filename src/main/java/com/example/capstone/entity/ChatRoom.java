package com.example.capstone.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private String roomId;

    // 게시물 PID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_pid", nullable = false)
    private ExchangePost exchangePost;

    // 신청자 UID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_UID")
    private UserEntity applicant;

    // 채팅 메시지들
    // mappedBy = "{필드명}"
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL)
    private List<ChatMessage> messages = new ArrayList<>();

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
}
