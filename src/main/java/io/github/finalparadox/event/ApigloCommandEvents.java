package io.github.finalparadox.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.arena.ArenaCommands;
import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.ZombieSupermatrixEntity;
import io.github.finalparadox.entity.B8EncounterController;
import io.github.finalparadox.entity.B8EncounterManager;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = FinalParadox.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ApigloCommandEvents {
    private ApigloCommandEvents() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("finalparadox")
                .then(ArenaCommands.build())
                .then(Commands.literal("rover")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawnEncounterRover(
                                        context.getSource().getPlayerOrException()))
                                .then(Commands.literal("boss")
                                        .executes(context -> spawnEncounterRover(
                                                context.getSource().getPlayerOrException())))
                                .then(Commands.literal("improved")
                                        .executes(context -> spawnImprovedRover(
                                                context.getSource().getPlayerOrException()))))
                        .then(Commands.literal("damage")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> damageRover(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "amount"))))))
                .then(Commands.literal("supermatrix")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawn")
                                .executes(context -> spawnSupermatrix(
                                        context.getSource().getPlayerOrException(), false))
                                .then(Commands.literal("vulnerable")
                                        .executes(context -> spawnSupermatrix(
                                                context.getSource().getPlayerOrException(), true))))
                        .then(Commands.literal("compact")
                                .executes(context -> setSupermatrixState(
                                        context.getSource().getPlayerOrException(), false)))
                        .then(Commands.literal("vulnerable")
                                .executes(context -> setSupermatrixState(
                                        context.getSource().getPlayerOrException(), true)))
                        .then(Commands.literal("toggle")
                                .executes(context -> toggleSupermatrix(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("remove")
                                .executes(context -> removeSupermatrix(
                                        context.getSource().getPlayerOrException()))))
                .then(Commands.literal("b8")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("status")
                                .executes(context -> b8Status(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("reset")
                                .executes(context -> b8Reset(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("skip")
                                .executes(context -> b8Skip(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("health")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 250))
                                        .executes(context -> b8Health(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "amount")))))
                        .then(Commands.literal("phase")
                                .then(Commands.argument("phase", IntegerArgumentType.integer(1, 5))
                                        .executes(context -> b8Phase(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "phase")))))
                        .then(Commands.literal("vulnerable")
                                .executes(context -> b8Vulnerable(
                                        context.getSource().getPlayerOrException(), true)))
                        .then(Commands.literal("invulnerable")
                                .executes(context -> b8Vulnerable(
                                        context.getSource().getPlayerOrException(), false)))
                        .then(Commands.literal("h1")
                                .executes(context -> b8H1(
                                        context.getSource().getPlayerOrException(), 1))
                                .then(Commands.argument("mode", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> b8H1(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "mode")))))
                        .then(Commands.literal("h2")
                                .executes(context -> b8H2(
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("h4")
                                .then(Commands.argument("variant", IntegerArgumentType.integer(1, 3))
                                        .executes(context -> b8H4(
                                                context.getSource().getPlayerOrException(),
                                                IntegerArgumentType.getInteger(context, "variant")))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("type", StringArgumentType.word())
                                        .executes(context -> b8Add(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "type"))))))
                .then(Commands.literal("apiglo_menu")
                        .then(Commands.argument("boss", StringArgumentType.word())
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .executes(context -> handleGuideAction(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "boss"),
                                                StringArgumentType.getString(context, "action"))))))
                .then(Commands.literal("koros_menu")
                        .then(Commands.argument("echo", StringArgumentType.word())
                                .then(Commands.argument("action", StringArgumentType.word())
                                        .executes(context -> handleKorosAction(
                                                context.getSource().getPlayerOrException(),
                                                StringArgumentType.getString(context, "echo"),
                                                StringArgumentType.getString(context, "action")))))));
    }

    private static int spawnEncounterRover(ServerPlayer player) {
        return TerrastalkerRoverEntity.spawnEncounter(player, player.position())
                .map(rover -> 1)
                .orElse(0);
    }

    private static int spawnImprovedRover(ServerPlayer player) {
        if (TerrastalkerRoverEntity.spawnImproved(player, true)) {
            player.sendSystemMessage(Component.literal("Spawned the improved Terrastalker."));
            return 1;
        }
        return 0;
    }

    private static int damageRover(ServerPlayer player, int amount) {
        if (!(player.getVehicle() instanceof TerrastalkerRoverEntity rover)) {
            player.displayClientMessage(Component.literal("You are not riding a Terrastalker."), true);
            return 0;
        }
        rover.damageEnergy(amount);
        player.displayClientMessage(Component.literal("Damaged the "
                + (rover.isEncounterMode() ? "B8 boss" : "improved")
                + " Terrastalker by " + amount + " energy."), true);
        return 1;
    }

    private static int spawnSupermatrix(ServerPlayer player, boolean vulnerable) {
        if (guardActiveEncounter(player)) return 0;
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-6D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        } else {
            horizontal = horizontal.normalize();
        }
        Vec3 raw = player.position().add(horizontal.scale(5.0D)).add(0.0D, 4.0D, 0.0D);
        Vec3 core = new Vec3(Math.floor(raw.x), Math.floor(raw.y), Math.floor(raw.z));
        ZombieSupermatrixEntity.spawn(player.serverLevel(), core, vulnerable);
        player.sendSystemMessage(Component.literal(vulnerable
                ? "Spawned the vulnerable Zombie Supermatrix model."
                : "Spawned the compact Zombie Supermatrix model."));
        return 1;
    }

    private static int setSupermatrixState(ServerPlayer player, boolean vulnerable) {
        if (guardActiveEncounter(player)) return 0;
        ZombieSupermatrixEntity matrix = nearestSupermatrix(player);
        if (matrix == null) {
            player.sendSystemMessage(Component.literal("No Zombie Supermatrix model found within 96 blocks."));
            return 0;
        }
        matrix.setVulnerable(vulnerable);
        return 1;
    }

    private static int toggleSupermatrix(ServerPlayer player) {
        if (guardActiveEncounter(player)) return 0;
        ZombieSupermatrixEntity matrix = nearestSupermatrix(player);
        if (matrix == null) {
            player.sendSystemMessage(Component.literal("No Zombie Supermatrix model found within 96 blocks."));
            return 0;
        }
        matrix.toggleVulnerable();
        return 1;
    }

    private static int removeSupermatrix(ServerPlayer player) {
        if (guardActiveEncounter(player)) return 0;
        ZombieSupermatrixEntity matrix = nearestSupermatrix(player);
        if (matrix == null) {
            player.sendSystemMessage(Component.literal("No Zombie Supermatrix model found within 96 blocks."));
            return 0;
        }
        matrix.discard();
        return 1;
    }

    private static ZombieSupermatrixEntity nearestSupermatrix(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(
                        ZombieSupermatrixEntity.class, player.getBoundingBox().inflate(96.0D))
                .stream()
                .min((left, right) -> Double.compare(
                        player.distanceToSqr(left), player.distanceToSqr(right)))
                .orElse(null);
    }

    private static boolean guardActiveEncounter(ServerPlayer player) {
        if (!B8EncounterManager.isActive(player.serverLevel())) return false;
        player.sendSystemMessage(Component.literal(
                "A B8 encounter is active and owns the matrix; use /finalparadox b8 commands."));
        return true;
    }

    private static int b8Status(ServerPlayer player) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active in this dimension."));
            return 0;
        }
        player.sendSystemMessage(Component.literal(controller.status()));
        return 1;
    }

    private static int b8Reset(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        boolean wasActive = B8EncounterManager.isActive(level);
        B8EncounterManager.reset(level);
        player.sendSystemMessage(Component.literal(wasActive
                ? "B8 encounter reset; arena and matrix cleaned up."
                : "B8 was already idle; nothing to reset."));
        return 1;
    }

    private static int b8Skip(ServerPlayer player) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.skip(player.serverLevel(),
                B8EncounterManager.encounterData(player.serverLevel()));
        player.sendSystemMessage(Component.literal("B8 boss skipped."));
        return 1;
    }

    private static int b8Health(ServerPlayer player, int amount) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.setHealth(amount);
        player.sendSystemMessage(Component.literal("B8 matrix health set to " + amount + "."));
        return 1;
    }

    private static int b8Phase(ServerPlayer player, int phase) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.forcePhase(phase);
        player.sendSystemMessage(Component.literal("B8 forced to phase " + phase + "."));
        return 1;
    }

    private static int b8Vulnerable(ServerPlayer player, boolean vulnerable) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.setVulnerable(vulnerable);
        player.sendSystemMessage(Component.literal("B8 matrix is now "
                + (vulnerable ? "vulnerable." : "invulnerable (compact).")));
        return 1;
    }

    private static int b8H1(ServerPlayer player, int mode) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.startH1(player.serverLevel(),
                B8EncounterManager.encounterData(player.serverLevel()), mode);
        player.sendSystemMessage(Component.literal("B8 H1 triggered with " + mode + " beam(s)."));
        return 1;
    }

    private static int b8H2(ServerPlayer player) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.startH2(player.serverLevel(),
                B8EncounterManager.encounterData(player.serverLevel()));
        player.sendSystemMessage(Component.literal("B8 H2 gold-module throw triggered."));
        return 1;
    }

    private static int b8H4(ServerPlayer player, int variant) {
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.startH4(player.serverLevel(),
                B8EncounterManager.encounterData(player.serverLevel()), variant);
        player.sendSystemMessage(Component.literal("B8 H4 triggered with variant " + variant + "."));
        return 1;
    }

    private static int b8Add(ServerPlayer player, String type) {
        if (!java.util.Set.of("zombie", "sniper", "golem", "tnt").contains(type)) {
            player.sendSystemMessage(Component.literal("Unknown add type: " + type
                    + " (zombie|sniper|golem|tnt)."));
            return 0;
        }
        B8EncounterController controller = B8EncounterManager.requireController(player.serverLevel());
        if (controller == null) {
            player.sendSystemMessage(Component.literal("No B8 encounter is active."));
            return 0;
        }
        controller.spawnAddWave(player.serverLevel(),
                B8EncounterManager.encounterData(player.serverLevel()), type);
        player.sendSystemMessage(Component.literal("B8 " + type + " add wave spawned."));
        return 1;
    }

    private static int handleGuideAction(ServerPlayer player, String rawUuid, String action) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        Entity entity = player.serverLevel().getEntity(uuid);
        return entity instanceof ApigloBossEntity apiglo ? apiglo.handleGuideAction(player, action) : 0;
    }

    private static int handleKorosAction(ServerPlayer player, String rawUuid, String action) {
        final UUID uuid;
        try {
            uuid = UUID.fromString(rawUuid);
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        Entity entity = player.serverLevel().getEntity(uuid);
        return entity instanceof KorosEchoEntity koros ? koros.handleGuideAction(player, action) : 0;
    }
}
