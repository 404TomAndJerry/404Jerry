package com.notFoundTomAndJerry.notFoundJerry.domain.location.converter;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationDistanceResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.response.LocationRegionResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
public class LocationConverter {

  private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

  // 위경도 데이터를 JTS Point 객체로 변환
  public Point toPoint(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
      return null;
    }
    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }

  // Location 엔티티에서 지역 명칭 응답 DTO로 변환
  public LocationRegionResponse toRegionResponse(Location location) {
    if (location == null) {
      return null;
    }
    return new LocationRegionResponse(location.getRegionName());
  }

  // 계산된 거리 결과값을 DTO로 변환
  public LocationDistanceResponse toDistanceResponse(Double distanceInMeters) {
    if (distanceInMeters == null) {
      return null;
    }

    if (distanceInMeters >= 1000) {
      double km = Math.round((distanceInMeters / 1000.0) * 10) / 10.0;
      return new LocationDistanceResponse(km, "km");
    }
    return new LocationDistanceResponse(Math.floor(distanceInMeters), "m");
  }
}