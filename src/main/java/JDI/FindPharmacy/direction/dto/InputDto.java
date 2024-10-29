package JDI.FindPharmacy.direction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InputDto {
    private String address;

    // 기본 생성자
    public InputDto() {
    }

    // 생성자
    public InputDto(String address) {
        this.address = address;
    }

}