package com.example.capstone.entity;

import com.example.capstone.dto.ChatRoomDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "chat_room")
@ToString
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "roomName", nullable = false)
    private String roomName; // 채팅방 이름

    // 게시물 PID
    // exchange_post 테이블이 chat_room 테이블에서 참조되기 전에 먼저 exchange_post 테이블 생성하고, chat_room 테이블 생성
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_pid", referencedColumnName = "Pid", nullable = false)
    private ExchangePost exchangePost;

    // 게시글 작성자 UID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "postWriter_UID")
    private UserEntity postWriter;

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

    // 세션
    @ElementCollection
    private Set<String> sessions = new HashSet<>();

    // 세션 추가
    public void addSession(String sessionId) {
        sessions.add(sessionId);
    }

    // 세션 제거
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
    }

}
