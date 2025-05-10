package com.example.seoulproject.controller;

import com.example.seoulproject.dto.response.data.BudgetInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    // 국비(NATN_CURR_AMT), 편성액(COMPO_AMT), 소계(SUB_SUM_CURR_AMT)
    @GetMapping("/budget/simple")
    public List<BudgetInfoDto> getSimpleBudgetData(@RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(name = "field") String field) {
        int pageSize = 10;
        int startIndex = (page - 1) * pageSize + 1;
        int endIndex = page * pageSize;

        String url = UriComponentsBuilder.fromHttpUrl("http://openapi.seoul.go.kr:8088/" + apiKey + "/json/FiosTbmTecurramt/" + startIndex + "/" + endIndex)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> rows = (List<Map<String, Object>>)
                ((Map<String, Object>) response.get("FiosTbmTecurramt")).get("row");

        DecimalFormat formatter = new DecimalFormat("#,###");
        Map<String, ColorPair> colorMap = new HashMap<>();
        Random random = new Random();

        return rows.stream()
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

                    // 색상쌍 생성 (분야별 고정)
                    ColorPair colors = colorMap.computeIfAbsent(fieldName, k -> {
                        float hue = random.nextInt(360);
                        float saturation = 0.5f;
                        String light = hslToHex(hue, saturation, 0.85f); // 밝은 색
                        String dark = hslToHex(hue, saturation, 0.35f);  // 진한 색
                        return new ColorPair(light, dark);
                    });

                    return new BudgetInfoDto(deptName, formattedValue, fieldName, colors.getLightColor(), colors.getDarkColor());
                })
                .collect(Collectors.toList());
    }

}
