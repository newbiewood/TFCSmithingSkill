package com.nbw.tfc.skill;

import com.nbw.tfc.skill.config.ServerConfig;

public final class Skills {

    public static float baseMult(SkillDef skill, int xp) {
        float rate = ServerConfig.INSTANCE.getRate(skill);
        return 1f - rate / (rate + xp);
    }

    public static float skillMult(SkillDef skill, int xp) {
        float base = (float)(double)ServerConfig.INSTANCE.skillMultBase.get();
        float range = ServerConfig.INSTANCE.skillMultRange.get().floatValue();
        return base + baseMult(skill, xp) * range;
    }

    public static float rankBonus(float baseMult) {
        if (baseMult >= 0.75f) return ServerConfig.INSTANCE.rankBonusMaster.get().floatValue();
        if (baseMult >= 0.5f)  return ServerConfig.INSTANCE.rankBonusExpert.get().floatValue();
        if (baseMult >= 0.25f) return ServerConfig.INSTANCE.rankBonusAdept.get().floatValue();
        return ServerConfig.INSTANCE.rankBonusNovice.get().floatValue();
    }

    public static float xpBonus(SkillDef skill, int xp) {
        float base = baseMult(skill, xp);
        float rankBase = rankBaseMult(base);
        float tierProgress = (base - rankBase) / 0.25f;
        return tierProgress * ServerConfig.INSTANCE.xpBonusMaxPerTier.get().floatValue();
    }

    public static float skillExtra(SkillDef skill, int xp) {
        return rankBonus(baseMult(skill, xp)) + xpBonus(skill, xp);
    }

    public static String skillRankName(float baseMult) {
        if (baseMult >= 0.75f) return "Master";
        if (baseMult >= 0.5f)  return "Expert";
        if (baseMult >= 0.25f) return "Adept";
        return "Novice";
    }

    public static float rankBaseMult(float baseMult) {
        if (baseMult >= 0.75f) return 0.75f;
        if (baseMult >= 0.5f)  return 0.50f;
        if (baseMult >= 0.25f) return 0.25f;
        return 0f;
    }

    public static float weightedSkillMult(SkillData data, SkillDef specialist) {
        float wGen = ServerConfig.INSTANCE.weightGensmith.get().floatValue();
        float wSpec = 1f - wGen;
        return wGen * skillMult(SkillDef.GENSMITH, data.getXp(SkillDef.GENSMITH))
             + wSpec * skillMult(specialist, data.getXp(specialist));
    }

    public static float weightedSkillExtra(SkillData data, SkillDef specialist) {
        float wGen = ServerConfig.INSTANCE.weightGensmith.get().floatValue();
        float wSpec = 1f - wGen;
        return wGen * skillExtra(SkillDef.GENSMITH, data.getXp(SkillDef.GENSMITH))
             + wSpec * skillExtra(specialist, data.getXp(specialist));
    }
}
