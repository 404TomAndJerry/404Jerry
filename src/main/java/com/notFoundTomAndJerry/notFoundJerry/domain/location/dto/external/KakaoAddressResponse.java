package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoAddressResponse {

  // 카카오 응답의 최상위 리스트
  @JsonProperty("documents")
  private List<Document> documents;

  @Getter
  @NoArgsConstructor
  public static class Document {

    @JsonProperty("address")
    private Address address;

    @JsonProperty("road_address")
    private RoadAddress roadAddress; // 도로명 주소
  }

  @Getter
  @NoArgsConstructor
  public static class Address {

    @JsonProperty("address_name")
    private String addressName; // 예: 서울특별시 강남구 역삼동 123-4

    @JsonProperty("region_1depth_name")
    private String region1DepthName; // 예: 서울특별시

    @JsonProperty("region_2depth_name")
    private String regionName; // 예: 강남구
  }

  @Getter
  @NoArgsConstructor
  public static class RoadAddress {

    @JsonProperty("address_name")
    private String addressName; // 도로명 주소 전체 명칭
  }
}