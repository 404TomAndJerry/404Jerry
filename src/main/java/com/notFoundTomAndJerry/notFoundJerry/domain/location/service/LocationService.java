package com.notFoundTomAndJerry.notFoundJerry.domain.location.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.converter.LocationConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request.LocationBoundsRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request.UserLocationRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationDistanceResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationFilterResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationRegionResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.LocationErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private final LocationRepository locationRepository;
  private final LocationConverter locationConverter;

  // 전체 조회 (프론트엔드 마커용)
  public List<LocationResponse> getAllAvailableLocations() {
    // 배치가 검증 완료(isValid=true)하고 주소까지 채워진 데이터만 조회
    return locationRepository.findAllByIsValidTrue().stream()
        .map(LocationResponse::from)
        .collect(Collectors.toList());
  }

  /**
   * 반경 기반 조회 (주변 구 포함)
   *
   * @param latitude       중심 위도
   * @param longitude      중심 경도
   * @param radiusInMeters 반경(미터 단위, 예: 3000 = 3km)
   */
  public List<LocationResponse> getLocationsByRadius(Double latitude, Double longitude,
      Double radiusInMeters) {
    // 위경도를 공간 객체(Point)로 변환
    Point centerPoint = locationConverter.toPoint(latitude, longitude);

    // Repository의 공간 쿼리 호출
    List<Location> locations = locationRepository.findLocationsWithinRadius(centerPoint,
        radiusInMeters);

    // DTO 변환 후 반환
    return locations.stream()
        .map(LocationResponse::from)
        .toList();
  }

  // Room 생성 시 위치 유효성 검증
  public void validateLocation(Long locationId) {
    if (!locationRepository.existsByIdAndIsValidTrue(locationId)) {
      throw new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND);
    }
  }

  // 지도 영역(MBR) 내의 방 ID 목록 조회
  public LocationFilterResponse filterLocationIdsByBounds(LocationBoundsRequest request) {
    // 좌표 값이 유효하지 않을 경우를 대비한 방어 로직
    if (request.getMinLat() == null || request.getMinLng() == null) {
      throw new BusinessException(LocationErrorCode.INVALID_COORDINATES);
    }

    // Polygon(사각형 영역)을 만들어 Repository에 전달
    String envelope = String.format("POLYGON((%f %f, %f %f, %f %f, %f %f, %f %f))",
        request.getMinLng(), request.getMinLat(),
        request.getMaxLng(), request.getMaxLat(),
        request.getMaxLng(), request.getMaxLat(),
        request.getMinLng(), request.getMaxLat(),
        request.getMinLng(), request.getMinLat());

    List<Long> ids = locationRepository.findIdsWithinBounds(envelope);
    return new LocationFilterResponse(ids);
  }

  // 특정 행정구역(구 단위) 명칭으로 위치 정보 조회
  public LocationRegionResponse getRegionName(Double latitude, Double longitude) {
    Point point = locationConverter.toPoint(latitude, longitude);

    Location nearestLocation = locationRepository.findNearestLocation(point)
        .orElseThrow(() -> new BusinessException(LocationErrorCode.REGION_NOT_FOUND));
    return locationConverter.toRegionResponse(nearestLocation);
  }

  // 사용자 위치와 특정 장소 간의 거리 계산
  public LocationDistanceResponse calculateDistance(UserLocationRequest userLoc, Long locationId) {
    Location target = locationRepository.findById(locationId)
        .orElseThrow(() -> new BusinessException(LocationErrorCode.LOCATION_NOT_FOUND));
    Point userPoint = locationConverter.toPoint(userLoc.getLatitude(), userLoc.getLongitude());

    Double distanceInMeters = locationRepository.getDistanceBetween(userPoint, target.getId());

    if (distanceInMeters == null) {
      throw new BusinessException(LocationErrorCode.DATA_MAPPING_ERROR); // 계산 실패 시
    }

    return locationConverter.toDistanceResponse(distanceInMeters);
  }
}