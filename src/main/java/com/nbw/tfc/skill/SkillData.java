package com.nbw.tfc.skill;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.EnumMap;

public class SkillData implements INBTSerializable<CompoundTag> {

    private final EnumMap<SkillDef, Integer> xpMap = new EnumMap<>(SkillDef.class);

    public SkillData() {
        for (SkillDef s : SkillDef.values()) xpMap.put(s, 0);
    }

    public int getXp(SkillDef skill) {
        return xpMap.getOrDefault(skill, 0);
    }

    public void addXp(SkillDef skill, int amount) {
        xpMap.merge(skill, amount, Integer::sum);
    }

    public void setXp(SkillDef skill, int amount) {
        xpMap.put(skill, Math.max(0, amount));
    }

    @Override
    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        for (var e : xpMap.entrySet())
            tag.putInt(e.getKey().id, e.getValue());
        return tag;
    }

    @Override
    public void deserializeNBT(net.minecraft.core.HolderLookup.Provider provider, CompoundTag tag) {
        for (SkillDef s : SkillDef.values())
            xpMap.put(s, tag.getInt(s.id));
    }
}
