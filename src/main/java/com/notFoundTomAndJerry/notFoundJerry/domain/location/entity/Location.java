package com.notFoundTomAndJerry.notFoundJerry.domain.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "POINT SRID 4326", nullable = false)
  private Point point; // 위경도 공간 데이터

  @Column(nullable = true)
  private String address; // 전체 주소

  @Column(nullable = true)
  private String regionName; // 구 단위

  @Column(nullable = false, unique = true)
  private String manageNo; // 공원관리번호
  private String parkNm; // 공원명
  private String parkSe; // 공원구분
  private String parkAr; // 공원면적

  /**
   * 일단 배치 확인 전까지 false로 두는게 안전! 배치가 카카오 API로 주소 채우고 유효 지역 확인 끝나면 true로 바꿔주기
   */
  @Column(nullable = false)
  @Builder.Default
  private boolean isValid = false; // 배치가 관리하는 유효성 플래그

  // 카카오 API로부터 받은 정보 반영
  public void updateAddressInfo(String address, String regionName) {
    this.address = address;
    this.regionName = regionName;
  }

  // 공공데이터 API로부터 받은 공원 정보 업데이트
  public void updateFromPublicApi(String manageNo, String parkNm, String parkSe, String parkAr,
      String lat, String lon) {
    // 위경도 -> Point 변환
    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    double longitude = Double.parseDouble(lon);
    double latitude = Double.parseDouble(lat);
    this.point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

    this.manageNo = manageNo;
    this.parkNm = parkNm;
    this.parkSe = parkSe;
    this.parkAr = parkAr;
  }

  // 배치가 유효 구역 대조 후 상태 변경 시 사용
  public void updateValidity(boolean isValid) {
    this.isValid = isValid;
  }
}