package com.nbw.tfc.client;

import com.nbw.tfc.skill.screen.SkillScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SkillTabButton extends AbstractButton {

    private static final ItemStack ICON = new ItemStack(Items.ANVIL);

    public SkillTabButton(int x, int y) {
        super(x, y, 24, 22, Component.translatable("screen.tfcsmithingskill.skills"));
    }

    @Override
    public void onPress() {
        Minecraft.getInstance().setScreen(new SkillScreen());
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, 0xFF_606060);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, 0xFF_8B8B8B);
        graphics.renderItem(ICON, getX() + 4, getY() + 3);
        if (isHoveredOrFocused())
            graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40_FFFFFF);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}
}
