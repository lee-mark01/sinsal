package com.sinsal.model;

/**
 * 사주의 한 기둥 (천간 + 지지)
 */
public class Pillar {
    private final HeavenlyStem stem;
    private final EarthlyBranch branch;

    public Pillar(HeavenlyStem stem, EarthlyBranch branch) {
        this.stem = stem;
        this.branch = branch;
    }

    public HeavenlyStem getStem() { return stem; }
    public EarthlyBranch getBranch() { return branch; }

    @Override
    public String toString() {
        return stem.getKorean() + branch.getKorean();
    }
}
