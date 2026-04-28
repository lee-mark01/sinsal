package com.sinsal.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * 절기(節氣) 날짜 계산 서비스
 *
 * Jean Meeus "Astronomical Algorithms" 기반으로 태양 황경을 계산해
 * 특정 절기가 해당 연도 몇 월 며칠에 시작하는지 반환한다.
 *
 * 월주 계산에 사용되는 12절(節)의 태양 황경:
 *   입춘=315°, 경칩=345°, 청명=15°, 입하=45°, 망종=75°, 소서=105°,
 *   입추=135°, 백로=165°, 한로=195°, 입동=225°, 대설=255°, 소한=285°
 */
@Service
public class SolarTermService {

    /**
     * 지정된 연도·태양황경에 해당하는 절기 날짜를 반환한다.
     *
     * @param year              연도
     * @param targetLongitude   태양 황경 (0~360도)
     * @return 해당 절기가 시작되는 날짜 (KST 기준)
     */
    public LocalDate getSolarTermDate(int year, double targetLongitude) {
        // 초기 추정일: 태양 황경 ≈ 월 × 30°에서 시작
        // 입춘(315°) → 2월 초, 소한(285°) → 1월 초 등
        double approxDayOfYear = ((targetLongitude - 315 + 360) % 360) / 360.0 * 365.25 + 35;
        LocalDate approxDate = LocalDate.of(year, 1, 1).plusDays((long) approxDayOfYear - 1);

        // 이진탐색으로 정확한 날짜 계산 (±15일 범위)
        LocalDate low  = approxDate.minusDays(15);
        LocalDate high = approxDate.plusDays(15);

        while (!low.isEqual(high)) {
            LocalDate mid = low.plusDays(low.until(high, java.time.temporal.ChronoUnit.DAYS) / 2);
            double longitude = getSolarLongitude(mid);

            // 황경이 목표에 도달했는지 방향 체크
            if (isBeforeTarget(longitude, targetLongitude)) {
                low = mid.plusDays(1);
            } else {
                high = mid;
            }
        }
        return low;
    }

    /**
     * 주어진 날짜의 태양 황경을 계산한다 (KST 정오 기준).
     * Meeus 간략식 적용 — 정확도 ±0.01도 (날짜 오차 ±1일 이내)
     */
    public double getSolarLongitude(LocalDate date) {
        // KST 정오 = UTC 03:00
        long epochSecond = date.atTime(3, 0).toEpochSecond(ZoneOffset.UTC);
        double jde = epochSecond / 86400.0 + 2440587.5; // Julian Day

        double T = (jde - 2451545.0) / 36525.0; // J2000.0 기준 율리우스 세기

        // 태양 평균 황경
        double L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T * T;
        // 태양 평균 근점이각
        double M  = 357.52911 + 35999.05029 * T - 0.0001537 * T * T;
        double Mrad = Math.toRadians(M % 360);

        // 중심차
        double C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * Math.sin(Mrad)
                 + (0.019993 - 0.000101 * T) * Math.sin(2 * Mrad)
                 + 0.000289 * Math.sin(3 * Mrad);

        // 달의 승교점 황경 (섭동 보정용)
        double omega = 125.04 - 1934.136 * T;

        // 태양 겉보기 황경
        double lambda = (L0 + C - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega))) % 360;
        if (lambda < 0) lambda += 360;

        return lambda;
    }

    /**
     * 현재 황경이 목표 황경보다 "이전"인지 판단.
     * 황경은 원형(0~360°)이므로 0/360° 경계 처리 필요.
     */
    private boolean isBeforeTarget(double current, double target) {
        // 목표 황경까지의 반시계 방향 거리
        double diff = (target - current + 360) % 360;
        return diff > 0 && diff <= 180;
    }
}
