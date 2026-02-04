package com.notFoundTomAndJerry.notFoundJerry.batch;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.PublicParkResponse;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.PublicParkResponse.ParkItem;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.BusinessException;
import com.notFoundTomAndJerry.notFoundJerry.global.exception.domain.LocationErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublicDataCollector {

  private final LocationRepository locationRepository;
  private final RestTemplate restTemplate;

  @Value("${public-data.api.url}")
  private String endPoint;

  @Value("${public-data.api.key}")
  private String serviceKey;

  public void collect(int numOfRows) {
    String url = endPoint + "?serviceKey=" + serviceKey + "&type=json&numOfRows=" + numOfRows;

    try {
      log.info("공공데이터 수집 시작");
      ResponseEntity<PublicParkResponse> response = restTemplate.getForEntity(url,
          PublicParkResponse.class);

      if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
        log.warn("공공데이터 서버 상태 불량");
        return;
      }

      List<ParkItem> items = response.getBody().getResponse().getBody()
          .getItems();
      for (var item : items) {
        // 어린이, 소공원 제외 및 면적 1500 이상만
        if (isAppropriateForGame(item)) {
          saveLocation(item);
        }
      }
      log.info("필터링 완료");
    } catch (Exception e) {
      log.error("수집 중 에러 발생(서버 장애 등): " + e.getMessage());
    }
  }

  private boolean isAppropriateForGame(PublicParkResponse.ParkItem item) {
    if (item.getParkSe() == null || item.getParkAr() == null) {
      return false;
    }

    String parkSe = item.getParkSe().trim();

    // 어린이, 소공원 필터링
    if (parkSe.contains("어린이") || parkSe.contains("소공원")) {
      log.info(">>>> [필터링 탈락] 공원 구분: {}, 이름: {}", parkSe, item.getParkNm());
      return false;
    }

    // 면적 필터링
    try {
      double parkArea = Double.parseDouble(item.getParkAr());
      if (parkArea < 1500) {
        log.info(">>>> [면적 탈락] 면적: {}, 이름: {}", parkArea, item.getParkNm());
        return false;
      }
    } catch (Exception e) {
      log.warn("면적 데이터 변환 실패: {} - {}", item.getParkNm(), item.getParkAr());
      throw new BusinessException(LocationErrorCode.INVALID_LOCATION_DATA);
    }

    return true;
  }

  private void saveLocation(PublicParkResponse.ParkItem item) {
    try {
      if (item.getManageNo() == null) {
        return;
      }

      // 중복 체크: 이미 있으면 가져오고 없으면 새로 생성
      Location location = locationRepository.findByManageNo(item.getManageNo())
          .orElseGet(() -> Location.builder()
              .manageNo(item.getManageNo())
              .build());

      // 필수값 누락되었을 경우
      if (item.getLatitude() == null || item.getLongitude() == null) {
        throw new BusinessException(LocationErrorCode.INVALID_COORDINATES);
      }

      // 공공데이터 정보 반영 (좌표 변환 등)
      location.updateFromPublicApi(
          item.getManageNo(), item.getParkNm(), item.getParkSe(),
          item.getParkAr(), item.getLatitude(), item.getLongitude()
      );

      // 필터를 통과했으므로 즉시 활성화
      location.updateValidity(true);

      // DB 저장
      locationRepository.save(location);

    } catch (Exception e) {
      log.error("장소 저장 중 에러 발생 ({}): {}", item.getParkNm(), e.getMessage());
    }
  }
}