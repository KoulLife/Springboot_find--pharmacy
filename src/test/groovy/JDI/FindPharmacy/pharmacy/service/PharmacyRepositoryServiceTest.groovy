package JDI.FindPharmacy.pharmacy.service

import JDI.FindPharmacy.AbstractIntegrationContainerBaseTest
import JDI.FindPharmacy.pharmacy.entity.Pharmacy
import JDI.FindPharmacy.pharmacy.repository.PharmacyRepository
import org.springframework.beans.factory.annotation.Autowired

class PharmacyRepositoryServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private PharmacyRepository pharmacyRepository;

    @Autowired
    private PharmacyRepositoryService pharmacyRepositoryService;

    def setup() {
        pharmacyRepository.deleteAll()
    }

    def "self invocation - roll back)"() {
        given:
        String address = "서울 특별시 성북구 종암동"
        String name = "은혜 약국"
        double latitude = 36.11
        double longitude = 128.11

        def pharmacy = Pharmacy.builder()
                .pharmacyAddress(address)
                .pharmacyName(name)
                .latitude(latitude)
                .longitude(longitude)
                .build()

        when:
        pharmacyRepositoryService.bar(Arrays.asList(pharmacy))

        then:
        def e = thrown(RuntimeException.class)
        def result = pharmacyRepositoryService.findAll()
        result.size() == 1 // 트랜잭션이 적용되지 않음 (롤백 적용 X)

    }

    def "PharmacyRepository update - dirty checking success"() {
        given:
        String inputAddress = "서울 특별시 성북구 종암동"
        String modifiedAddress = "서울 특별시 광진구 구의동"
        String name = "은혜 약국"

        def pharmacy = Pharmacy.builder()
                .pharmacyName(name)
                .pharmacyAddress(inputAddress)
                .build()

        when:
        def entity = pharmacyRepository.save(pharmacy)
        pharmacyRepositoryService.updateAddress(entity.getId(), modifiedAddress)

        def result = pharmacyRepository.findAll()

        then:
        result.get(0).getPharmacyAddress() == modifiedAddress

    }

    def "PharmacyRepository update - dirty checking fail"() {
        given:
        String inputAddress = "서울 특별시 성북구 종암동"
        String modifiedAddress = "서울 특별시 광진구 구의동"
        String name = "은혜 약국"

        def pharmacy = Pharmacy.builder()
                .pharmacyName(name)
                .pharmacyAddress(inputAddress)
                .build()

        when:
        def entity = pharmacyRepository.save(pharmacy)
        pharmacyRepositoryService.updateAddressWithoutTransactional(entity.getId(), modifiedAddress)

        def result = pharmacyRepository.findAll()

        then:
        result.get(0).getPharmacyAddress() == inputAddress

    }

}
