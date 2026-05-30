package com.nbw.tfc.skill.config;

import com.nbw.tfc.skill.SkillDef;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class ServerConfig {

    public static final ServerConfig INSTANCE;
    public static final ModConfigSpec SPEC;

    public final ModConfigSpec.DoubleValue skillMultBase;
    public final ModConfigSpec.DoubleValue skillMultRange;

    public final ModConfigSpec.DoubleValue rankBonusNovice;
    public final ModConfigSpec.DoubleValue rankBonusAdept;
    public final ModConfigSpec.DoubleValue rankBonusExpert;
    public final ModConfigSpec.DoubleValue rankBonusMaster;

    public final ModConfigSpec.DoubleValue xpBonusMaxPerTier;

    public final ModConfigSpec.DoubleValue weightGensmith;

    public final ModConfigSpec.IntValue rateGensmith;
    public final ModConfigSpec.IntValue rateToolsmith;
    public final ModConfigSpec.IntValue rateWeaponsmith;
    public final ModConfigSpec.IntValue rateArmorsmith;

    public final ModConfigSpec.BooleanValue modifyRatio;
    public final ModConfigSpec.BooleanValue applySkillExtra;
    public final ModConfigSpec.BooleanValue grantXp;

    public final ModConfigSpec.ConfigValue<List<? extends String>> customToolsmithItems;
    public final ModConfigSpec.ConfigValue<List<? extends String>> customWeaponsmithItems;
    public final ModConfigSpec.ConfigValue<List<? extends String>> customArmorsmithItems;

    public List<? extends String> getCustomItems(SkillDef skill) {
        return switch (skill) {
            case TOOLSMITH -> customToolsmithItems.get();
            case WEAPONSMITH -> customWeaponsmithItems.get();
            case ARMORSMITH -> customArmorsmithItems.get();
            default -> List.of();
        };
    }

    public int getRate(com.nbw.tfc.skill.SkillDef skill) {
        return switch (skill) {
            case GENSMITH -> rateGensmith.getAsInt();
            case TOOLSMITH -> rateToolsmith.getAsInt();
            case WEAPONSMITH -> rateWeaponsmith.getAsInt();
            case ARMORSMITH -> rateArmorsmith.getAsInt();
        };
    }

    static {
        var pair = new ModConfigSpec.Builder().configure(ServerConfig::new);
        SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    private ServerConfig(ModConfigSpec.Builder builder) {
        builder.push("skill_mult");
        skillMultBase = builder.comment("Base value for skillMult (lower bound)").defineInRange("base", 0.5, 0.0, 10.0);
        skillMultRange = builder.comment("Range added by XP (upper = base + range)").defineInRange("range", 1.0, 0.0, 10.0);
        builder.pop();

        builder.push("rank_bonus");
        rankBonusNovice  = builder.comment("Fixed bonus at Novice rank").defineInRange("novice", 0.00, 0.0, 10.0);
        rankBonusAdept   = builder.comment("Fixed bonus at Adept rank").defineInRange("adept", 0.20, 0.0, 10.0);
        rankBonusExpert  = builder.comment("Fixed bonus at Expert rank").defineInRange("expert", 0.40, 0.0, 10.0);
        rankBonusMaster  = builder.comment("Fixed bonus at Master rank").defineInRange("master", 0.70, 0.0, 10.0);
        builder.pop();

        builder.push("xp_bonus");
        xpBonusMaxPerTier = builder.comment("Max XP bonus gained within a tier (0 -> this value)").defineInRange("max_per_tier", 0.10, 0.0, 10.0);
        builder.pop();

        builder.push("weight");
        weightGensmith = builder.comment("Weight of general smithing in weighted average (specialist = 1 - this)").defineInRange("gensmith", 0.7, 0.0, 1.0);
        builder.pop();

        builder.push("xp_rates");
        rateGensmith    = builder.comment("XP rate for General Smithing").defineInRange("gensmith", 250, 1, 100000);
        rateToolsmith   = builder.comment("XP rate for Tool Smithing").defineInRange("toolsmith", 100, 1, 100000);
        rateWeaponsmith = builder.comment("XP rate for Weapon Smithing").defineInRange("weaponsmith", 100, 1, 100000);
        rateArmorsmith  = builder.comment("XP rate for Armor Smithing").defineInRange("armorsmith", 100, 1, 100000);
        builder.pop();

        builder.push("toggles");
        modifyRatio    = builder.comment("Enable skill-modified effective ratio for quality tier").define("modify_ratio", true);
        applySkillExtra= builder.comment("Enable extra attribute multiplier from skill (efficiency/damage/durability)").define("apply_skill_extra", true);
        grantXp        = builder.comment("Grant skill XP when forging completes").define("grant_xp", true);
        builder.pop();

        builder.push("skill_mappings").comment("Add items from other mods to skill categories. Use format: \"modid:item_path\"");
        customToolsmithItems   = builder.comment("Items treated as Tool Smithing").defineListAllowEmpty("toolsmith", List.of(), o -> o instanceof String);
        customWeaponsmithItems = builder.comment("Items treated as Weapon Smithing").defineListAllowEmpty("weaponsmith", List.of(), o -> o instanceof String);
        customArmorsmithItems  = builder.comment("Items treated as Armor Smithing").defineListAllowEmpty("armorsmith", List.of(), o -> o instanceof String);
        builder.pop();
    }
}
