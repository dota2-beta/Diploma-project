package com.abs.newssystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MlResultMessageDto {
    private Long id;
    private Map<String, Double> probabilities;
}
