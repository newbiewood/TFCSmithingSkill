package com.nbw.tfc;

import com.nbw.tfc.api.ItemSkillRules;
import com.nbw.tfc.command.SkillCommand;
import com.nbw.tfc.event.PlayerEvents;
import com.nbw.tfc.event.SkillBonusEvents;
import com.nbw.tfc.skill.SkillAttachments;
import com.nbw.tfc.skill.SkillComponents;
import com.nbw.tfc.skill.SkillDef;
import com.nbw.tfc.skill.config.ServerConfig;
import com.nbw.tfc.skill.network.SyncSkillPacket;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@Mod(TFCSmithingSkill.MODID)
public class TFCSmithingSkill {

    public static final String MODID = "tfcsmithingskill";

    public TFCSmithingSkill(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerPayloads);

        SkillAttachments.register(modEventBus);
        SkillComponents.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);

        ItemSkillRules.addBuiltinRules();

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(SkillBonusEvents.class);
        NeoForge.EVENT_BUS.register(PlayerEvents.class);
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        registerConfigMapping(SkillDef.TOOLSMITH, ServerConfig.INSTANCE.customToolsmithItems.get());
        registerConfigMapping(SkillDef.WEAPONSMITH, ServerConfig.INSTANCE.customWeaponsmithItems.get());
        registerConfigMapping(SkillDef.ARMORSMITH, ServerConfig.INSTANCE.customArmorsmithItems.get());
    }

    private void registerConfigMapping(SkillDef skill, List<? extends String> items) {
        for (String itemId : items) {
            String trimmed = itemId.trim();
            if (trimmed.isEmpty()) continue;
            ResourceLocation id = ResourceLocation.tryParse(trimmed);
            if (id != null) {
                ItemSkillRules.registerItemRule(
                    ResourceLocation.fromNamespaceAndPath(MODID, "config_" + skill.id + "_" + id.toString().replace(':', '_')),
                    stack -> {
                        ResourceLocation stackId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                        return id.equals(stackId);
                    },
                    skill
                );
            }
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncSkillPacket.TYPE, SyncSkillPacket.CODEC, SyncSkillPacket::handle);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        SkillCommand.register(event.getDispatcher());
    }
}
