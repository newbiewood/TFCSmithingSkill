package com.nbw.tfc.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.nbw.tfc.skill.SkillComponents;
import com.nbw.tfc.skill.config.ServerConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ItemStack.class, priority = 1001)
public abstract class ItemStackMixin {

    @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processDurabilityChange(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;I)I"))
    private int applySkillExtraDurability(ServerLevel level, ItemStack stack, int damage, Operation<Integer> original) {
        int result = original.call(level, stack, damage);
        if (ServerConfig.INSTANCE.applySkillExtra.get()) {
            Float skillExtra = stack.get(SkillComponents.SKILL_EXTRA.get());
            if (skillExtra != null && skillExtra > 0f) {
                result = Math.max(1, (int) (result / (1f + skillExtra)));
            }
        }
        return result;
    }
}
