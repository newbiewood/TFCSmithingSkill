package com.nbw.tfc.event;

import com.nbw.tfc.TFCSmithingSkill;
import com.nbw.tfc.skill.SkillComponents;
import com.nbw.tfc.skill.config.ServerConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class SkillBonusEvents {

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (!ServerConfig.INSTANCE.applySkillExtra.get()) return;
        ItemStack stack = event.getEntity().getMainHandItem();
        if ("skill.tfcsmithingskill.armorsmith".equals(stack.get(SkillComponents.SKILL_TYPE.get()))) return;
        Float skillExtra = stack.get(SkillComponents.SKILL_EXTRA.get());
        if (skillExtra != null && skillExtra > 0f) {
            event.setNewSpeed(event.getNewSpeed() * (1f + skillExtra));
        }
    }

    @SubscribeEvent
    public static void onItemAttribute(ItemAttributeModifierEvent event) {
        if (!ServerConfig.INSTANCE.applySkillExtra.get()) return;
        ItemStack stack = event.getItemStack();
        String skillType = stack.get(SkillComponents.SKILL_TYPE.get());
        if ("skill.tfcsmithingskill.armorsmith".equals(skillType)) return;
        Float skillExtra = stack.get(SkillComponents.SKILL_EXTRA.get());
        if (skillExtra != null && skillExtra > 0f) {
            event.addModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(TFCSmithingSkill.MODID, "skill_extra_damage"),
                    skillExtra,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ),
                EquipmentSlotGroup.MAINHAND
            );
            event.addModifier(Attributes.ATTACK_SPEED,
                new AttributeModifier(
                    ResourceLocation.fromNamespaceAndPath(TFCSmithingSkill.MODID, "skill_extra_speed"),
                    skillExtra * 0.5f,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                ),
                EquipmentSlotGroup.MAINHAND
            );
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        if (!ServerConfig.INSTANCE.applySkillExtra.get()) return;
        Float bestExtra = null;
        String bestRank = null;
        String bestType = null;
        for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
            ItemStack input = event.getInventory().getItem(i);
            Float f = input.get(SkillComponents.SKILL_EXTRA.get());
            if (f != null && (bestExtra == null || f > bestExtra)) {
                bestExtra = f;
                bestRank = input.get(SkillComponents.SKILL_RANK.get());
                bestType = input.get(SkillComponents.SKILL_TYPE.get());
            }
        }
        if (bestExtra != null && bestExtra > 0f) {
            event.getCrafting().set(SkillComponents.SKILL_EXTRA.get(), bestExtra);
            if (event.getCrafting().isDamageableItem()) {
                int maxDmg = event.getCrafting().getMaxDamage();
                if (maxDmg > 0) event.getCrafting().set(DataComponents.MAX_DAMAGE, (int)(maxDmg * (1f + bestExtra)));
            }
            if (bestRank != null) event.getCrafting().set(SkillComponents.SKILL_RANK.get(), bestRank);
            if (bestType != null) event.getCrafting().set(SkillComponents.SKILL_TYPE.get(), bestType);
        }
    }
}
