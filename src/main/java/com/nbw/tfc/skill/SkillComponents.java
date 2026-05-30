package com.nbw.tfc.skill;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.nbw.tfc.TFCSmithingSkill;

public class SkillComponents {

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TFCSmithingSkill.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Float>> SKILL_EXTRA =
        COMPONENTS.register("skill_extra", () ->
            DataComponentType.<Float>builder().persistent(Codec.FLOAT).networkSynchronized(ByteBufCodecs.FLOAT).build()
        );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SKILL_RANK =
        COMPONENTS.register("skill_rank", () ->
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
        );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> SKILL_TYPE =
        COMPONENTS.register("skill_type", () ->
            DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build()
        );

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
