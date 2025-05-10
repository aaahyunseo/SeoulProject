package com.example.seoulproject.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BudgetTop10Dto {
    private String deptName;
    private String value;
}
