package com.nbw.tfc.skill.screen;

import com.nbw.tfc.skill.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SkillScreen extends Screen {

    private static final int PANEL_WIDTH = 268;
    private static final int PANEL_HEIGHT = 140;
    private static final float TITLE_SCALE = 0.85f;
    private static final float TEXT_SCALE = 0.72f;
    private static final int TEXT = 0x404040;
    private static final int SUBTEXT = 0x606060;
    private static final int BAR_FILL = 0xD0A000;
    private static final int MASTER = 0xAA55FF;

    public SkillScreen() {
        super(Component.translatable("screen.tfcsmithingskill.skills"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderSkillPanel(graphics);
    }

    private void renderSkillPanel(GuiGraphics graphics) {
        var player = minecraft.player;
        if (player == null) return;
        var data = SkillAttachments.get(player);

        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        drawPanel(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT);

        drawCenteredText(graphics, title.getString(), left + PANEL_WIDTH / 2, top + 8, TEXT, TITLE_SCALE);

        SkillDef[] skills = SkillDef.values();
        for (int i = 0; i < skills.length; i++) {
            SkillDef skill = skills[i];
            int xp = data.getXp(skill);
            float baseMult = Skills.baseMult(skill, xp);
            float mult = Skills.skillMult(skill, xp);
            float extra = Skills.skillExtra(skill, xp);
            String rank = Skills.skillRankName(baseMult);

            int rowX = left + 12;
            int rowY = top + 29 + i * 26;
            int barX = left + 136;
            int barY = rowY;
            int barW = 108;
            int barH = 7;

            String name = Component.translatable(skill.translationKey).getString();
            drawText(graphics, name, rowX, rowY, TEXT, TEXT_SCALE);
            drawText(graphics, "[" + rank + "]", rowX, rowY + 10, rank.equals("Master") ? MASTER : SUBTEXT, TEXT_SCALE);

            float tierProgress = rankProgress(baseMult);
            drawProgressBar(graphics, barX, barY, barW, barH, tierProgress);

            String xpText = xp + " XP / " + skill.rate;
            drawText(graphics, xpText, barX, barY + 10, SUBTEXT, TEXT_SCALE);

            String statText = "mult: " + String.format("%.2f", mult) + "  extra: +" + String.format("%.0f", extra * 100) + "%";
            drawText(graphics, statText, barX, barY + 20, SUBTEXT, TEXT_SCALE);
        }
    }

    private void drawText(GuiGraphics graphics, String text, int x, int y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1f);
        graphics.drawString(font, text, (int) (x / scale), (int) (y / scale), color, false);
        graphics.pose().popPose();
    }

    private void drawCenteredText(GuiGraphics graphics, String text, int centerX, int y, int color, float scale) {
        drawText(graphics, text, centerX - (int) (font.width(text) * scale / 2f), y, color, scale);
    }

    private static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xFF_373737);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF_B9B9B9);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF_D4D0C0);
        graphics.fill(x + 4, y + 18, x + width - 4, y + height - 4, 0xFF_C6C1B1);
        graphics.fill(x + 5, y + 19, x + width - 5, y + height - 5, 0xFF_D8D3C3);
    }

    private static void drawProgressBar(GuiGraphics graphics, int x, int y, int width, int height, float progress) {
        graphics.fill(x, y, x + width, y + height, 0xFF_5B5B5B);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF_9A9A9A);
        int fillWidth = (int) ((width - 2) * progress);
        if (fillWidth > 0) {
            graphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + height - 1, 0xFF_000000 | BAR_FILL);
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
