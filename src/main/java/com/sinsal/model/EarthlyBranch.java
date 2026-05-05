package com.sinsal.model;

/**
 * 십이지지 (十二地支)
 * 인덱스 0(자) ~ 11(해)
 */
public enum EarthlyBranch {
    JA("자", "子", 0),
    CHUK("축", "丑", 1),
    IN("인", "寅", 2),
    MYO("묘", "卯", 3),
    JIN("진", "辰", 4),
    SA("사", "巳", 5),
    O("오", "午", 6),
    MI("미", "未", 7),
    SIN("신", "申", 8),
    YU("유", "酉", 9),
    SUL("술", "戌", 10),
    HAE("해", "亥", 11);

    private final String korean;
    private final String hanja;
    private final int index;

    EarthlyBranch(String korean, String hanja, int index) {
        this.korean = korean;
        this.hanja = hanja;
        this.index = index;
    }

    public String getKorean() { return korean; }
    public String getHanja() { return hanja; }
    public int getIndex() { return index; }

    public static EarthlyBranch fromIndex(int index) {
        int i = ((index % 12) + 12) % 12;
        for (EarthlyBranch b : values()) {
            if (b.index == i) return b;
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}
