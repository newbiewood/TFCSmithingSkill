package com.nbw.tfc.skill.screen;

import com.nbw.tfc.skill.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SkillScreen extends Screen {

    public SkillScreen() {
        super(Component.translatable("screen.tfcsmithingskill.skills"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderSkillBars(graphics);
    }

    private void renderSkillBars(GuiGraphics graphics) {
        var player = minecraft.player;
        if (player == null) return;
        var data = SkillAttachments.get(player);

        SkillDef[] skills = SkillDef.values();
        for (int i = 0; i < skills.length; i++) {
            SkillDef skill = skills[i];
            int xp = data.getXp(skill);
            float baseMult = Skills.baseMult(skill, xp);
            float mult = Skills.skillMult(skill, xp);
            float extra = Skills.skillExtra(skill, xp);
            String rank = Skills.skillRankName(baseMult);

            int y = 20 + i * 28;
            int x = 20;

            String name = Component.translatable(skill.translationKey).getString();
            graphics.drawString(font, name + "  [" + rank + "]", x, y, 0xFFFFFF, false);

            float tierProgress = rankProgress(baseMult);
            int barX = x + 150;
            int barY = y + 2;
            int barW = 100;
            int barH = 7;
            graphics.fill(barX, barY, barX + barW, barY + barH, 0xFF_555555);
            graphics.fill(barX + 1, barY + 1, barX + 1 + (int)((barW - 2) * tierProgress), barY + barH - 1, 0xFF_D0A000);

            String xpText = xp + " XP  (rate: " + skill.rate + ")";
            graphics.drawString(font, xpText, barX, barY + 9, 0x666666, false);

            String statText = "mult: " + String.format("%.2f", mult) + "  extra: +" + String.format("%.0f", extra * 100) + "%";
            graphics.drawString(font, statText, x + 10, y + 12, 0xAAAAAA, false);
        }
    }

    private static float rankProgress(float baseMult) {
        if (baseMult >= 0.75f) return (baseMult - 0.75f) / 0.25f;
        if (baseMult >= 0.5f)  return (baseMult - 0.50f) / 0.25f;
        if (baseMult >= 0.25f) return (baseMult - 0.25f) / 0.25f;
        return baseMult / 0.25f;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}