package com.nubianlanguages.contentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContributorProgressRequest {


    @NotBlank
    private Long contirbId;

    @NotBlank
    private  Long numberOfRecordings;
    @NotBlank
    private  Long rangeStart;



}
