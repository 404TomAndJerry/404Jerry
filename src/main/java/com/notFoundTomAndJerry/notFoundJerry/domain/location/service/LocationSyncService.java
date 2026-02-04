package com.notFoundTomAndJerry.notFoundJerry.domain.location.service;

import com.notFoundTomAndJerry.notFoundJerry.domain.location.dto.external.PublicParkResponse.ParkItem;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.entity.Location;
import com.notFoundTomAndJerry.notFoundJerry.domain.location.repository.LocationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationSyncService {

  private final LocationExternalApiService locationExternalApiService;
  private final LocationRepository locationRepository;

  @Transactional
  public void syncLocationData(int pageNo, int numOfRows) {
    // 외부 API에서 데이터 가져오기
    List<ParkItem> items = locationExternalApiService.fetchParkData(pageNo, numOfRows);

    if (items.isEmpty()) {
      return;
    }

    log.info("가져온 데이터 개수: {}", items.size());
    for (ParkItem item : items) {
      log.info("공원 이름: {}, 관리번호: {}", item.getParkNm(), item.getManageNo());
      // 이미 있는 공원인지 중복 체크 (공원 관리번호로)
      Location location = locationRepository.findByManageNo(item.getManageNo()).orElse(null);

      if (location == null) {
        // 신규 생성 시에는 아주 최소한의 정보로 먼저 생성
        location = Location.builder()
            .manageNo(item.getManageNo())
            .point(new GeometryFactory(new PrecisionModel(), 4326)
                .createPoint(new Coordinate(0, 0))) // 임시 값
            .build();
        log.info("신규 공원 생성 준비: {}", item.getParkNm());
      }

      // 공공데이터 정보 반영
      location.updateFromPublicApi(
          item.getManageNo(),
          item.getParkNm(),
          item.getParkSe(),
          item.getParkAr(),
          item.getLatitude(),
          item.getLongitude()
      );
      location = locationRepository.saveAndFlush(location);
      log.info("건별 저장 강제 수행 완료: {}, ID: {}", item.getParkNm(), location.getId());
    }
    locationRepository.flush();
  }
}