package JDI.FindPharmacy.pharmacy.service;

import JDI.FindPharmacy.api.dto.DocumentDto;
import JDI.FindPharmacy.api.dto.KakaoApiResponseDto;
import JDI.FindPharmacy.api.service.KakaoAddressSearchService;
import JDI.FindPharmacy.direction.dto.OutputDto;
import JDI.FindPharmacy.direction.entity.Direction;
import JDI.FindPharmacy.direction.service.DirectionService;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;
    private final DirectionService directionService;

    public List<OutputDto> recommendPharmacyList(String address) {
        // 고객의 주소(문자열)를 받으면 카카오 API 를 통해 위치 기반 데이터로 변환
        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if (Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentDtoList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: {}", address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentDtoList().get(0);

//        List<Direction> directionList = directionService.buildDirectionList(documentDto);
        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);

        return directionService.saveAll(directionList)
                .stream()
                .map(t -> convertToOutputDto(t))
                .collect(Collectors.toList());
    }

    private OutputDto convertToOutputDto(Direction direction) {
        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl("todo")   // todo
                .roadViewUrl("todo")    // todo
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
