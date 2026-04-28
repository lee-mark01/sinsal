package com.sinsal.controller;

import com.sinsal.dto.SinSalRequest;
import com.sinsal.dto.SinSalResponse;
import com.sinsal.model.SinSalInfo;
import com.sinsal.model.ThreePillars;
import com.sinsal.service.SajuCalculatorService;
import com.sinsal.service.SinSalDetectorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SinSalController {

    private final SajuCalculatorService sajuCalculator;
    private final SinSalDetectorService sinSalDetector;

    public SinSalController(SajuCalculatorService sajuCalculator,
                            SinSalDetectorService sinSalDetector) {
        this.sajuCalculator = sajuCalculator;
        this.sinSalDetector = sinSalDetector;
    }

    /**
     * POST /api/sinsals
     * Body: { "birthDate": "1990-05-15" }
     */
    @PostMapping("/sinsals")
    public ResponseEntity<SinSalResponse> getSinSals(@Valid @RequestBody SinSalRequest request) {
        ThreePillars pillars = sajuCalculator.calculate(request.getBirthDate());
        List<SinSalInfo> sinSalInfos = sinSalDetector.detect(pillars);

        String pillarsStr = pillars.getYearPillar()  + "년 " +
                            pillars.getMonthPillar() + "월 " +
                            pillars.getDayPillar()   + "일";

        List<SinSalResponse.SinSalItem> items = sinSalInfos.stream()
                .map(SinSalResponse.SinSalItem::from)
                .toList();

        return ResponseEntity.ok(new SinSalResponse(request.getBirthDate(), pillarsStr, items));
    }
}
