package com.notFoundTomAndJerry.notFoundJerry.domain.location.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.converter.LocationConverter;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request.LocationBoundsRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.request.UserLocationRequest;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationDistanceResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationFilterResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationRegionResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationService {

  private LocationRepository locationRepository;
  private LocationConverter locationConverter;

  // Room 생성 시 위치 유효성 검증
  public void validateLocation(Long locationId) {
    if (!locationRepository.existsByIdAndIsValidTrue(locationId)) {
      throw new IllegalArgumentException("존재하지 않는 지역입니다. id=" + locationId);
      // 또는 커스텀 예외: throw new LocationNotFoundException(locationId);
    }
  }

  // 지도 영역(MBR) 내의 방 ID 목록 조회
  public LocationFilterResponse filterLocationIdsByBounds(LocationBoundsRequest request) {
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
        .orElseThrow(() -> new IllegalArgumentException("해당 좌표 근처에 등록된 지역 정보가 없습니다."));
    return locationConverter.toRegionResponse(nearestLocation);
  }

  // 사용자 위치와 특정 장소 간의 거리 계산
  public LocationDistanceResponse calculateDistance(UserLocationRequest userLoc, Long locationId) {
    Location target = locationRepository.findById(locationId)
        .orElseThrow(() -> new IllegalArgumentException("위치 정보를 찾을 수 없습니다."));
    Point userPoint = locationConverter.toPoint(userLoc.getLatitude(), userLoc.getLongitude());

    Double distanceInMeters = locationRepository.getDistanceBetween(userPoint, target.getId());

    return locationConverter.toDistanceResponse(distanceInMeters);
  }
}