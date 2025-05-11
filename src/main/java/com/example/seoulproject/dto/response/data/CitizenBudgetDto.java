package com.example.seoulproject.dto.response.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CitizenBudgetDto {
    private String bizName;
    private String budgetCost;
    private String year;
    private String location;
}

