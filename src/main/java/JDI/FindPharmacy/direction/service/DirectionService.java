package JDI.FindPharmacy.direction.service;

import JDI.FindPharmacy.api.dto.DocumentDto;
import JDI.FindPharmacy.api.service.KakaoCategorySearchService;
import JDI.FindPharmacy.direction.entity.Direction;
import JDI.FindPharmacy.direction.repository.DirectionRepository;
import JDI.FindPharmacy.pharmacy.dto.PharmacyDto;
import JDI.FindPharmacy.pharmacy.service.PharmacySearchService;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private static final int MAX_SEARCH_COUNT = 3;  // 약국 최대 검색 개수
    private static final double RADIUS_KM = 10.0;   // 검색 반경

    private final PharmacySearchService pharmacySearchService;
    private final DirectionRepository directionRepository;
    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final Base62Service base62Service;

    // Direction 저장
    @Transactional
    public List<Direction> saveAll(List<Direction> directionList) {
        if (CollectionUtils.isEmpty(directionList)) return Collections.emptyList(); // validation 체크
        return directionRepository.saveAll(directionList);
    }

    public Direction findById(String encodedId) {
        Long decodedId = base62Service.decodeDirectionId(encodedId);
        return directionRepository.findById(decodedId).orElse(null);
    }

    // 가장 가까운 약국 3개까지 추천
    public List<Direction> buildDirectionList(DocumentDto documentDto) {

        // null 인지 체크
        if (Objects.isNull(documentDto)) return Collections.emptyList();

        // 약국 데이터 조회
        return pharmacySearchService.searchPharmacyDtoList()
                .stream().map(pharmacyDto ->
                        Direction.builder()
                                .inputAddress(documentDto.getAddressName())
                                .inputLatitude(documentDto.getLatitude())
                                .inputLongitude(documentDto.getLongitude())
                                .targetPharmacyName(pharmacyDto.getPharmacyName())
                                .targetAddress(pharmacyDto.getPharmacyAddress())
                                .targetLatitude(pharmacyDto.getLatitude())
                                .targetLongitude(pharmacyDto.getLongitude())
                                .distance(
                                    calculateDistance(documentDto.getLatitude(), documentDto.getLongitude(),
                                            pharmacyDto.getLatitude(), pharmacyDto.getLongitude())
                                )
                                .build())
                .filter(direction -> direction.getDistance() <= RADIUS_KM)
                .sorted(Comparator.comparing(Direction::getDistance))
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    public List<Direction> buildDirectionListByCategoryApi(DocumentDto documentDto) {

        // null 인지 체크
        if (Objects.isNull(documentDto)) return Collections.emptyList();

        // 약국 데이터 조회
        return kakaoCategorySearchService
                .requestPharmacyCategorySearch(documentDto.getLatitude(), documentDto.getLongitude(), RADIUS_KM)
                .getDocumentDtoList()
                .stream().map(pharmacyDto ->
                        Direction.builder()
                                .inputAddress(documentDto.getAddressName())
                                .inputLatitude(documentDto.getLatitude())
                                .inputLongitude(documentDto.getLongitude())
                                .targetPharmacyName(pharmacyDto.getPlaceName())
                                .targetAddress(pharmacyDto.getAddressName())
                                .targetLatitude(pharmacyDto.getLatitude())
                                .targetLongitude(pharmacyDto.getLongitude())
                                .distance(pharmacyDto.getDistance() * 0.001)
                                .build())
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

}
