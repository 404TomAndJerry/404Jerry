package com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums;

public enum RoomStatus {
    WAITING,    // 대기 중 (참가 가능)
    RUNNING,    // 게임 진행 중
    FINISHED,   // 게임 종료
    DELETED     // 삭제됨 (소프트 삭제)
}
