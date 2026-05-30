package com.nbw.tfc.event;

import com.nbw.tfc.skill.SkillAttachments;
import com.nbw.tfc.skill.SkillData;
import com.nbw.tfc.skill.network.SyncSkillPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerEvents {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SkillData data = SkillAttachments.get(sp);
            PacketDistributor.sendToPlayer(sp, new SyncSkillPacket(data));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) {
            SkillData data = SkillAttachments.get(sp);
            PacketDistributor.sendToPlayer(sp, new SyncSkillPacket(data));
        }
    }
}
