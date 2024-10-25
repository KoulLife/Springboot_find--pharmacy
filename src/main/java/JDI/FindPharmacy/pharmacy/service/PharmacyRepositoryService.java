package JDI.FindPharmacy.pharmacy.service;

import JDI.FindPharmacy.pharmacy.entity.Pharmacy;
import JDI.FindPharmacy.pharmacy.repository.PharmacyRepository;
import jakarta.transaction.Transactional;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class PharmacyRepositoryService {

    private final PharmacyRepository pharmacyRepository;

    @Transactional
    public void updateAddress(Long id, String address) {
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if (Objects.isNull(entity)) {
            log.error("[PharmacyRepositoryService updateAddress] not found id : {}", id);
            return;
        }

        entity.changePharmacyAddress(address);
    }

    public void updateAddressWithoutTransactional(Long id, String address) {
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if (Objects.isNull(entity)) {
            log.error("[PharmacyRepositoryService updateAddress] not found id : {}", id);
            return;
        }

        entity.changePharmacyAddress(address);
    }

}
