package com.nbw.tfc.skill.network;

import com.nbw.tfc.TFCSmithingSkill;
import com.nbw.tfc.skill.SkillAttachments;
import com.nbw.tfc.skill.SkillData;
import com.nbw.tfc.skill.SkillDef;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncSkillPacket(int[] xpValues) implements CustomPacketPayload {

    public static final Type<SyncSkillPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(TFCSmithingSkill.MODID, "sync_skills"));

    public static final StreamCodec<ByteBuf, SyncSkillPacket> CODEC = new StreamCodec<>() {
        @Override
        public SyncSkillPacket decode(ByteBuf buf) {
            int count = buf.readInt();
            int[] values = new int[count];
            for (int i = 0; i < count; i++) values[i] = buf.readInt();
            return new SyncSkillPacket(values);
        }
        @Override
        public void encode(ByteBuf buf, SyncSkillPacket p) {
            buf.writeInt(p.xpValues.length);
            for (int v : p.xpValues) buf.writeInt(v);
        }
    };

    public SyncSkillPacket(SkillData data) {
        this(makeArray(data));
    }

    private static int[] makeArray(SkillData data) {
        SkillDef[] skills = SkillDef.values();
        int[] arr = new int[skills.length];
        for (int i = 0; i < skills.length; i++) arr[i] = data.getXp(skills[i]);
        return arr;
    }

    public void apply(SkillData data) {
        SkillDef[] skills = SkillDef.values();
        for (int i = 0; i < skills.length && i < xpValues.length; i++) {
            data.setXp(skills[i], xpValues[i]);
        }
    }

    public static void handle(SyncSkillPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() != null) {
                var data = SkillAttachments.get(ctx.player());
                packet.apply(data);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
