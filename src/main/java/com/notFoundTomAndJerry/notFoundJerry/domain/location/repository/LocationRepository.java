package com.notFoundTomAndJerry.notFoundJerry.domain.location.repository;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

  // 주소가 비어있는 장소들만 찾아오는 메서드 추가
  List<Location> findByAddressIsNull();

  // 중복 확인을 위해 공원관리번호 조회 메서드 추가
  Optional<Location> findByManageNo(String manageNo);

  // Room 생성 시 위치 존재 여부 및 유효성 검증
  boolean existsByIdAndIsValidTrue(Long id);

  /**
   * 지도 영역(MBR) 내 방 ID 필터링
   * <pre>
   * MBRContains: 사각형 영역 안에 점이 포함되는지 확인
   * ST_GeomFromText: WKT 형식을 공간 객체로 변환
   * </pre>
   */
  @Query(value = "SELECT id FROM locations l " +
      "WHERE l.is_valid = true AND MBRContains(ST_GeomFromText(:envelope, 4326), l.point)",
      nativeQuery = true)
  List<Long> findIdsWithinBounds(@Param("envelope") String envelope);

  // 특정 행정구역(구 단위) 명칭으로 위치 정보 조회(유효할 경우)
  List<Location> findByRegionName(String regionName);

  /**
   * 특정 좌표와 가장 가까운 위치 정보 1건 조회 (유효성 및 명칭 확인용)
   * <pre>
   * ST_Distance_Sphere: 지구 곡률을 고려한 거리 계산
   * </pre>
   */
  @Query(value = "SELECT * FROM locations l " +
      "WHERE l.is_valid = true " +
      "ORDER BY ST_Distance_Sphere(l.point, :point) ASC LIMIT 1",
      nativeQuery = true)
  Optional<Location> findNearestLocation(@Param("point") Point point);

  // 내 위치 기준 반경 내 위치들 조회 및 거리 계산
  @Query(value = "SELECT *, ST_Distance_Sphere(point, :userPoint) AS distance " +
      "FROM locations " +
      "WHERE ST_Distance_Sphere(point, :userPoint) <= :radius " +
      "AND is_valid = true " +
      "ORDER BY distance ASC",
      nativeQuery = true)
  List<Location> findLocationsWithinRadius(@Param("userPoint") Point userPoint,
      @Param("radius") Double radius);

  // 특정 지점(사용자)과 특정 장소 사이의 거리 계산
  @Query(value = "SELECT ST_Distance_Sphere(:userPoint, l.point) " +
      "FROM locations l " +
      "WHERE l.id = :locationId",
      nativeQuery = true)
  Double getDistanceBetween(@Param("userPoint") Object userPoint,
      @Param("locationId") Long locationId);

  /**
   * 유효성이 검증된 모든 장소 목록 조회 프론트엔드 초기 지도 렌더링 시 전체 마커를 표시하기 위해 사용
   */
  List<Location> findAllByIsValidTrue();
}