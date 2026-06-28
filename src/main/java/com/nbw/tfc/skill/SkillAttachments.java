package com.nbw.tfc.skill;

import com.nbw.tfc.TFCSmithingSkill;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import java.util.function.Supplier;

public class SkillAttachments {

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
        DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, TFCSmithingSkill.MODID);

    public static final Supplier<AttachmentType<SkillData>> SKILL_DATA = ATTACHMENTS.register(
        "skill_data",
        () -> AttachmentType.serializable(SkillData::new).copyOnDeath().build()
    );

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }

    public static SkillData get(Player player) {
        return player.getData(SKILL_DATA);
    }
}
