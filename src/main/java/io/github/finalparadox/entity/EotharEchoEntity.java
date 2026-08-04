package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.MarawTharArenaStaging;
import io.github.finalparadox.registry.ModEntities;
import io.github.finalparadox.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * Server-side Eo'Thar echo used before the Maraw'Thar B9 encounter. It plays
 * the source pre-battle dialogue and exposes the original clickable advice and
 * lore menu, then hands off to the Maraw'Thar boss.
 */
public final class EotharEchoEntity extends Entity {
    private static final DustParticleOptions EOTHAR_DUST =
            new DustParticleOptions(new Vector3f(0.58F, 0.89F, 1.0F), 1.0F);
    private static final long[] DIALOGUE_TICKS = {
            0L, 80L, 160L, 240L, 320L, 400L, 480L, 560L,
            640L, 720L, 800L, 880L, 960L, 1040L, 1120L, 1200L
    };
    private static final String[] DIALOGUE_KEYS = {
            "cld_lore_116", "cld_lore_117", "cld_lore_118",
            "b9_dialogo_1", "b9_dialogo_2", "b9_dialogo_3",
            "b9_dialogo_4", "b9_dialogo_5", "b9_dialogo_6",
            "b9_dialogo_7", "b9_dialogo_8", "b9_dialogo_9",
            "b9_dialogo_10", "b9_dialogo_11", "b9_dialogo_12",
            "b9_dialogo_13"
    };
    private static final int POSSESSION_TICKS = 60;

    private BlockPos arenaAnchor = BlockPos.ZERO;
    private long dialogueStart = -1L;
    private int dialogueStep;
    private boolean menuReady;
    private int possessionTicks = -1;

    public EotharEchoEntity(EntityType<? extends EotharEchoEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    @Nullable
    public static EotharEchoEntity create(ServerLevel level, BlockPos arenaAnchor) {
        EotharEchoEntity echo = ModEntities.EOTHAR_ECHO.get().create(level);
        if (echo != null) echo.arenaAnchor = arenaAnchor.immutable();
        return echo;
    }

    public BlockPos arenaAnchor() {
        return arenaAnchor;
    }

    public Vec3 visualCore() {
        return position().add(0.0D, 1.0D, 0.0D);
    }

    public boolean interactionReady() {
        return menuReady && possessionTicks < 0;
    }

    public void playArrivalEffects() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 core = visualCore();
        server.sendParticles(ParticleTypes.END_ROD, core.x, core.y, core.z,
                30, 0.35D, 0.35D, 0.35D, 0.25D);
        server.sendParticles(ParticleTypes.FLASH, core.x, core.y, core.z, 2, 0, 0, 0, 0);
        server.playSound(null, blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.AMBIENT, 1.0F, 1.2F);
    }

    public void depart() {
        if (level() instanceof ServerLevel server) {
            Vec3 core = visualCore();
            server.sendParticles(ParticleTypes.EXPLOSION, core.x, core.y, core.z, 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, core.x, core.y, core.z, 15, 0, 0, 0, 0.2D);
            server.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.MASTER, 1.0F, 1.0F);
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        Vec3 core = visualCore();
        if (server.random.nextInt(3) == 0) {
            server.sendParticles(EOTHAR_DUST, core.x, core.y + 0.2D, core.z,
                    1, 0.3D, 0.3D, 0.3D, 0.1D);
        }
        if (possessionTicks >= 0) {
            tickPossession(server);
            return;
        }
        if (dialogueStart < 0) {
            dialogueStart = server.getGameTime();
            sendPreBattleLine(server, 0);
            dialogueStep = 1;
        }
        long elapsed = server.getGameTime() - dialogueStart;
        if (dialogueStep < DIALOGUE_TICKS.length
                && elapsed >= DIALOGUE_TICKS[dialogueStep]) {
            sendPreBattleLine(server, dialogueStep);
            dialogueStep++;
        }
        if (elapsed >= 1280L) {
            menuReady = true;
        }
    }

