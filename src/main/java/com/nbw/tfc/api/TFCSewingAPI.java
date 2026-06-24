package com.nbw.tfc.api;

import com.nbw.tfc.TFCSmithingSkill;
import com.nbw.tfc.skill.*;
import com.nbw.tfc.skill.config.ServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public final class TFCSewingAPI {

    private TFCSewingAPI() {}

    public static SkillDef determineSkillType(ItemStack stack) {
        return ItemSkillRules.determine(stack);
    }

    public static float calculateSkillMult(ServerPlayer player, SkillDef specialist) {
        SkillData data = SkillAttachments.get(player);
        if (specialist != null) {
            return Skills.weightedSkillMult(data, specialist);
        }
        return Skills.skillMult(SkillDef.GENSMITH, data.getXp(SkillDef.GENSMITH));
    }

    public static float calculateSkillMult(ServerPlayer player, ItemStack item) {
        SkillDef specialist = ItemSkillRules.determine(item);
        return calculateSkillMult(player, specialist);
    }

    public static float calculateSkillExtra(ServerPlayer player, SkillDef specialist) {
        SkillData data = SkillAttachments.get(player);
        if (specialist != null) {
            return Skills.weightedSkillExtra(data, specialist);
        }
        return Skills.skillExtra(SkillDef.GENSMITH, data.getXp(SkillDef.GENSMITH));
    }

    public static float calculateSkillExtra(ServerPlayer player, ItemStack item) {
        SkillDef specialist = ItemSkillRules.determine(item);
        return calculateSkillExtra(player, specialist);
    }

    public static String calculateSkillRank(ServerPlayer player, SkillDef specialist) {
        SkillData data = SkillAttachments.get(player);
        SkillDef effectiveSkill = specialist != null ? specialist : SkillDef.GENSMITH;
        float baseMult = Skills.baseMult(effectiveSkill, data.getXp(effectiveSkill));
        return Skills.skillRankName(baseMult);
    }

    public static void applySkillBonuses(ItemStack stack, ServerPlayer player, SkillDef specialist) {
        if (!ServerConfig.INSTANCE.applySkillExtra.get()) return;

        SkillData data = SkillAttachments.get(player);
        float extra = specialist != null
            ? Skills.weightedSkillExtra(data, specialist)
            : Skills.skillExtra(SkillDef.GENSMITH, data.getXp(SkillDef.GENSMITH));

        SkillDef effectiveSkill = specialist != null ? specialist : SkillDef.GENSMITH;
        float rankBase = Skills.baseMult(effectiveSkill, data.getXp(effectiveSkill));
        String rank = Skills.skillRankName(rankBase);

        applySkillValues(stack, extra, rank, specialist);
    }

    public static void applySkillValues(ItemStack stack, float skillExtra, String rank, SkillDef skillType) {
        if (skillExtra <= 0f) return;

        stack.set(SkillComponents.SKILL_EXTRA.get(), skillExtra);
        stack.set(SkillComponents.SKILL_RANK.get(), rank != null ? rank : "Novice");

        if (skillType != null) {
            stack.set(SkillComponents.SKILL_TYPE.get(), skillType.translationKey);
        } else {
            stack.remove(SkillComponents.SKILL_TYPE.get());
        }

        if (stack.isDamageableItem()) {
            int maxDmg = stack.getMaxDamage();
            if (maxDmg > 0) {
                stack.set(DataComponents.MAX_DAMAGE, (int)(maxDmg * (1f + skillExtra)));
            }
        }
    }

    public static void grantXp(ServerPlayer player, SkillDef specialist) {
        if (!ServerConfig.INSTANCE.grantXp.get()) return;
        SkillData data = SkillAttachments.get(player);
        int amount = 1 + player.getRandom().nextInt(3); // 1~3
        data.addXp(SkillDef.GENSMITH, amount);
        if (specialist != null) {
            data.addXp(specialist, amount);
        }
    }

    public static void registerItemRule(ResourceLocation id, Predicate<ItemStack> predicate, SkillDef skill) {
        ItemSkillRules.registerItemRule(id, predicate, skill);
    }

    public static boolean unregisterItemRule(ResourceLocation id) {
        return ItemSkillRules.unregisterItemRule(id);
    }

    public static SkillData getSkillData(ServerPlayer player) {
        return SkillAttachments.get(player);
    }

    public static boolean hasSkillData(ItemStack stack) {
        Float extra = stack.get(SkillComponents.SKILL_EXTRA.get());
        return extra != null && extra > 0f;
    }

    public static String getModId() {
        return TFCSmithingSkill.MODID;
    }
}