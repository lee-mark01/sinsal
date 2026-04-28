package com.sinsal.model;

/**
 * 십천간 (十天干)
 * 인덱스 0(갑) ~ 9(계)
 */
public enum HeavenlyStem {
    GAP("갑", "甲", 0),
    EUL("을", "乙", 1),
    BYEONG("병", "丙", 2),
    JEONG("정", "丁", 3),
    MU("무", "戊", 4),
    GI("기", "己", 5),
    GYEONG("경", "庚", 6),
    SIN("신", "辛", 7),
    IM("임", "壬", 8),
    GYE("계", "癸", 9);

    private final String korean;
    private final String hanja;
    private final int index;

    HeavenlyStem(String korean, String hanja, int index) {
        this.korean = korean;
        this.hanja = hanja;
        this.index = index;
    }

    public String getKorean() { return korean; }
    public String getHanja() { return hanja; }
    public int getIndex() { return index; }

    public static HeavenlyStem fromIndex(int index) {
        int i = ((index % 10) + 10) % 10;
        for (HeavenlyStem s : values()) {
            if (s.index == i) return s;
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}
