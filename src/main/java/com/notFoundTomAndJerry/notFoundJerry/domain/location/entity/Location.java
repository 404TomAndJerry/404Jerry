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
import org.locationtech.jts.geom.Point;

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

  @Column(nullable = false)
  private String address; // 전체 주소

  @Column(nullable = false)
  private String regionName; // 구 단위

  @Builder.Default
  @Column(nullable = false)
  private boolean isValid = true; // 배치가 관리하는 유효성 플래그

  // 배치가 유효 구역 대조 후 상태 변경 시 사용
  public void updateValidity(boolean isValid) {
    this.isValid = isValid;
  }
}