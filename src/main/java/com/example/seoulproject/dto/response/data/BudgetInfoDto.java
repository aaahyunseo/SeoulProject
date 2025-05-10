package com.example.seoulproject.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetInfoDto {
    private String deptName;
    private String value;
    private String fieldName;
    private String bgColor;
    private String textColor;
}