package com.sinsal.model;

/**
 * 연주 + 월주 + 일주
 */
public class ThreePillars {
    private final Pillar yearPillar;
    private final Pillar monthPillar;
    private final Pillar dayPillar;

    public ThreePillars(Pillar yearPillar, Pillar monthPillar, Pillar dayPillar) {
        this.yearPillar  = yearPillar;
        this.monthPillar = monthPillar;
        this.dayPillar   = dayPillar;
    }

    public Pillar getYearPillar()  { return yearPillar; }
    public Pillar getMonthPillar() { return monthPillar; }
    public Pillar getDayPillar()   { return dayPillar; }
}
