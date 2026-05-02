package com.sinsal.model;

/**
 * 연주 + 월주 + 일주 + 시주(선택)
 * 시주는 birthTime이 제공된 경우에만 포함된다.
 */
public class ThreePillars {
    private final Pillar yearPillar;
    private final Pillar monthPillar;
    private final Pillar dayPillar;
    private final Pillar hourPillar; // null이면 시주 미포함

    public ThreePillars(Pillar yearPillar, Pillar monthPillar, Pillar dayPillar) {
        this(yearPillar, monthPillar, dayPillar, null);
    }

    public ThreePillars(Pillar yearPillar, Pillar monthPillar, Pillar dayPillar, Pillar hourPillar) {
        this.yearPillar  = yearPillar;
        this.monthPillar = monthPillar;
        this.dayPillar   = dayPillar;
        this.hourPillar  = hourPillar;
    }

    public Pillar getYearPillar()  { return yearPillar; }
    public Pillar getMonthPillar() { return monthPillar; }
    public Pillar getDayPillar()   { return dayPillar; }

    /** 시주. birthTime이 제공되지 않았으면 null. */
    public Pillar getHourPillar()  { return hourPillar; }
    public boolean hasHourPillar() { return hourPillar != null; }
}
