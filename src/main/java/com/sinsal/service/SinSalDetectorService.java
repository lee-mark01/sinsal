package com.sinsal.service;

import com.sinsal.model.EarthlyBranch;
import com.sinsal.model.HeavenlyStem;
import com.sinsal.model.SinSalInfo;
import com.sinsal.model.ThreePillars;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 신살 탐지 서비스 — sin_sal.ts의 findSinSals() 포팅
 *
 * 구현된 신살 7종:
 *   천을귀인, 도화살, 역마살, 공망, 화개살, 원진살, 귀문관살
 */
@Service
public class SinSalDetectorService {

    private final SinSalDataService dataService;

    /**
     * 천을귀인 판단표: 일간 → 해당 지지 목록
     */
    private static final Map<HeavenlyStem, Set<EarthlyBranch>> CHEON_EUL_TABLE = Map.of(
        HeavenlyStem.GAP,    Set.of(EarthlyBranch.CHUK, EarthlyBranch.MI),
        HeavenlyStem.EUL,    Set.of(EarthlyBranch.JA,   EarthlyBranch.SIN),
        HeavenlyStem.BYEONG, Set.of(EarthlyBranch.HAE,  EarthlyBranch.YU),
        HeavenlyStem.JEONG,  Set.of(EarthlyBranch.HAE,  EarthlyBranch.YU),
        HeavenlyStem.MU,     Set.of(EarthlyBranch.CHUK, EarthlyBranch.MI),
        HeavenlyStem.GI,     Set.of(EarthlyBranch.JA,   EarthlyBranch.SIN),
        HeavenlyStem.GYEONG, Set.of(EarthlyBranch.CHUK, EarthlyBranch.MI),
        HeavenlyStem.SIN,    Set.of(EarthlyBranch.IN,   EarthlyBranch.O),
        HeavenlyStem.IM,     Set.of(EarthlyBranch.SA,   EarthlyBranch.MYO),
        HeavenlyStem.GYE,    Set.of(EarthlyBranch.SA,   EarthlyBranch.MYO)
    );

    /**
     * 공망 판단표: 일지 → 공망 지지 쌍
     */
    private static final Map<EarthlyBranch, Set<EarthlyBranch>> GONG_MANG_TABLE = Map.ofEntries(
        Map.entry(EarthlyBranch.JA,   Set.of(EarthlyBranch.SUL,  EarthlyBranch.HAE)),
        Map.entry(EarthlyBranch.CHUK, Set.of(EarthlyBranch.SUL,  EarthlyBranch.HAE)),
        Map.entry(EarthlyBranch.IN,   Set.of(EarthlyBranch.JA,   EarthlyBranch.CHUK)),
        Map.entry(EarthlyBranch.MYO,  Set.of(EarthlyBranch.JA,   EarthlyBranch.CHUK)),
        Map.entry(EarthlyBranch.JIN,  Set.of(EarthlyBranch.IN,   EarthlyBranch.MYO)),
        Map.entry(EarthlyBranch.SA,   Set.of(EarthlyBranch.IN,   EarthlyBranch.MYO)),
        Map.entry(EarthlyBranch.O,    Set.of(EarthlyBranch.JIN,  EarthlyBranch.SA)),
        Map.entry(EarthlyBranch.MI,   Set.of(EarthlyBranch.JIN,  EarthlyBranch.SA)),
        Map.entry(EarthlyBranch.SIN,  Set.of(EarthlyBranch.O,    EarthlyBranch.MI)),
        Map.entry(EarthlyBranch.YU,   Set.of(EarthlyBranch.O,    EarthlyBranch.MI)),
        Map.entry(EarthlyBranch.SUL,  Set.of(EarthlyBranch.SIN,  EarthlyBranch.YU)),
        Map.entry(EarthlyBranch.HAE,  Set.of(EarthlyBranch.SIN,  EarthlyBranch.YU))
    );

