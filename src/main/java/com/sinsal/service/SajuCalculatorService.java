package com.sinsal.service;

import com.sinsal.model.EarthlyBranch;
import com.sinsal.model.HeavenlyStem;
import com.sinsal.model.Pillar;
import com.sinsal.model.ThreePillars;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 사주 계산 서비스 (연주·월주·일주)
 *
 * 참조: saju.ts의 calculateSaju() 포팅
 *
 * [연주] (year - 4) % 60 갑자 순환. 입춘 이전이면 전년도 기준.
 * [월주] 절기(節氣) 기준 월지 + 연간 연동 월간 계산.
 * [일주] 기준일 1900-01-01 = 甲戌(인덱스 0,10)에서 경과 일수로 계산.
 */
@Service
public class SajuCalculatorService {

    /**
     * 월주 계산에 사용되는 12절(節)의 태양 황경
     * index 0 = 소한(285°, 축월 시작), index 1 = 입춘(315°, 인월 시작) ...
     *
     * 월지 매핑:
     *   소한(285°)→축(1), 입춘(315°)→인(2), 경칩(345°)→묘(3),
     *   청명(15°)→진(4),  입하(45°)→사(5),  망종(75°)→오(6),
     *   소서(105°)→미(7), 입추(135°)→신(8), 백로(165°)→유(9),
     *   한로(195°)→술(10),입동(225°)→해(11),대설(255°)→자(0)
     */
    private static final double[] JEOL_LONGITUDES  = {285, 315, 345, 15, 45, 75, 105, 135, 165, 195, 225, 255};
    private static final int[]    JEOL_BRANCH_IDX  = {  1,   2,   3,  4,  5,  6,   7,   8,   9,  10,  11,   0};

    /**
     * 기준일: 1900-01-01 = 甲戌日 (천간 0 = 갑, 지지 10 = 술)
     */
    private static final LocalDate BASE_DATE       = LocalDate.of(1900, 1, 1);
    private static final int       BASE_STEM_IDX   = 0;  // 갑
    private static final int       BASE_BRANCH_IDX = 10; // 술

    private final SolarTermService solarTermService;

    public SajuCalculatorService(SolarTermService solarTermService) {
        this.solarTermService = solarTermService;
    }

    /**
     * 생년월일(양력)로부터 연·월·일주를 계산한다. (시주 미포함)
     */
    public ThreePillars calculate(LocalDate birthDate) {
        return calculate(birthDate, null);
    }

    /**
     * 생년월일(양력) + 출생 시각으로부터 연·월·일·시주를 계산한다.
     *
     * @param birthDate 생년월일
     * @param birthTime 출생 시각 (HH:mm 형식). null이면 시주를 계산하지 않음.
     */
    public ThreePillars calculate(LocalDate birthDate, String birthTime) {
        Pillar yearPillar  = calcYearPillar(birthDate);
        Pillar monthPillar = calcMonthPillar(birthDate, yearPillar.getStem());
        Pillar dayPillar   = calcDayPillar(birthDate);
        Pillar hourPillar  = (birthTime != null) ? calcHourPillar(birthTime, dayPillar.getStem()) : null;
        return new ThreePillars(yearPillar, monthPillar, dayPillar, hourPillar);
    }

    // ── 연주 ──────────────────────────────────────────────────────

    private Pillar calcYearPillar(LocalDate birthDate) {
        int year = birthDate.getYear();

        // 입춘 이전이면 전년도 기준
        LocalDate ipchun = solarTermService.getSolarTermDate(year, 315);
        if (birthDate.isBefore(ipchun)) {
            year -= 1;
        }

        int stemIdx   = positiveMod(year - 4, 10);
        int branchIdx = positiveMod(year - 4, 12);
        return new Pillar(HeavenlyStem.fromIndex(stemIdx), EarthlyBranch.fromIndex(branchIdx));
    }

    // ── 월주 ──────────────────────────────────────────────────────

    private Pillar calcMonthPillar(LocalDate birthDate, HeavenlyStem yearStem) {
        int year = birthDate.getYear();

        // 이번 해와 전년도 절기 날짜를 모두 준비해두고
        // 생일 이전에 해당하는 가장 최근 절기를 찾는다
        int monthBranchIdx = -1;
        LocalDate latestJeolDate = null;

        for (int i = 0; i < JEOL_LONGITUDES.length; i++) {
            double longitude = JEOL_LONGITUDES[i];

            // 해당 절기가 어느 해에 속하는지 — 소한(285°)/대설(255°)은 연초/연말에 걸쳐있음
            // 일단 이번 해와 이전 해 두 가지를 다 체크
            for (int y = year - 1; y <= year; y++) {
                LocalDate jeolDate = solarTermService.getSolarTermDate(y, longitude);
                if (!birthDate.isBefore(jeolDate)) {
                    if (latestJeolDate == null || jeolDate.isAfter(latestJeolDate)) {
                        latestJeolDate = jeolDate;
                        monthBranchIdx = JEOL_BRANCH_IDX[i];
                    }
                }
            }
        }

        if (monthBranchIdx == -1) {
            // 소한 이전 — 전년 대설 기준 자월
            monthBranchIdx = 0;
        }

        // 월간: 연간 기준 공식
        // yearStemIdx % 5 → baseStem(인월 기준): [2,4,6,8,0]
        // monthStemIdx = (baseStem + (branchIdx - 2 + 12) % 12) % 10
        int baseStem = positiveMod(yearStem.getIndex() % 5 * 2 + 2, 10);
        int monthStemIdx = positiveMod(baseStem + positiveMod(monthBranchIdx - 2, 12), 10);

        return new Pillar(HeavenlyStem.fromIndex(monthStemIdx), EarthlyBranch.fromIndex(monthBranchIdx));
    }

    // ── 일주 ──────────────────────────────────────────────────────

    private Pillar calcDayPillar(LocalDate birthDate) {
        long daysSince = ChronoUnit.DAYS.between(BASE_DATE, birthDate);
        int stemIdx   = positiveMod((int)(daysSince + BASE_STEM_IDX),   10);
        int branchIdx = positiveMod((int)(daysSince + BASE_BRANCH_IDX), 12);
        return new Pillar(HeavenlyStem.fromIndex(stemIdx), EarthlyBranch.fromIndex(branchIdx));
    }

    // ── 시주 ──────────────────────────────────────────────────────

    /**
     * 시주 계산 (오자원두법, 五子元頭法)
     *
     * 시지: 자시(子時)는 23:00~01:00 → branchIndex=0
     *       이후 2시간 단위로 하나씩 증가
     * 시간: (일간.index × 2 + 시지.index) % 10
     *
     * @param birthTime HH:mm 형식 문자열
     * @param dayStem   일주 천간
     */
    private Pillar calcHourPillar(String birthTime, HeavenlyStem dayStem) {
        int hour = Integer.parseInt(birthTime.split(":")[0]);

        int branchIdx = (hour >= 23 || hour < 1) ? 0 : (hour + 1) / 2;
        int stemIdx   = positiveMod(dayStem.getIndex() * 2 + branchIdx, 10);

        return new Pillar(HeavenlyStem.fromIndex(stemIdx), EarthlyBranch.fromIndex(branchIdx));
    }

    // ── 유틸 ──────────────────────────────────────────────────────

    /** 항상 양수인 모듈로 연산 */
    private static int positiveMod(int value, int mod) {
        return ((value % mod) + mod) % mod;
    }
}
