package io.github.finalparadox.arena;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.B5EncounterManager;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class ArenaCommands {
    private ArenaCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("arena")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("deploy")
                        .then(Commands.literal("b1")
                                .executes(context -> deploy(context, ArenaDefinitions.B1, defaultAnchor(context.getSource())))
                                .then(Commands.argument("anchor", BlockPosArgument.blockPos())
                                        .executes(context -> deploy(context, ArenaDefinitions.B1,
                                                BlockPosArgument.getBlockPos(context, "anchor")))))
                        .then(Commands.literal("marawthar")
                                .executes(context -> deploy(context, ArenaDefinitions.MARAWTHAR, defaultAnchor(context.getSource())))
                                .then(Commands.argument("anchor", BlockPosArgument.blockPos())
                                        .executes(context -> deploy(context, ArenaDefinitions.MARAWTHAR,
                                                BlockPosArgument.getBlockPos(context, "anchor")))))
                        .then(Commands.literal("b5")
                                .executes(context -> deploy(context, ArenaDefinitions.B5, defaultAnchor(context.getSource())))
                                .then(Commands.argument("anchor", BlockPosArgument.blockPos())
                                        .executes(context -> deploy(context, ArenaDefinitions.B5,
                                                BlockPosArgument.getBlockPos(context, "anchor"))))))
                .then(Commands.literal("status").executes(ArenaCommands::status))
                .then(Commands.literal("reset")
                        .then(Commands.literal("b1").executes(context -> reset(context, ArenaDefinitions.B1)))
                        .then(Commands.literal("marawthar")
                                .executes(context -> reset(context, ArenaDefinitions.MARAWTHAR)))
                        .then(Commands.literal("b5").executes(context -> reset(context, ArenaDefinitions.B5))))
                .then(Commands.literal("start")
                        .then(Commands.literal("b1").executes(ArenaCommands::startB1))
                        .then(Commands.literal("marawthar").executes(ArenaCommands::startMarawThar))
                        .then(Commands.literal("b5").executes(ArenaCommands::startB5)));
    }

    private static BlockPos defaultAnchor(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player != null ? player.blockPosition().below() : BlockPos.containing(source.getPosition());
    }

    private static int deploy(
            CommandContext<CommandSourceStack> context,
            ArenaDefinition definition,
            BlockPos anchor
    ) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level);

        if (data.state() == ArenaDeploymentData.DeploymentState.DEPLOYING) {
            source.sendFailure(Component.literal("An arena deployment is already running; wait for it or reset its arena id."));
            return 0;
        }
        ArenaDefinitions.find(data.arenaId()).ifPresent(existing -> {
            if (existing == ArenaDefinitions.B5 && !B5EncounterManager.isActive(level)) {
                B5ArenaStaging.cleanupWaiting(level, data);
            }
        });
        if (ArenaDefinitions.find(data.arenaId()).flatMap(existing -> ArenaDeploymentManager.findActiveBoss(level, data, existing)).isPresent()) {
            source.sendFailure(Component.literal("The recorded arena still has a living boss. Defeat or remove it first."));
            return 0;
        }
        if (!level.isInWorldBounds(definition.minimumCorner(anchor)) || !level.isInWorldBounds(definition.maximumCorner(anchor))) {
            source.sendFailure(Component.literal("The arena volume would extend outside this dimension's build bounds."));
            return 0;
        }

        B5EncounterManager.reset(level);
        data.begin(definition, anchor);
        source.sendSuccess(() -> Component.literal("Started " + definition.id().toUpperCase()
                + " arena deployment at floor anchor " + ArenaDeploymentManager.format(anchor)
                + " (" + definition.tileCount() + " staged template"
                + (definition.tileCount() == 1 ? "" : "s") + ")."), true);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context, ArenaDefinition definition) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level);
        if (!matchesRecordedDefinition(data, definition) || data.floorAnchor().isEmpty()) {
            source.sendFailure(Component.literal("No recorded " + definition.id().toUpperCase()
                    + " arena exists in this dimension."));
            return 0;
        }
        if (definition == ArenaDefinitions.B5 && !B5EncounterManager.isActive(level)) {
            B5ArenaStaging.cleanupWaiting(level, data);
        }
        if (ArenaDeploymentManager.findActiveBoss(level, data, definition).isPresent()) {
            source.sendFailure(Component.literal("The " + definition.id().toUpperCase()
                    + " arena still has a living boss. Defeat or remove it first."));
            return 0;
        }
        B5EncounterManager.reset(level);
        BlockPos anchor = data.floorAnchor().orElseThrow();
        data.begin(definition, anchor);
        source.sendSuccess(() -> Component.literal("Restarted " + definition.id().toUpperCase()
                + " arena deployment at "
                + ArenaDeploymentManager.format(anchor) + "."), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ArenaDeploymentData data = ArenaDeploymentData.get(source.getLevel());
        if (data.state() == ArenaDeploymentData.DeploymentState.IDLE) {
            source.sendSuccess(() -> Component.literal("No arena is recorded in this dimension."), false);
            return 1;
        }
        int total = ArenaDefinitions.find(data.arenaId()).map(ArenaDefinition::tileCount).orElse(0);
        String anchor = data.floorAnchor().map(ArenaDeploymentManager::format).orElse("unknown");
        String detail = data.state() == ArenaDeploymentData.DeploymentState.ERROR ? "; error=" + data.error() : "";
        source.sendSuccess(() -> Component.literal("Arena " + data.arenaId() + ": " + data.state().name().toLowerCase()
                + ", tiles=" + data.nextTile() + "/" + total + ", floor anchor=" + anchor + detail), false);
        return 1;
    }

    private static int startB1(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ArenaDefinition definition = ArenaDefinitions.B1;
        ArenaDeploymentData data = ArenaDeploymentData.get(level);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY || !data.arenaId().equals(definition.id())
                || data.floorAnchor().isEmpty()) {
            source.sendFailure(Component.literal("B1 is not ready. Deploy it first and check arena status."));
            return 0;
        }
        if (ArenaDeploymentManager.findActiveBoss(level, data, definition).isPresent()) {
            source.sendFailure(Component.literal("A living Apiglo already exists in the recorded B1 arena."));
            return 0;
        }

        BlockPos spawn = definition.bossSpawnBlock(data.floorAnchor().orElseThrow());
        ApigloBossEntity boss = ModEntities.APIGLO.get().create(level);
        if (boss == null) {
            source.sendFailure(Component.literal("Could not create the Apiglo entity."));
            return 0;
        }
        boss.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), 90.0F, 0.0F);
        if (!level.addFreshEntity(boss)) {
            source.sendFailure(Component.literal("Could not add Apiglo to the world."));
            return 0;
        }
        data.setActiveBossUuid(boss.getUUID());
        source.sendSuccess(() -> Component.literal("Apiglo created for B1 at logical anchor "
                + ArenaDeploymentManager.format(spawn) + "."), true);
        return 1;
    }

    private static int startMarawThar(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ArenaDefinition definition = ArenaDefinitions.MARAWTHAR;
        ArenaDeploymentData data = ArenaDeploymentData.get(level);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !matchesRecordedDefinition(data, definition)
                || data.floorAnchor().isEmpty()) {
            source.sendFailure(Component.literal("Maraw‘Thar arena is not ready. Deploy it first and check arena status."));
            return 0;
        }
        if (ArenaDeploymentManager.findActiveBoss(level, data, definition).isPresent()) {
            source.sendFailure(Component.literal("A living Maraw‘Thar already exists in the recorded arena."));
            return 0;
        }

        BlockPos spawn = definition.bossSpawnBlock(data.floorAnchor().orElseThrow());
        MarawTharBossEntity boss = ModEntities.MARAWTHAR.get().create(level);
        if (boss == null) {
            source.sendFailure(Component.literal("Could not create the Maraw‘Thar entity."));
            return 0;
        }
        boss.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), 90.0F, 0.0F);
        if (!level.addFreshEntity(boss)) {
            source.sendFailure(Component.literal("Could not add Maraw‘Thar to the world."));
            return 0;
        }
        data.setActiveBossUuid(boss.getUUID());
        source.sendSuccess(() -> Component.literal("Maraw‘Thar created at logical anchor "
                + ArenaDeploymentManager.format(spawn) + "."), true);
        return 1;
    }

    private static int startB5(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        ArenaDefinition definition = ArenaDefinitions.B5;
        ArenaDeploymentData data = ArenaDeploymentData.get(level);
        if (data.state() != ArenaDeploymentData.DeploymentState.READY
                || !matchesRecordedDefinition(data, definition)
                || data.floorAnchor().isEmpty()) {
            source.sendFailure(Component.literal("B5 arena is not ready. Deploy it first and check arena status."));
            return 0;
        }
        BlockPos anchor = data.floorAnchor().orElseThrow();
        if (B5EncounterManager.isActive(level)) {
            source.sendFailure(Component.literal("The B5 encounter is already active."));
            return 0;
        }
        if (B5ArenaStaging.find(level, data).isEmpty()
                && B5ArenaStaging.spawn(level, data, anchor).isEmpty()) {
            source.sendFailure(Component.literal("Could not restore the staged B5 entities."));
            return 0;
        }
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("A player must start B5 so the arena admission check can run."));
            return 0;
        }
        if (!B5ArenaStaging.allPlayersInside(level, anchor)) {
            source.sendFailure(Component.literal("All players must be within the B5 fight arena."));
            return 0;
        }
        if (!B5ArenaStaging.beginEncounter(player)) {
            source.sendFailure(Component.literal("Could not start the staged B5 encounter."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("B5 countdown started at floor anchor "
                + ArenaDeploymentManager.format(anchor) + "."), true);
        return 1;
    }

    private static boolean matchesRecordedDefinition(
            ArenaDeploymentData data,
            ArenaDefinition expected
    ) {
        return ArenaDefinitions.find(data.arenaId())
                .map(definition -> definition == expected)
                .orElse(false);
    }
}
