package com.nbw.tfc.api;

import com.nbw.tfc.skill.SkillDef;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ItemSkillRules {

    private static final List<Rule> RULES = new ArrayList<>();
    private static boolean builtinAdded = false;

    private ItemSkillRules() {}

    public static void addBuiltinRules() {
        if (builtinAdded) return;
        builtinAdded = true;

        registerItemRule(ResourceLocation.fromNamespaceAndPath("tfcs", "weapon_builtin"), stack -> {
            String key = stack.getDescriptionId().toLowerCase();
            return key.contains("sword_blade") || key.contains("mace_head")
                || key.contains("javelin_head") || key.contains("knife_blade");
        }, SkillDef.WEAPONSMITH);

        registerItemRule(ResourceLocation.fromNamespaceAndPath("tfcs", "armor_builtin"), stack -> {
            String key = stack.getDescriptionId().toLowerCase();
            return key.contains("unfinished_helmet") || key.contains("unfinished_chestplate")
                || key.contains("unfinished_greaves") || key.contains("unfinished_boots")
                || key.contains("_shield");
        }, SkillDef.ARMORSMITH);

        registerItemRule(ResourceLocation.fromNamespaceAndPath("tfcs", "tool_builtin"), stack -> {
            String key = stack.getDescriptionId().toLowerCase();
            return key.contains("_head") || key.contains("_blade");
        }, SkillDef.TOOLSMITH);
    }

    public static void registerItemRule(ResourceLocation id, Predicate<ItemStack> predicate, SkillDef skill) {
        RULES.removeIf(r -> r.id.equals(id));
        RULES.add(new Rule(id, predicate, skill));
    }

    public static boolean unregisterItemRule(ResourceLocation id) {
        return RULES.removeIf(r -> r.id.equals(id));
    }

    public static void clearAllRules() {
        RULES.clear();
        builtinAdded = false;
    }

    public static SkillDef determine(ItemStack stack) {
        for (Rule rule : RULES) {
            try {
                if (rule.predicate.test(stack)) {
                    return rule.skill;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public static SkillDef determine(AnvilBlockEntity anvil) {
        Forging forging = anvil.getMainInputForging();
        if (forging == null) return null;
        AnvilRecipe recipe = forging.getRecipe();
        if (recipe == null || !recipe.shouldApplyForgingBonus()) return null;

        try {
            ItemStack sample = recipe.assemble(anvil.getInventory(), anvil.getLevel().registryAccess());
            if (sample.isEmpty()) return null;
            return determine(sample);
        } catch (Exception ignored) {
            return null;
        }
    }

    private record Rule(ResourceLocation id, Predicate<ItemStack> predicate, SkillDef skill) {}
}