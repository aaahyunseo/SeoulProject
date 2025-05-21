package com.example.seoulproject.controller;

import com.example.seoulproject.dto.response.ResponseDto;
import com.example.seoulproject.dto.response.data.BudgetInfoDto;
import com.example.seoulproject.dto.response.data.BudgetTop10Dto;
import com.example.seoulproject.dto.response.data.CitizenBudgetDto;
import com.example.seoulproject.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class BudgetController {

    private final BudgetService budgetService;

    /**
     * 서울시 행정 예산
     * 국비(NATN_CURR_AMT), 도비(SIDO_CURR_AMT), 편성액(COMPO_AMT), 소계(SUB_SUM_CURR_AMT)
     * **/
    @GetMapping("/budget/simple")
    public List<BudgetInfoDto> getSimpleBudgetData(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(name = "field") String field) {
        return budgetService.getSimpleBudgetData(page, field);
    }


    /**
     * 서울시 행정 예산 top10
     * **/
    @GetMapping("/budget/top10")
    public List<BudgetTop10Dto> getTop10BudgetData(@RequestParam(name = "field") String field) {
        return budgetService.getTop10BudgetData(field);
    }

    /**
     * 시민 참여 예산
     * **/
    @GetMapping("/budget/citizen")
    public List<CitizenBudgetDto> getCitizenBudgetData(@RequestParam(defaultValue = "1") int page) {
        return budgetService.getCitizenBudgetData(page);
    }

    /**
     * 분야별 소계 예산 Top5
     * **/
    @GetMapping("/budget-by-field/top5")
    public List<BudgetTop10Dto> getTop5SubSumByField() {
        return budgetService.getTop5SubSumByField();
    }

    /**
     * 서울시 행정 예산 키워드 검색
     * **/
    @GetMapping("/budget/search/admin")
    public ResponseEntity<ResponseDto<List<BudgetInfoDto>>> searchAdminBudget(@RequestParam String keyword) {
        List<BudgetInfoDto> result = budgetService.searchAdminBudget(keyword);
        if (result.isEmpty()) {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "일치하는 행정예산 항목이 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "예산 항목 검색 성공", result), HttpStatus.OK);
        }
    }

    /**
     * 시민 예산 키워드 검색
     * **/
    @GetMapping("/budget/search/citizen")
    public ResponseEntity<ResponseDto<List<CitizenBudgetDto>>> searchCitizenBudget(@RequestParam String keyword) {
        return budgetService.searchCitizenBudget(keyword);
    }

}