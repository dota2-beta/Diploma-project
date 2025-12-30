package com.abs.newssystem.Dto;

import lombok.Data;

import java.util.Map;

@Data
public class PredictionResponseDto {
    // {"probabilities": {"sport": 0.9, ...}}
    private Map<String, Double> probabilities;
}
