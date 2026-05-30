package com.nbw.tfc.mixin;

import com.nbw.tfc.api.ItemSkillRules;
import com.nbw.tfc.api.TFCSewingAPI;
import com.nbw.tfc.skill.*;
import com.nbw.tfc.skill.config.ServerConfig;
import com.nbw.tfc.skill.network.SyncSkillPacket;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.component.forge.ForgingBonus;
import net.dries007.tfc.common.component.forge.ForgingBonusComponent;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(AnvilBlockEntity.class)
public abstract class AnvilBlockEntityMixin {

    private static final Field INVENTORY_FIELD;
    @Unique
    private SkillDef tfcs$lastSpecialist;
    @Unique
    private Float tfcs$weldExtra;
    @Unique
    private String tfcs$weldRank;
    @Unique
    private String tfcs$weldType;

    static {
        try {
            INVENTORY_FIELD = AnvilBlockEntity.class.getSuperclass().getDeclaredField("inventory");
            INVENTORY_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find inventory field", e);
        }
    }

    @ModifyVariable(method = "work", at = @At("STORE"), ordinal = 0)
    private float modifyRatio(float ratio, ServerPlayer player, ForgeStep step) {
        AnvilBlockEntity anvil = (AnvilBlockEntity) (Object) this;
        tfcs$lastSpecialist = ItemSkillRules.determine(anvil);
        if (!ServerConfig.INSTANCE.modifyRatio.get()) return ratio;
        float skillMult = TFCSewingAPI.calculateSkillMult(player, tfcs$lastSpecialist);
        return ratio / skillMult;
    }

    @Inject(method = "work", at = @At("RETURN"))
    private void onWorkReturn(ServerPlayer player, ForgeStep step, CallbackInfo ci) {
        SkillDef specialist = tfcs$lastSpecialist;
        tfcs$lastSpecialist = null;

        ItemStack result = ItemStack.EMPTY;
        try {
            result = ((IItemHandlerModifiable) INVENTORY_FIELD.get(this)).getStackInSlot(0);
        } catch (Exception ignored) {}

        if (result.isEmpty()) return;
        ForgingBonus bonus = ForgingBonusComponent.get(result);
        if (bonus == ForgingBonus.NONE) return;

        if (ServerConfig.INSTANCE.grantXp.get()) {
            TFCSewingAPI.grantXp(player, specialist);
            PacketDistributor.sendToPlayer(player, new SyncSkillPacket(SkillAttachments.get(player)));
        }

        if (ServerConfig.INSTANCE.applySkillExtra.get()) {
            TFCSewingAPI.applySkillBonuses(result, player, specialist);
        }
    }

    @Inject(method = "weld", at = @At("HEAD"))
    private void onWeldStart(Player player, CallbackInfoReturnable<InteractionResult> cir) {
        tfcs$weldExtra = null;
        tfcs$weldRank = null;
        tfcs$weldType = null;
        try {
            IItemHandlerModifiable inv = (IItemHandlerModifiable) INVENTORY_FIELD.get(this);
            Float extra0 = inv.getStackInSlot(0).get(SkillComponents.SKILL_EXTRA.get());
            Float extra1 = inv.getStackInSlot(1).get(SkillComponents.SKILL_EXTRA.get());
            if (extra0 != null && (extra1 == null || extra0 >= extra1)) {
                tfcs$weldExtra = extra0;
                tfcs$weldRank = inv.getStackInSlot(0).get(SkillComponents.SKILL_RANK.get());
                tfcs$weldType = inv.getStackInSlot(0).get(SkillComponents.SKILL_TYPE.get());
            } else if (extra1 != null) {
                tfcs$weldExtra = extra1;
                tfcs$weldRank = inv.getStackInSlot(1).get(SkillComponents.SKILL_RANK.get());
                tfcs$weldType = inv.getStackInSlot(1).get(SkillComponents.SKILL_TYPE.get());
            }
        } catch (Exception ignored) {}
    }

    @Inject(method = "weld", at = @At("RETURN"))
    private void onWeldEnd(Player player, CallbackInfoReturnable<InteractionResult> cir) {
        if (tfcs$weldExtra == null || tfcs$weldExtra <= 0f) return;
        try {
            IItemHandlerModifiable inv = (IItemHandlerModifiable) INVENTORY_FIELD.get(this);
            ItemStack result = inv.getStackInSlot(0);
            if (result.isEmpty()) return;
            result.set(SkillComponents.SKILL_EXTRA.get(), tfcs$weldExtra);
            if (tfcs$weldExtra > 0f && result.isDamageableItem()) {
                int maxDmg = result.getMaxDamage();
                if (maxDmg > 0) result.set(net.minecraft.core.component.DataComponents.MAX_DAMAGE, (int)(maxDmg * (1f + tfcs$weldExtra)));
            }
            if (tfcs$weldRank != null) result.set(SkillComponents.SKILL_RANK.get(), tfcs$weldRank);
            if (tfcs$weldType != null) result.set(SkillComponents.SKILL_TYPE.get(), tfcs$weldType);
        } catch (Exception ignored) {}
    }
}
