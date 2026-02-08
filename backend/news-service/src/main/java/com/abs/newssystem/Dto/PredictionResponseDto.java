package com.abs.newssystem.Dto;

import lombok.Data;

import java.util.Map;

@Data
public class PredictionResponseDto {
    private Map<String, Double> probabilities;
}
