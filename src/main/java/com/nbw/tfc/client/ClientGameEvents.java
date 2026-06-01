package com.nbw.tfc.client;

import com.nbw.tfc.TFCSmithingSkill;
import com.nbw.tfc.skill.SkillComponents;
import net.dries007.tfc.client.screen.AnvilScreen;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.forge.ForgingBonus;
import net.dries007.tfc.common.component.forge.ForgingBonusComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = TFCSmithingSkill.MODID, value = Dist.CLIENT)
public class ClientGameEvents {

    private static String skillAbbr(String typeKey) {
        if (typeKey == null) return "G.S.";
        return switch (typeKey) {
            case "skill.tfcsmithingskill.toolsmith" -> "T.S.";
            case "skill.tfcsmithingskill.weaponsmith" -> "W.S.";
            case "skill.tfcsmithingskill.armorsmith" -> "A.S.";
            default -> "G.S.";
        };
    }

    @SubscribeEvent
    static void onItemTooltip(ItemTooltipEvent event) {
        Float skillExtra = event.getItemStack().get(SkillComponents.SKILL_EXTRA.get());
        if (skillExtra == null || skillExtra <= 0f) return;
        String rank = event.getItemStack().get(SkillComponents.SKILL_RANK.get());
        String typeKey = event.getItemStack().get(SkillComponents.SKILL_TYPE.get());
        String abbr = skillAbbr(typeKey);
        String pct = String.format("+%.1f%%", skillExtra * 100);
        boolean isMaster = "Master".equals(rank);

        ForgingBonusComponent tfcBonus = event.getItemStack().get(TFCComponents.FORGING_BONUS.get());
        if (tfcBonus != null && tfcBonus.type() != ForgingBonus.NONE && tfcBonus.author().isPresent()) {
            String author = tfcBonus.author().get();
            MutableComponent typeName = tfcBonus.type().getDisplayName().copy();
            List<Component> tips = event.getToolTip();
            for (int i = 0; i < tips.size(); i++) {
                if (tips.get(i).getString().contains(author)) {
                    MutableComponent newLine = Component.literal(
                        typeName.getString() + " by [" + abbr + " " + (rank != null ? rank : "?") + "] " + author);
                    if (isMaster) {
                        newLine = newLine.withStyle(ChatFormatting.LIGHT_PURPLE);
                    } else {
                        newLine = newLine.withStyle(tips.get(i).getStyle());
                    }
                    tips.set(i, newLine);
                    break;
                }
            }
        }

        MutableComponent line;
        if (typeKey != null) {
            line = Component.literal("Skill: " + pct);
        } else {
            line = Component.literal("Skill: " + pct);
        }
        line = line.withColor(0xFF_D0A000);
        event.getToolTip().add(line);
    }

    @SubscribeEvent
    static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen invScreen) {
            int guiLeft = invScreen.getGuiLeft();
            int guiTop = invScreen.getGuiTop();
            event.addListener(new SkillTabButton(guiLeft + 176, guiTop + 119));
        }
    }

    @SubscribeEvent
    static void onAnvilScreenRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof AnvilScreen anvilScreen) {
            GuiGraphics graphics = event.getGuiGraphics();
            int guiLeft = anvilScreen.getGuiLeft();
            int guiTop = anvilScreen.getGuiTop();
            var player = Minecraft.getInstance().player;
            if (player == null) return;
            var data = com.nbw.tfc.skill.SkillAttachments.get(player);

            float skillMult = com.nbw.tfc.skill.Skills.skillMult(
                com.nbw.tfc.skill.SkillDef.GENSMITH,
                data.getXp(com.nbw.tfc.skill.SkillDef.GENSMITH));
            float baseMult = com.nbw.tfc.skill.Skills.baseMult(
                com.nbw.tfc.skill.SkillDef.GENSMITH,
                data.getXp(com.nbw.tfc.skill.SkillDef.GENSMITH));
            String rank = com.nbw.tfc.skill.Skills.skillRankName(baseMult);

            String text = "Skill: " + String.format("%.2f", skillMult) + " (" + rank + ")";
            var pose = graphics.pose();
            pose.pushPose();
            pose.translate(guiLeft + 105, guiTop + 6, 0);
            pose.scale(0.7f, 0.7f, 1f);
            graphics.drawString(Minecraft.getInstance().font, text, 0, 0, 0xFF_D0A000, false);
            pose.popPose();
        }
    }
}
