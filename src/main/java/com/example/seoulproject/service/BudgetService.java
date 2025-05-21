package com.example.seoulproject.service;

import com.example.seoulproject.controller.ColorPair;
import com.example.seoulproject.dto.response.ResponseDto;
import com.example.seoulproject.dto.response.data.BudgetInfoDto;
import com.example.seoulproject.dto.response.data.BudgetTop10Dto;
import com.example.seoulproject.dto.response.data.CitizenBudgetDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {
    @Value("${seoul.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final DecimalFormat formatter = new DecimalFormat("#,###");

    // 서울시 행정 예산 가져오기
    public List<BudgetInfoDto> getSimpleBudgetData(int page, String field) {
        int pageSize = 10;
        int currentPage = page;
        int fetchSize = 100;
        int startIndex = (currentPage - 1) * pageSize + 1;
        List<BudgetInfoDto> result = new ArrayList<>();

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


    // 서울시 행정 예산 키워드 검색
    public List<BudgetInfoDto> searchAdminBudget(String keyword) {
        int startIndex = 1;
        int endIndex = 1000;

        String url = UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

        return rows.stream()
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
    }

    // 서울시 행정 예산 top10
    public List<BudgetTop10Dto> getTop10BudgetData(String field) {
        String url = buildUrl("FiosTbmTecurramt", 1, 1000);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = extractRows(response, "FiosTbmTecurramt");

        return rows.stream()
                .filter(row -> row.get(field) != null)
                .sorted((a, b) -> {
                    double aVal = Double.parseDouble(a.get(field).toString());
                    double bVal = Double.parseDouble(b.get(field).toString());
                    return Double.compare(bVal, aVal); // 내림차순
                })
                .limit(10)
                .map(row -> new BudgetTop10Dto(
                        (String) row.get("DBIZ_NM"),
                        formatValue(row.get(field))
                ))
                .collect(Collectors.toList());
    }

    //
    public List<CitizenBudgetDto> getCitizenBudgetData(int page) {
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize + 1;
        int endIndex = page * pageSize;

        String url = buildUrl("bizSuggExecutionInfo", startIndex, endIndex);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = extractRows(response, "bizSuggExecutionInfo");

        return rows.stream()
                .map(row -> new CitizenBudgetDto(
                        (String) row.getOrDefault("BIZ_NM", "-"),
                        formatValue(row.get("BUD_COST")),
                        String.valueOf(row.getOrDefault("FIS_YEAR", "-")),
                        (String) row.getOrDefault("BUD_LOC", "-")
                ))
                .collect(Collectors.toList());
    }

    // 분야별 소계 예산 Top5
    public List<BudgetTop10Dto> getTop5SubSumByField() {
        String url = buildUrl("FiosTbmTecurramt", 1, 1000);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = extractRows(response, "FiosTbmTecurramt");

        Map<String, Double> sumByField = new HashMap<>();
        for (Map<String, Object> row : rows) {
            String fieldName = (String) row.get("FLD_NM");
            Object rawValue = row.get("SUB_SUM_CURR_AMT");
            if (fieldName == null || rawValue == null) continue;
            try {
                double value = Double.parseDouble(rawValue.toString());
                sumByField.put(fieldName, sumByField.getOrDefault(fieldName, 0.0) + value);
            } catch (NumberFormatException ignored) {}
        }

        return sumByField.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new BudgetTop10Dto(entry.getKey(), formatter.format(entry.getValue())))
                .collect(Collectors.toList());
    }

    // 시민 예산 키워드 검색
    public ResponseEntity<ResponseDto<List<CitizenBudgetDto>>> searchCitizenBudget(String keyword) {
        String url = buildUrl("bizSuggExecutionInfo", 1, 1000);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = extractRows(response, "bizSuggExecutionInfo");

        List<CitizenBudgetDto> result = rows.stream()
                .filter(row -> {
                    String name = (String) row.get("BIZ_NM");
                    return name != null && name.contains(keyword);
                })
                .map(row -> new CitizenBudgetDto(
                        (String) row.getOrDefault("BIZ_NM", "-"),
                        formatValue(row.get("BUD_COST")),
                        String.valueOf(row.getOrDefault("FIS_YEAR", "-")),
                        (String) row.getOrDefault("BUD_LOC", "-")
                ))
                .toList();

        if (result.isEmpty()) {
            return ResponseEntity.ok(ResponseDto.res(HttpStatus.OK, "일치하는 시민예산 항목이 없습니다."));
        } else {
            return ResponseEntity.ok(ResponseDto.res(HttpStatus.OK, "예산 항목 검색 성공", result));
        }
    }

    private String buildUrl(String apiName, int start, int end) {
        return UriComponentsBuilder
                .fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/" + apiName + "/" + start + "/" + end)
                .toUriString();
    }

    private List<Map<String, Object>> extractRows(Map<String, Object> response, String rootKey) {
        if (response == null || !response.containsKey(rootKey)) return Collections.emptyList();
        Map<String, Object> data = (Map<String, Object>) response.get(rootKey);
        return (List<Map<String, Object>>) data.getOrDefault("row", Collections.emptyList());
    }

    private String formatValue(Object value) {
        try {
            return formatter.format(Double.parseDouble(value.toString()));
        } catch (Exception e) {
            return "0";
        }
    }


}
