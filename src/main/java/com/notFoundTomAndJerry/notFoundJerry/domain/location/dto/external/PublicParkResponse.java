package com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external;

import java.util.List;
import lombok.Getter;

@Getter
public class PublicParkResponse {

  private Response response;

  @Getter
  public static class Response {

    private Body body;
  }

  @Getter
  public static class Body {

    private List<ParkItem> items;
  }

  @Getter
  public static class ParkItem {

    private String latitude; // 위도 (y)
    private String longitude; // 경도 (x)
    private String parkNm; // 공원명
    private String manageNo; // 공원관리번호
    private String parkSe; // 공원구분
    private String parkAr; // 공원면적
  }
}