package com.nbw.tfc.skill;

public enum SkillDef {
    GENSMITH("gensmith", "skill.tfcsmithingskill.gensmith", 250),
    TOOLSMITH("toolsmith", "skill.tfcsmithingskill.toolsmith", 100),
    WEAPONSMITH("weaponsmith", "skill.tfcsmithingskill.weaponsmith", 100),
    ARMORSMITH("armorsmith", "skill.tfcsmithingskill.armorsmith", 100);

    public final String id;
    public final String translationKey;
    public final int rate;

    SkillDef(String id, String translationKey, int rate) {
        this.id = id;
        this.translationKey = translationKey;
        this.rate = rate;
    }
}