    private void sendPreBattleLine(ServerLevel server, int index) {
        String speaker = index <= 2 || (index >= 4 && index <= 14)
                ? "b10_boss_name" : "eothar";
        Component message = Component.empty()
                .append(Component.translatable(speaker).withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(0xFF5555)).withBold(true).withItalic(true)))
                .append(Component.translatable(DIALOGUE_KEYS[index]));
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && player.distanceToSqr(Vec3.atCenterOf(arenaAnchor)) <= 100.0D * 100.0D) {
                player.sendSystemMessage(message);
            }
        }
    }

    private void tickPossession(ServerLevel server) {
        possessionTicks++;
        if (possessionTicks == 1) {
            server.playSound(null, arenaAnchor, SoundEvents.END_PORTAL_SPAWN,
                    SoundSource.MASTER, 1.0F, 0.7F);
        }
        if (possessionTicks % 10 == 0) {
            Vec3 center = Vec3.atCenterOf(arenaAnchor);
            server.sendParticles(ParticleTypes.PORTAL, center.x, center.y + 1.0D, center.z,
                    12, 1.5D, 1.0D, 1.5D, 0.0D);
            server.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y + 1.0D, center.z,
                    6, 0.6D, 0.8D, 0.6D, 0.02D);
        }
        if (possessionTicks == 40) {
            server.playSound(null, arenaAnchor, SoundEvents.GENERIC_EXPLODE,
                    SoundSource.MASTER, 1.0F, 0.6F);
            server.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 1.0D, getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        if (possessionTicks >= POSSESSION_TICKS) {
            ArenaDeploymentData data = ArenaDeploymentData.get(server);
            if (MarawTharArenaStaging.spawnBoss(server, data, arenaAnchor).isPresent()) {
                depart();
            } else {
                possessionTicks = -1;
                menuReady = true;
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        handleMenu(serverPlayer, "main");
        return InteractionResult.CONSUME;
    }

    public int handleMenu(ServerPlayer player, String action) {
        if (!canUse(player)) return 0;
        switch (action) {
            case "main" -> showMain(player);
            case "advice" -> showAdvice(player);
            case "explain" -> showExplain(player, 0);
            case "explain_1" -> showExplain(player, 1);
            case "explain_2" -> showExplain(player, 2);
            case "explain_3" -> showExplain(player, 3);
            case "challenge" -> showChallenge(player);
            case "confirm" -> startPossession(player);
            default -> {
                return 0;
            }
        }
        return 1;
    }

    private boolean canUse(ServerPlayer player) {
        if (!interactionReady() || player.level() != level()
                || !player.isAlive() || player.distanceToSqr(this) > 100.0D) {
            return false;
        }
        ArenaDeploymentData data = ArenaDeploymentData.get(player.serverLevel());
        return data.state() == ArenaDeploymentData.DeploymentState.READY
                && ArenaDefinitions.MARAWTHAR.id().equals(data.arenaId())
                && !data.marawTharTriggered()
                && data.activeBossUuid().isEmpty()
                && data.floorAnchor().map(arenaAnchor::equals).orElse(false)
                && data.eotharUuid().map(getUUID()::equals).orElse(false);
    }

    private void showMain(ServerPlayer player) {
        sendSpeakerLine(player, "eothar", "b9_info_eothar_0");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("b9_info_eothar_1", "advice"));
        player.sendSystemMessage(button("b9_info_eothar_2", "explain"));
        player.sendSystemMessage(button(
                "luisb1202.functions.talentos.minikoros.dialogos.b1.ini.3", "challenge"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable(
                "luisb1202.functions.carga_lanas.106_interseccion_4.dialogos.dan_larock.1.4"));
        playGuideSound(player);
    }

    private void showAdvice(ServerPlayer player) {
        sendSpeakerLine(player, "eothar", "b9_info_eothar_3");
        player.sendSystemMessage(Component.translatable("b9_info_eothar_4"));
        player.sendSystemMessage(hover("b9_info_eothar_13", "b9_info_eothar_14"));
        player.sendSystemMessage(hover("b9_info_eothar_15", "b9_info_eothar_16"));
        player.sendSystemMessage(hover("b9_info_eothar_5", "b9_info_eothar_9"));
        player.sendSystemMessage(hover("b9_info_eothar_25", "b9_info_eothar_26"));
        player.sendSystemMessage(hover("b9_info_eothar_6", "b9_info_eothar_10"));
        player.sendSystemMessage(hover("b9_info_eothar_7", "b9_info_eothar_11"));
        player.sendSystemMessage(hover("b9_info_eothar_8", "b9_info_eothar_12"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(backButton());
        playGuideSound(player);
    }

    private void showExplain(ServerPlayer player, int step) {
        switch (step) {
            case 0 -> {
                sendSpeakerLine(player, "eothar", "b9_info_eothar_17");
                player.sendSystemMessage(button("b9_info_eothar_18", "explain_1"));
            }
            case 1 -> {
                sendSpeakerLine(player, "eothar", "b9_info_eothar_19");
                player.sendSystemMessage(button("b9_info_eothar_20", "explain_2"));
            }
            case 2 -> {
                sendSpeakerLine(player, "eothar", "b9_info_eothar_21");
                player.sendSystemMessage(button("b9_info_eothar_22", "explain_3"));
            }
            default -> {
                sendSpeakerLine(player, "eothar", "b9_info_eothar_23");
                player.sendSystemMessage(backButton());
            }
        }
        playGuideSound(player);
    }

    private void showChallenge(ServerPlayer player) {
        ServerLevel server = player.serverLevel();
        if (!allPlayersReady(server, arenaAnchor)) {
            sendSpeakerLine(player, "eothar", "b9_dialogo_20");
            player.sendSystemMessage(backButton());
            playGuideSound(player);
            return;
        }
        sendSpeakerLine(player, "eothar", "b9_info_eothar_24");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(
                "luisb1202.functions.talentos.minikoros.dialogos.b1.ini.3", "confirm"));
        player.sendSystemMessage(backButton());
        playGuideSound(player);
    }

    private static boolean allPlayersReady(ServerLevel server, BlockPos anchor) {
        Vec3 center = Vec3.atCenterOf(anchor);
        for (ServerPlayer player : server.getServer().getPlayerList().getPlayers()) {
            if (player.level() != server || player.isSpectator()
                    || player.distanceToSqr(center) > 16.0D * 16.0D
                    || !player.getOffhandItem().is(ModItems.ATACROM_GAUNTLET.get())) {
                return false;
            }
        }
        return true;
    }

    private void startPossession(ServerPlayer player) {
        if (possessionTicks >= 0) return;
        possessionTicks = 0;
        player.serverLevel().playSound(null, arenaAnchor, SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.MASTER, 1.0F, 1.0F);
    }

    private void sendSpeakerLine(ServerPlayer player, String speakerKey, String lineKey) {
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(Component.translatable(speakerKey).withStyle(Style.EMPTY
                        .withColor(TextColor.fromRgb(0x94E4FF)).withBold(true).withItalic(true)))
                .append(Component.translatable(lineKey)));
    }

    private Component button(String key, String action) {
        return Component.translatable(key).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/finalparadox eothar_menu " + getUUID() + " " + action)));
    }

    private Component backButton() {
        return Component.translatable(
                "luisb1202.functions.talentos.minikoros.dialogos.10_gris.d1.2")
                .setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/finalparadox eothar_menu " + getUUID() + " main")));
    }

    private static Component hover(String key, String detailKey) {
        return Component.translatable(key).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT, Component.translatable(detailKey))));
    }

    private static void playGuideSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.GRASS_BREAK, SoundSource.MASTER, 0.5F, 2.0F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ArenaAnchor")) arenaAnchor = BlockPos.of(tag.getLong("ArenaAnchor"));
        if (tag.contains("DialogueStart")) dialogueStart = tag.getLong("DialogueStart");
        if (tag.contains("DialogueStep")) dialogueStep = tag.getInt("DialogueStep");
        if (tag.contains("MenuReady")) menuReady = tag.getBoolean("MenuReady");
        if (tag.contains("PossessionTicks")) possessionTicks = tag.getInt("PossessionTicks");
        setInvulnerable(true);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("ArenaAnchor", arenaAnchor.asLong());
        tag.putLong("DialogueStart", dialogueStart);
        tag.putInt("DialogueStep", dialogueStep);
        tag.putBoolean("MenuReady", menuReady);
        tag.putInt("PossessionTicks", possessionTicks);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity attacker) {
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}