package com.notFoundTomAndJerry.notFoundJerry.domain.room.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.LocationWithRoomCountResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.Room;
import com.notFoundTomAndJerry.notFoundJerry.domain.room.entity.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // 기본 목록 조회 (필터 + 페이징)
    @Query("SELECT r FROM Room r " +
            "WHERE (:locationId IS NULL OR r.locationId = :locationId) " +
            "AND (:status IS NULL OR r.status = :status)")
    Page<Room> findRooms(
            @Param("locationId") Long locationId,
            @Param("status") RoomStatus status,
            Pageable pageable
    );

    // 권한/검증 유틸 (방장 여부 확인)
    boolean existsByIdAndHostId(Long id, Long hostId);

    // 상세 조회 (참가자 정보 Fetch Join으로 한 번에 로딩)
    @Query("SELECT r FROM Room r LEFT JOIN FETCH r.participants WHERE r.id = :id")
    Optional<Room> findByIdWithParticipants(@Param("id") Long id);

    // 내가 만든 방 목록 (Host 기준 조회)
    // - 보통 "내 방 목록"은 최신순 정렬이 많아서 페이징 없이 리스트로 받거나, 필요하면 Pageable 추가 가능
    List<Room> findByHostId(Long hostId);

    // 내가 참가 중인 방 목록 (Participant 조인)
    // - RoomParticipant 테이블을 조인해서 내 userId가 포함된 방을 찾음
    // - DISTINCT를 써서 중복 방지 (한 방에 역할 변경 등으로 여러 번 참가 로그가 남지 않는다면 생략 가능하지만 안전하게 추가)
    @Query("SELECT DISTINCT r FROM Room r " +
            "JOIN r.participants p " +
            "WHERE p.userId = :userId " +
            "AND r.status IN ('WAITING', 'RUNNING')")
    // 보통 완료/삭제된 방은 제외하고 보여줌
    List<Room> findParticipatedRooms(@Param("userId") Long userId);

    /**
     * 지도 마커용: 특정 영역 내 장소별 방 개수 집계 - Location 엔티티에 추가한 latitude, longitude, address 필드를 사용
     */
    @Query(
        "SELECT new com.notFoundTomAndJerry.notFoundJerry.domain.room.dto.response.LocationWithRoomCountResponse("
            + "l.id, l.parkNm, l.latitude, l.longitude, l.address, "
            + "CAST(SUM(CASE WHEN r.status = 'WAITING' THEN 1 ELSE 0 END) AS long), "
            + "CAST(SUM(CASE WHEN r.status = 'RUNNING' THEN 1 ELSE 0 END) AS long)) "
            + "FROM Location l "
            + "LEFT JOIN Room r ON l.id = r.locationId "
            + "WHERE l.latitude BETWEEN :minLat AND :maxLat "
            + "AND l.longitude BETWEEN :minLng AND :maxLng "
            + "GROUP BY l.id, l.parkNm, l.latitude, l.longitude, l.address")
    List<LocationWithRoomCountResponse> findAllWithRoomStatusCount(
        @Param("minLat") Double minLat, @Param("maxLat") Double maxLat,
        @Param("minLng") Double minLng, @Param("maxLng") Double maxLng
    );
}