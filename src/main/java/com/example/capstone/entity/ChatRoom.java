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
    private Long roomId;

    @Column(name = "name", nullable = false)
    private String name; // 채팅방 이름

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

//    public static ChatRoomDTO toChatRoomDTO(ChatRoom chatRoom) {
//        ChatRoomDTO chatRoomDTO = new ChatRoomDTO();
//        chatRoomDTO.setRoomId(chatRoom.getRoomId());
//        chatRoomDTO.setName(chatRoom.getName());
//
//        return chatRoomDTO;
//    }
}
