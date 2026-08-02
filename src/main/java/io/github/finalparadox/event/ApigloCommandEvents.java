package io.github.finalparadox.event;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.arena.ArenaCommands;
import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import io.github.finalparadox.entity.ZombieSupermatrixEntity;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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
                                        context.getSource().getPlayerOrException())))
                        .then(Commands.literal("damage")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                        .executes(context -> damageEncounterRover(
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

    private static int damageEncounterRover(ServerPlayer player, int amount) {
        if (!(player.getVehicle() instanceof TerrastalkerRoverEntity rover) || !rover.isEncounterMode()) {
            player.displayClientMessage(Component.literal("You are not riding a B8 Terrastalker."), true);
            return 0;
        }
        rover.damageEnergy(amount);
        return 1;
    }

    private static int spawnSupermatrix(ServerPlayer player, boolean vulnerable) {
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
        ZombieSupermatrixEntity matrix = nearestSupermatrix(player);
        if (matrix == null) {
            player.sendSystemMessage(Component.literal("No Zombie Supermatrix model found within 96 blocks."));
            return 0;
        }
        matrix.setVulnerable(vulnerable);
        return 1;
    }

    private static int toggleSupermatrix(ServerPlayer player) {
        ZombieSupermatrixEntity matrix = nearestSupermatrix(player);
        if (matrix == null) {
            player.sendSystemMessage(Component.literal("No Zombie Supermatrix model found within 96 blocks."));
            return 0;
        }
        matrix.toggleVulnerable();
        return 1;
    }

    private static int removeSupermatrix(ServerPlayer player) {
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
