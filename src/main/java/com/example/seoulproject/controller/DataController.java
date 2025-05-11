package com.example.seoulproject.controller;

import com.example.seoulproject.dto.response.data.BudgetInfoDto;
import com.example.seoulproject.dto.response.data.BudgetTop10Dto;
import com.example.seoulproject.dto.response.data.CitizenBudgetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.seoulproject.controller.ColorPair.hslToHex;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DataController {

    @Value("${seoul.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public Object getBudgetData(@RequestParam(defaultValue = "1") int page) {
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize + 1;
        int endIndex = page * pageSize;

        String url = UriComponentsBuilder.fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        return restTemplate.getForObject(url, Map.class);
    }

    // 서울시 행정 예산 - 국비(NATN_CURR_AMT), 도비(SIDO_CURR_AMT), 편성액(COMPO_AMT), 소계(SUB_SUM_CURR_AMT)
    @GetMapping("/budget/simple")
    public List<BudgetInfoDto> getSimpleBudgetData(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(name = "field") String field) {
        int pageSize = 10;
        int fetchSize = 100;
        int startIndex = 1;
        List<BudgetInfoDto> result = new ArrayList<>();

        DecimalFormat formatter = new DecimalFormat("#,###");

        while (result.size() < pageSize) {
            int endIndex = startIndex + fetchSize - 1;

            String url = UriComponentsBuilder.fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> rows = (List<Map<String, Object>>)
                    ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

            if (rows == null || rows.isEmpty()) break;

            for (Map<String, Object> row : rows) {
                String deptName = (String) row.get("DBIZ_NM");
                Object rawValue = row.getOrDefault(field, 0);
                String fieldName = (String) row.get("FLD_NM");

                double value;
                try {
                    value = Double.parseDouble(rawValue.toString());
                } catch (NumberFormatException e) {
                    continue;
                }

                if (value <= 0) continue;

                String formattedValue = formatter.format(value);

                ColorPair colors = ColorPair.getColorPairForField(fieldName);

                result.add(new BudgetInfoDto(deptName, formattedValue, fieldName, colors.getLightColor(), colors.getDarkColor()));

                if (result.size() >= pageSize) break;
            }

            startIndex += fetchSize;
        }
        return result;
    }


    // 서울시 행정 예산 top10
    @GetMapping("/budget/top10")
    public List<BudgetTop10Dto> getTop10BudgetData(@RequestParam(name = "field") String field) {
        int pageSize = 1000;
        int startIndex = 1;
        int endIndex = 1000;

        String url = UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

        DecimalFormat formatter = new DecimalFormat("#,###");

        return rows.stream()
                .filter(row -> row.get(field) != null)
                .sorted((a, b) -> {
                    double aVal = Double.parseDouble(a.get(field).toString());
                    double bVal = Double.parseDouble(b.get(field).toString());
                    return Double.compare(bVal, aVal); // 내림차순 정렬
                })
                .limit(10)
                .map(row -> {
                    String deptName = (String) row.get("DBIZ_NM");
                    String fieldValue;
                    try {
                        double number = Double.parseDouble(row.get(field).toString());
                        fieldValue = formatter.format(number);
                    } catch (NumberFormatException e) {
                        fieldValue = "0";
                    }
                    return new BudgetTop10Dto(deptName, fieldValue);
                })
                .collect(Collectors.toList());
    }

    // 시민 참여 예산
    @GetMapping("/budget/citizen")
    public List<CitizenBudgetDto> getCitizenBudgetData(@RequestParam(defaultValue = "1") int page) {
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize + 1;
        int endIndex = page * pageSize;

        String url = UriComponentsBuilder.fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/bizSuggExecutionInfo/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("bizSuggExecutionInfo")).get("row");

        DecimalFormat formatter = new DecimalFormat("#,###");

        return rows.stream()
                .map(row -> {
                    String bizName = (String) row.getOrDefault("BIZ_NM", "-");
                    String year = String.valueOf(row.getOrDefault("FIS_YEAR", "-"));
                    String location = (String) row.getOrDefault("BUD_LOC", "-");

                    Object rawBudget = row.get("BUD_COST");
                    String budgetCost;
                    try {
                        double number = Double.parseDouble(rawBudget.toString());
                        budgetCost = formatter.format(number);
                    } catch (Exception e) {
                        budgetCost = "0";
                    }

                    return new CitizenBudgetDto(bizName, budgetCost, year, location);
                })
                .collect(Collectors.toList());
    }

}
