package com.notFoundTomAndJerry.notFoundJerry.batch;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.KakaoAddressResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocationSyncBatch {

  private final LocationRepository locationRepository;
  private final RestTemplate restTemplate;

  @Value("${kakao.api.key}")
  private String kakaoKey;

  private final String KAKAO_URL = "https://dapi.kakao.com/v2/local/geo/coord2address.json";

  @Transactional
  public void run() {
    // 주소가 없는 로케이션들 조회
    List<Location> targets = locationRepository.findByAddressIsNull();
    log.info("카카오 주소 동기화 시작. 대상 건수: {}건", targets.size());

    if (targets.isEmpty()) {
      log.info("업데이트할 대상이 없습니다.");
      return;
    }

    for (Location location : targets) {
      try {
        updateAddressFromKakao(location);
        // API 호출 간격 조절 (초당 호출 제한 방지용)
        Thread.sleep(50);
      } catch (Exception e) {
        log.error("주소 업데이트 중 에러 발생 (ID: {}): {}", location.getId(), e.getMessage());
      }
    }
    log.info("모든 주소 동기화 완료");
  }

  private void updateAddressFromKakao(Location location) {
    // 엔티티의 Point에서 위경도 추출 (x가 경도, y가 위도)
    double longitude = location.getPoint().getX();
    double latitude = location.getPoint().getY();

    // 카카오 API 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + kakaoKey);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    // URL 빌드
    String url = UriComponentsBuilder.fromHttpUrl(KAKAO_URL)
        .queryParam("x", longitude)
        .queryParam("y", latitude)
        .build().toUriString();

    try {
      ResponseEntity<KakaoAddressResponse> response = restTemplate.exchange(
          url, HttpMethod.GET, entity, KakaoAddressResponse.class);

      if (response.getBody() != null && !response.getBody().getDocuments().isEmpty()) {
        var doc = response.getBody().getDocuments().get(0);

        // 도로명 주소가 있으면 우선 사용, 없으면 지번 주소 사용
        String addressName = (doc.getRoadAddress() != null)
            ? doc.getRoadAddress().getAddressName()
            : doc.getAddress().getAddressName();

        // 구 정보 (강남구 등)
        String regionName = doc.getAddress().getRegionName();

        // 엔티티 업데이트
        location.updateAddressInfo(addressName, regionName);
        log.info("업데이트 성공: {} -> {}", location.getParkNm(), addressName);
      } else {
        log.warn("주소 정보 없음: {}", location.getParkNm());
      }
    } catch (Exception e) {
      log.error("카카오 API 호출 실패 (ID: {}): {}", location.getId(), e.getMessage());
    }
  }
}