    public SinSalDetectorService(SinSalDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * 연/월/일주로부터 신살 목록 반환
     */
    public List<SinSalInfo> detect(ThreePillars pillars) {
        HeavenlyStem dayStem   = pillars.getDayPillar().getStem();
        EarthlyBranch dayBranch = pillars.getDayPillar().getBranch();

        Set<EarthlyBranch> branches = EnumSet.of(
            pillars.getYearPillar().getBranch(),
            pillars.getMonthPillar().getBranch(),
            pillars.getDayPillar().getBranch()
        );

        List<SinSalInfo> result = new ArrayList<>();

        if (hasCheonEulGwiIn(dayStem, branches))   result.add(dataService.get("cheon_eul_gwi_in"));
        if (hasDoHwaSal(branches))                  result.add(dataService.get("do_hwa_sal"));
        if (hasYeokMaSal(branches))                 result.add(dataService.get("yeok_ma_sal"));
        if (hasGongMang(dayBranch, branches))       result.add(dataService.get("gong_mang"));
        if (hasHwaGaeSal(branches))                 result.add(dataService.get("hwa_gae_sal"));
        if (hasWonJinSal(branches))                 result.add(dataService.get("won_jin_sal"));
        if (hasGwiMunGwanSal(branches))             result.add(dataService.get("gwi_mun_gwan_sal"));

        return result;
    }

    // ── 개별 신살 판단 ──────────────────────────────────────────

    /** 천을귀인: 일간 기준 해당 지지가 사주에 있으면 */
    private boolean hasCheonEulGwiIn(HeavenlyStem dayStem, Set<EarthlyBranch> branches) {
        Set<EarthlyBranch> targets = CHEON_EUL_TABLE.get(dayStem);
        return targets != null && branches.stream().anyMatch(targets::contains);
    }

    /**
     * 도화살
     *   인오술 삼합에 묘가 있거나
     *   사유축 삼합에 오가 있거나
     *   신자진 삼합에 유가 있거나
     *   해묘미 삼합에 자가 있으면
     */
    private boolean hasDoHwaSal(Set<EarthlyBranch> b) {
        return (anyOf(b, EarthlyBranch.IN, EarthlyBranch.O, EarthlyBranch.SUL)  && b.contains(EarthlyBranch.MYO)) ||
               (anyOf(b, EarthlyBranch.SA, EarthlyBranch.YU, EarthlyBranch.CHUK) && b.contains(EarthlyBranch.O))   ||
               (anyOf(b, EarthlyBranch.SIN, EarthlyBranch.JA, EarthlyBranch.JIN) && b.contains(EarthlyBranch.YU))  ||
               (anyOf(b, EarthlyBranch.HAE, EarthlyBranch.MYO, EarthlyBranch.MI) && b.contains(EarthlyBranch.JA));
    }

    /**
     * 역마살
     *   인오술 삼합에 신이 있거나
     *   사유축 삼합에 해가 있거나
     *   신자진 삼합에 인이 있거나
     *   해묘미 삼합에 사가 있으면
     */
    private boolean hasYeokMaSal(Set<EarthlyBranch> b) {
        return (anyOf(b, EarthlyBranch.IN, EarthlyBranch.O, EarthlyBranch.SUL)   && b.contains(EarthlyBranch.SIN)) ||
               (anyOf(b, EarthlyBranch.SA, EarthlyBranch.YU, EarthlyBranch.CHUK) && b.contains(EarthlyBranch.HAE)) ||
               (anyOf(b, EarthlyBranch.SIN, EarthlyBranch.JA, EarthlyBranch.JIN) && b.contains(EarthlyBranch.IN))  ||
               (anyOf(b, EarthlyBranch.HAE, EarthlyBranch.MYO, EarthlyBranch.MI) && b.contains(EarthlyBranch.SA));
    }

    /** 공망: 일지 기준 공망 지지가 사주에 있으면 */
    private boolean hasGongMang(EarthlyBranch dayBranch, Set<EarthlyBranch> branches) {
        Set<EarthlyBranch> gongMang = GONG_MANG_TABLE.get(dayBranch);
        return gongMang != null && branches.stream().anyMatch(gongMang::contains);
    }

    /**
     * 화개살
     *   인오술 삼합에 술이 있거나
     *   사유축 삼합에 축이 있거나
     *   신자진 삼합에 진이 있거나
     *   해묘미 삼합에 미가 있으면
     */
    private boolean hasHwaGaeSal(Set<EarthlyBranch> b) {
        return (anyOf(b, EarthlyBranch.IN, EarthlyBranch.O, EarthlyBranch.SUL)   && b.contains(EarthlyBranch.SUL)) ||
               (anyOf(b, EarthlyBranch.SA, EarthlyBranch.YU, EarthlyBranch.CHUK) && b.contains(EarthlyBranch.CHUK)) ||
               (anyOf(b, EarthlyBranch.SIN, EarthlyBranch.JA, EarthlyBranch.JIN) && b.contains(EarthlyBranch.JIN)) ||
               (anyOf(b, EarthlyBranch.HAE, EarthlyBranch.MYO, EarthlyBranch.MI) && b.contains(EarthlyBranch.MI));
    }

    /**
     * 원진살: 육충 관계 쌍이 사주에 모두 있으면
     *   자-오, 축-미, 인-신, 묘-유, 진-술, 사-해
     */
    private boolean hasWonJinSal(Set<EarthlyBranch> b) {
        EarthlyBranch[][] pairs = {
            {EarthlyBranch.JA, EarthlyBranch.O},
            {EarthlyBranch.CHUK, EarthlyBranch.MI},
            {EarthlyBranch.IN, EarthlyBranch.SIN},
            {EarthlyBranch.MYO, EarthlyBranch.YU},
            {EarthlyBranch.JIN, EarthlyBranch.SUL},
            {EarthlyBranch.SA, EarthlyBranch.HAE}
        };
        for (EarthlyBranch[] pair : pairs) {
            if (b.contains(pair[0]) && b.contains(pair[1])) return true;
        }
        return false;
    }

    /**
     * 귀문관살: 인·신·사·해 중 2개 이상 있으면
     */
    private boolean hasGwiMunGwanSal(Set<EarthlyBranch> b) {
        long count = List.of(EarthlyBranch.IN, EarthlyBranch.SIN, EarthlyBranch.SA, EarthlyBranch.HAE)
                        .stream().filter(b::contains).count();
        return count >= 2;
    }

    private boolean anyOf(Set<EarthlyBranch> branches, EarthlyBranch... candidates) {
        for (EarthlyBranch c : candidates) {
            if (branches.contains(c)) return true;
        }
        return false;
    }
}
