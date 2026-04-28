package com.sinsal;

import com.sinsal.model.SinSalInfo;
import com.sinsal.model.ThreePillars;
import com.sinsal.service.SajuCalculatorService;
import com.sinsal.service.SinSalDetectorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 생년월일 전수조사 테스트
 *
 * 1900-01-01 ~ 2010-12-31 범위의 날짜를 30일 간격으로 샘플링해
 * 사주/신살 계산 결과를 build/sinsal_bulk_result.csv 에 기록한다.
 *
 * 전체 날짜(매일)로 돌리고 싶다면 DATE_STEP_DAYS = 1 로 변경.
 */
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SinSalBulkTest {

    /** 샘플링 간격 (일). 1 = 매일 전수조사 (~40,000건), 30 = 월 단위 샘플 (~1,300건) */
    private static final int DATE_STEP_DAYS = 1;

    private static final LocalDate START_DATE = LocalDate.of(1900, 1, 1);
    private static final LocalDate END_DATE   = LocalDate.of(2010, 12, 31);

    private static final String OUTPUT_PATH = "build/sinsal_bulk_result.csv";

    @Autowired
    private SajuCalculatorService sajuCalculatorService;

    @Autowired
    private SinSalDetectorService sinSalDetectorService;

    /** 테스트 실행 중 수집된 결과 행 */
    private final List<String> rows = new ArrayList<>();

    // ── 날짜 스트림 생성 ────────────────────────────────────────────

    static Stream<LocalDate> dateProvider() {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = START_DATE;
        while (!current.isAfter(END_DATE)) {
            dates.add(current);
            current = current.plusDays(DATE_STEP_DAYS);
        }
        return dates.stream();
    }

    // ── 테스트 ──────────────────────────────────────────────────────

    @ParameterizedTest(name = "{0}")
    @MethodSource("dateProvider")
    void calculateSinSal(LocalDate birthDate) {
        ThreePillars pillars = sajuCalculatorService.calculate(birthDate);
        List<SinSalInfo> sinSals = sinSalDetectorService.detect(pillars);

        String sinSalNames = sinSals.stream()
                .map(SinSalInfo::getName)
                .reduce((a, b) -> a + "|" + b)
                .orElse("없음");

        String sinSalKeys = sinSals.stream()
                .map(SinSalInfo::getKey)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");

        String pillarsLabel = pillars.getYearPillar() + "년 "
                + pillars.getMonthPillar() + "월 "
                + pillars.getDayPillar() + "일";

        // birthDate, pillars, sinSal 개수, 신살명, 신살 key
        rows.add(String.format("%s,%s,%d,%s,%s",
                birthDate,
                pillarsLabel,
                sinSals.size(),
                sinSalNames,
                sinSalKeys));
    }

    // ── CSV 저장 ────────────────────────────────────────────────────

    @AfterAll
    void writeCsv() throws IOException {
        // build/ 디렉토리가 없으면 생성
        java.nio.file.Files.createDirectories(java.nio.file.Path.of("build"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_PATH))) {
            writer.write("birthDate,pillars,sinSalCount,sinSalNames,sinSalKeys");
            writer.newLine();
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }

        System.out.println("\n✅ CSV 저장 완료: " + OUTPUT_PATH);
        System.out.println("   총 " + rows.size() + "건 기록");
    }
}
