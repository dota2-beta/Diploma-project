package com.abs.newssystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddNewsRequestDto {
    private String title;
    private String content;
    private String link;
    private String dateStr;
}
