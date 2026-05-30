package com.nbw.tfc.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nbw.tfc.skill.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.nbw.tfc.skill.network.SyncSkillPacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class SkillCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tfcskill")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("get")
                    .executes(SkillCommand::listAll)
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (SkillDef s : SkillDef.values()) builder.suggest(s.id);
                            return builder.buildFuture();
                        })
                        .executes(SkillCommand::getSkill)
                    )
                )
                .then(Commands.literal("add")
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (SkillDef s : SkillDef.values()) builder.suggest(s.id);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100000))
                            .executes(SkillCommand::addXp)
                        )
                    )
                )
                .then(Commands.literal("set")
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (SkillDef s : SkillDef.values()) builder.suggest(s.id);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 100000))
                            .executes(SkillCommand::setXp)
                        )
                    )
                )
.then(Commands.literal("rankset")
                    .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (SkillDef s : SkillDef.values()) builder.suggest(s.id);
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("rank", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("novice");
                                builder.suggest("adept");
                                builder.suggest("expert");
                                builder.suggest("master");
                                return builder.buildFuture();
                            })
                            .executes(SkillCommand::rankSet)
                        )
                    )
                )
        );
    }

    private static SkillDef parseSkill(String name) {
        for (SkillDef s : SkillDef.values()) {
            if (s.id.equalsIgnoreCase(name)) return s;
        }
        return null;
    }

    private static int listAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SkillData data = SkillAttachments.get(player);
        for (SkillDef s : SkillDef.values()) {
            int xp = data.getXp(s);
            float mult = Skills.skillMult(s, xp);
            float extra = Skills.skillExtra(s, xp);
            String rank = Skills.skillRankName(Skills.baseMult(s, xp));
            ctx.getSource().sendSuccess(() ->
                Component.literal(s.id + ": " + xp + " XP | mult="
                    + String.format("%.2f", mult) + " | extra=+" + String.format("%.1f", extra * 100)
                    + "% | " + rank), false);
        }
        return 1;
    }

    private static int getSkill(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SkillDef skill = parseSkill(StringArgumentType.getString(ctx, "skill"));
        if (skill == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown skill"));
            return 0;
        }
        SkillData data = SkillAttachments.get(player);
        int xp = data.getXp(skill);
        float mult = Skills.skillMult(skill, xp);
        float extra = Skills.skillExtra(skill, xp);
        String rank = Skills.skillRankName(Skills.baseMult(skill, xp));
        ctx.getSource().sendSuccess(() ->
            Component.literal(skill.id + ": " + xp + " XP | mult="
                + String.format("%.2f", mult) + " | extra=+" + String.format("%.1f", extra * 100)
                + "% | " + rank), false);
        return 1;
    }

    private static int addXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SkillDef skill = parseSkill(StringArgumentType.getString(ctx, "skill"));
        if (skill == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown skill"));
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        SkillData data = SkillAttachments.get(player);
        data.addXp(skill, amount);
        PacketDistributor.sendToPlayer(player, new SyncSkillPacket(data));
        ctx.getSource().sendSuccess(() ->
            Component.literal("Added " + amount + " XP to " + skill.id + " (now " + data.getXp(skill) + ")"), true);
        return 1;
    }

    private static int setXp(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SkillDef skill = parseSkill(StringArgumentType.getString(ctx, "skill"));
        if (skill == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown skill"));
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        SkillData data = SkillAttachments.get(player);
        data.setXp(skill, amount);
        PacketDistributor.sendToPlayer(player, new SyncSkillPacket(data));
        ctx.getSource().sendSuccess(() ->
            Component.literal("Set " + skill.id + " XP to " + amount), true);
        return 1;
    }

    private static int rankSet(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        SkillDef skill = parseSkill(StringArgumentType.getString(ctx, "skill"));
        if (skill == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown skill"));
            return 0;
        }
        String rank = StringArgumentType.getString(ctx, "rank").toLowerCase();
        int xp = switch (rank) {
            case "novice" -> 0;
            case "adept"  -> (skill.rate + 2) / 3;
            case "expert" -> skill.rate;
            case "master" -> skill.rate * 3;
            default -> -1;
        };
        if (xp < 0) {
            ctx.getSource().sendFailure(Component.literal("Unknown rank. Use: novice, adept, expert, master"));
            return 0;
        }
        SkillData data = SkillAttachments.get(player);
        data.setXp(skill, xp);
        player.setData(SkillAttachments.SKILL_DATA.get(), data);
        PacketDistributor.sendToPlayer(player, new SyncSkillPacket(data));
        ctx.getSource().sendSuccess(() ->
            Component.literal("Set " + skill.id + " to " + rank + " (XP=" + xp + ")"), true);
        return 1;
    }
}
