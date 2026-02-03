package com.notFoundTomAndJerry.notFoundJerry.domain.location.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.PublicParkResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.PublicParkResponse.ParkItem;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationExternalApiService {

  @Value("${public-data.api.url}")
  private String apiUrl;

  @Value("${public-data.api.key}")
  private String serviceKey;

  private final RestTemplate restTemplate = new RestTemplate();

  public List<ParkItem> fetchParkData(int pageNo, int numOfRows) {
    // 공공데이터 API는 키에 특수문자가 많아 UriComponentsBuilder로 안전하게 생성해야 함
    URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
        .queryParam("serviceKey", serviceKey)
        .queryParam("type", "json")
        .queryParam("pageNo", pageNo)
        .queryParam("numOfRows", numOfRows)
        .build(true) // 인코딩된 키를 그대로 사용할 경우 true
        .toUri();

    try {
      log.info("공공데이터 호출 시작: pageNo={}, numOfRows={}", pageNo, numOfRows);
      PublicParkResponse response = restTemplate.getForObject(uri, PublicParkResponse.class);

      if (response != null && response.getResponse() != null
          && response.getResponse().getBody() != null) {
        return response.getResponse().getBody().getItems();
      }
    } catch (Exception e) {
      log.error("API 호출 에러: {}", e.getMessage());
    }
    return Collections.emptyList();
  }
}