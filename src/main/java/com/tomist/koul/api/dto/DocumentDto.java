package com.tomist.koul.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {

    @JsonProperty("address_name")
    private String addressNave;

    @JsonProperty("y")
    private double latitude;

    @JsonProperty("x")
    private double longitude;
}
