package com.notFoundTomAndJerry.notFoundJerry.domain.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(columnDefinition = "POINT SRID 4326", nullable = false)
  private Point point;

  @Column(nullable = false)
  private String address; // 전체 주소

  @Column(nullable = false)
  private String regionName; // 구 단위

  @Builder
  public Location(Point point, String address, String regionName) {
    this.point = point;
    this.address = address;
    this.regionName = regionName;
  }
}