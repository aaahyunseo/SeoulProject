package com.example.seoulproject.controller;

import com.example.seoulproject.dto.response.ResponseDto;
import com.example.seoulproject.dto.response.data.BudgetInfoDto;
import com.example.seoulproject.dto.response.data.BudgetTop10Dto;
import com.example.seoulproject.dto.response.data.CitizenBudgetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        int currentPage = page;
        int fetchSize = 100;
        int startIndex = (currentPage - 1) * pageSize + 1;
        List<BudgetInfoDto> result = new ArrayList<>();
        DecimalFormat formatter = new DecimalFormat("#,###");

        while (result.size() < pageSize) {
            int endIndex = startIndex + fetchSize - 1;

            String url = UriComponentsBuilder
                    .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                    .toUriString();

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, Object>> rows = (List<Map<String, Object>>)
                    ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

            if (rows == null || rows.isEmpty()) break;

            List<BudgetInfoDto> validItems = rows.stream()
                    .filter(row -> {
                        Object rawValue = row.get(field);
                        if (rawValue == null) return false;
                        try {
                            return Double.parseDouble(rawValue.toString()) > 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .map(row -> {
                        String deptName = (String) row.get("DBIZ_NM");
                        Object rawValue = row.getOrDefault(field, 0);
                        String fieldName = (String) row.get("FLD_NM");

                        String formattedValue;
                        try {
                            double number = Double.parseDouble(rawValue.toString());
                            formattedValue = formatter.format(number);
                        } catch (NumberFormatException e) {
                            formattedValue = "0";
                        }

                        ColorPair colors = ColorPair.getColorPairForField(fieldName);
                        return new BudgetInfoDto(deptName, formattedValue, fieldName, colors.getLightColor(), colors.getDarkColor());
                    })
                    .collect(Collectors.toList());

            for (BudgetInfoDto item : validItems) {
                if (result.size() >= pageSize) break;
                result.add(item);
            }

            startIndex = endIndex + 1;
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

    @GetMapping("/budget-by-field/top5")
    public List<BudgetTop10Dto> getTop5SubSumByField() {
        int startIndex = 1;
        int endIndex = 1000;

        String url = UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response == null || !response.containsKey("FiosTbmTecurramt")) return Collections.emptyList();

        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

        if (rows == null || rows.isEmpty()) return Collections.emptyList();

        Map<String, Double> sumByField = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String fieldName = (String) row.get("FLD_NM");
            Object rawValue = row.get("SUB_SUM_CURR_AMT");

            if (fieldName == null || rawValue == null) continue;

            try {
                double value = Double.parseDouble(rawValue.toString());
                sumByField.put(fieldName, sumByField.getOrDefault(fieldName, 0.0) + value);
            } catch (NumberFormatException e) {
                // 예산 값이 숫자가 아닐 경우 무시
            }
        }

        DecimalFormat formatter = new DecimalFormat("#,###");

        return sumByField.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(5)
                .map(entry -> new BudgetTop10Dto(entry.getKey(), formatter.format(entry.getValue())))
                .collect(Collectors.toList());
    }

    // 서울시 행정 예산 키워드 검색
    @GetMapping("/budget/search/admin")
    public ResponseEntity<ResponseDto<List<BudgetInfoDto>>> searchAdminBudget(@RequestParam String keyword) {
        int startIndex = 1;
        int endIndex = 1000;

        String url = UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

        DecimalFormat formatter = new DecimalFormat("#,###");

        List<BudgetInfoDto> result = rows.stream()
                .filter(row -> {
                    String name = (String) row.get("DBIZ_NM");
                    return name != null && name.contains(keyword);
                })
                .map(row -> {
                    String deptName = (String) row.get("DBIZ_NM");
                    String fieldName = (String) row.get("FLD_NM");
                    Object rawValue = row.get("SUB_SUM_CURR_AMT");
                    String formattedValue;
                    try {
                        double number = Double.parseDouble(rawValue.toString());
                        formattedValue = formatter.format(number);
                    } catch (Exception e) {
                        formattedValue = "0";
                    }
                    ColorPair colors = ColorPair.getColorPairForField(fieldName);
                    return new BudgetInfoDto(deptName, formattedValue, fieldName, colors.getLightColor(), colors.getDarkColor());
                })
                .toList();

        if (result.isEmpty()) {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "일치하는 행정예산 항목이 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "예산 항목 검색 성공", result), HttpStatus.OK);
        }
    }

    // 시민 예산 키워드 검색
    @GetMapping("/budget/search/citizen")
    public ResponseEntity<ResponseDto<List<CitizenBudgetDto>>> searchCitizenBudget(@RequestParam String keyword) {
        int startIndex = 1;
        int endIndex = 1000;

        String url = UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/bizSuggExecutionInfo/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("bizSuggExecutionInfo")).get("row");

        DecimalFormat formatter = new DecimalFormat("#,###");

        List<CitizenBudgetDto> result = rows.stream()
                .filter(row -> {
                    String name = (String) row.get("BIZ_NM");
                    return name != null && name.contains(keyword);
                })
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
                .toList();

        if (result.isEmpty()) {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "일치하는 시민예산 항목이 없습니다."), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(ResponseDto.res(HttpStatus.OK, "예산 항목 검색 성공", result), HttpStatus.OK);
        }
    }

}
