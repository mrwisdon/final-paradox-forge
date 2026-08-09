package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.ArenaEntranceMemory;
import io.github.finalparadox.arena.B1ArenaStaging;
import io.github.finalparadox.arena.B2ArenaStaging;
import io.github.finalparadox.arena.B5ArenaStaging;
import io.github.finalparadox.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The original quartz-and-sea-lantern Echo of Koros. Used to introduce B5
 * (guide menu) and placed in the B8 arena after reset (14_verde dialogue).
 */
public final class KorosEchoEntity extends Entity {
    private static final DustParticleOptions WHITE_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.0F);

    private BlockPos arenaAnchor = BlockPos.ZERO;
    private String guideMode = "b5";
    private long b8DialogueStart = -1L;
    private int b8DialogueStep;
    private boolean b8IntroStarted;
    private final Set<UUID> b8EntranceViewers = new HashSet<>();

    public KorosEchoEntity(EntityType<? extends KorosEchoEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
        setInvulnerable(true);
    }

    @Nullable
    public static KorosEchoEntity create(ServerLevel level, BlockPos arenaAnchor) {
        KorosEchoEntity echo = ModEntities.KOROS_ECHO.get().create(level);
        if (echo != null) echo.arenaAnchor = arenaAnchor.immutable();
        return echo;
    }

    @Nullable
    public static KorosEchoEntity createB8(ServerLevel level, BlockPos arenaAnchor) {
        KorosEchoEntity echo = create(level, arenaAnchor);
        if (echo != null) echo.guideMode = "b8";
        return echo;
    }

    @Nullable
    public static KorosEchoEntity createB1(ServerLevel level, BlockPos arenaAnchor) {
        KorosEchoEntity echo = create(level, arenaAnchor);
        if (echo != null) echo.guideMode = "b1";
        return echo;
    }

    @Nullable
    public static KorosEchoEntity createB2(ServerLevel level, BlockPos arenaAnchor) {
        KorosEchoEntity echo = create(level, arenaAnchor);
        if (echo != null) echo.guideMode = "b2";
        return echo;
    }

    public BlockPos arenaAnchor() {
        return arenaAnchor;
    }

    public Vec3 visualCore() {
        return position().add(0.0D, 1.0D, 0.0D);
    }

    public void playArrivalEffects() {
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 core = visualCore();
        server.sendParticles(ParticleTypes.EXPLOSION, core.x, core.y, core.z, 1, 0, 0, 0, 0);
        server.sendParticles(ParticleTypes.END_ROD, core.x, core.y, core.z, 25, 0.3, 0.3, 0.3, 0.3);
        server.sendParticles(ParticleTypes.FLASH, core.x, core.y, core.z, 2, 0, 0, 0, 0);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.AMBIENT, 1.0F, 0.5F);
    }

    public void depart() {
        if (level() instanceof ServerLevel server) {
            Vec3 core = visualCore();
            server.sendParticles(ParticleTypes.EXPLOSION, core.x, core.y, core.z, 1, 0, 0, 0, 0);
            server.sendParticles(ParticleTypes.LARGE_SMOKE, core.x, core.y, core.z, 15, 0, 0, 0, 0.2D);
            server.playSound(null, blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.MASTER, 1.0F, 1.7F);
        }
        discard();
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (!(level() instanceof ServerLevel server)) return;
        Vec3 core = visualCore();
        if (server.random.nextInt(3) == 0) {
            server.sendParticles(WHITE_DUST, core.x, core.y + 0.2D, core.z,
                    1, 0.3, 0.3, 0.3, 0.1D);
        }
        for (ServerPlayer player : server.players()) {
            if (player.distanceToSqr(core) <= 36.0D && !player.hasEffect(MobEffects.REGENERATION)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 1, true, false));
            }
        }
        if ("b5".equals(guideMode)) {
            ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B5);
            data.stagedGariUuid().map(server::getEntity)
                    .filter(GariBossEntity.class::isInstance)
                    .map(GariBossEntity.class::cast)
                    .filter(GariBossEntity::isAlive)
                    .ifPresent(gari -> {
                        int frame = Math.floorMod(tickCount - 1, 16) + 1;
                        double radius = frame <= 2 || frame >= 15 ? 1.5D
                                : frame <= 4 ? 1.45D
                                : frame <= 6 || frame >= 13 ? 1.4D
                                : frame <= 8 || frame >= 11 ? 1.3D : 1.25D;
                        B5Particles.shieldRing(server, gari.position().add(0, 1.4D, 0), radius);
                    });
        }
        if ("b8".equals(guideMode)) {
            // Source matriz/gen_no_boss: the matrix announces itself when a
            // player approaches, then Koros tells him to talk before fighting.
            ArenaDeploymentData data = ArenaDeploymentData.get(server, ArenaDefinitions.B8);
            if (!b8IntroStarted && !data.b8Triggered()) {
                b8EntranceViewers.clear();
                for (ServerPlayer player : server.players()) {
                    if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                            && player.distanceToSqr(this) <= 24.0D * 24.0D
                            && ArenaEntranceMemory.markIfFirst(player, "b8")) {
                        b8EntranceViewers.add(player.getUUID());
                    }
                }
                if (!b8EntranceViewers.isEmpty()) {
                    b8IntroStarted = true;
                    b8DialogueStart = server.getGameTime();
                    b8DialogueStep = 0;
                    sendB8IntroLine(server, 0);
                }
            }
            if (b8DialogueStart >= 0L) {
                // Source timings: matrix lines at +0s, +4s, +4s, then the
                // Koros dia_ini line at +5s.
                long elapsed = server.getGameTime() - b8DialogueStart;
                int step = elapsed >= 260 ? 3 : elapsed >= 160 ? 2 : elapsed >= 80 ? 1 : 0;
                if (step > b8DialogueStep) {
                    b8DialogueStep = step;
                    sendB8IntroLine(server, step);
                }
                if (elapsed >= 320) {
                    b8DialogueStart = -1L;
                    b8EntranceViewers.clear();
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        handleGuideAction(serverPlayer, "main");
        return InteractionResult.CONSUME;
    }

    public int handleGuideAction(ServerPlayer player, String action) {
        if (!canUseGuide(player)) {
            player.sendSystemMessage(Component.translatable("message.finalparadox.koros.interaction.unavailable"));
            return 0;
        }
        if ("b8".equals(guideMode)) {
            switch (action) {
                case "main" -> showB8Main(player);
                case "b8_d1" -> showB8D1(player);
                case "b8_d2" -> showB8D2(player);
                case "b8_start" -> showB8Start(player);
                case "b8_confirm" -> startB8FromKoros(player);
                case "b8_missing" -> showB8Missing(player);
                default -> {
                    return 0;
                }
            }
            return 1;
        }
        if ("b1".equals(guideMode)) {
            ArenaDeploymentData b1Data = ArenaDeploymentData.get(player.serverLevel(), ArenaDefinitions.B1);
            return B1ArenaStaging.find(player.serverLevel(), b1Data)
                    .map(stage -> stage.boss().handleGuideAction(player, action))
                    .orElse(0);
        }
        if ("b2".equals(guideMode)) {
            switch (action) {
                case "main" -> showB2Main(player);
                case "b2_d1" -> showB2D1(player);
                case "b2_d2" -> showB2D2(player);
                case "b2_d5" -> showB2D5(player);
                case "b2_d6" -> showB2D6(player);
                case "b2_d7" -> showB2D7(player);
                case "confirm" -> showB2Confirm(player);
                case "start" -> {
                    if (!B2ArenaStaging.allPlayersInside(player.serverLevel(), arenaAnchor)) {
                        showPlayersMissing(player);
                        return 0;
                    }
                    if (!B2ArenaStaging.beginEncounter(player)) {
                        player.sendSystemMessage(Component.translatable(
                                "message.finalparadox.koros.interaction.unavailable"));
                        return 0;
                    }
                }
                default -> {
                    return 0;
                }
            }
            return 1;
        }
        switch (action) {
            case "main" -> showMain(player);
            case "briefing" -> showBriefing(player);
            case "mechanics" -> showMechanics(player);
            case "confirm" -> showConfirmation(player);
            case "start" -> {
                if (!B5ArenaStaging.beginEncounter(player)) {
                    player.sendSystemMessage(Component.translatable("message.finalparadox.koros.interaction.unavailable"));
                    return 0;
                }
            }
            default -> {
                return 0;
            }
        }
        return 1;
    }

    private boolean canUseGuide(ServerPlayer player) {
        if (player.level() != level() || !player.isAlive()
                || player.distanceToSqr(this) > 100.0D) return false;
        ArenaDeploymentData data = ArenaDeploymentData.get(player.serverLevel(), guideMode);
        if ("b8".equals(guideMode)) {
            return data.state() == ArenaDeploymentData.DeploymentState.READY
                    && ArenaDefinitions.B8.id().equals(data.arenaId())
                    && !B8EncounterManager.isActive(player.serverLevel())
                    && data.floorAnchor().map(arenaAnchor::equals).orElse(false)
                    && data.korosUuid().map(getUUID()::equals).orElse(false);
        }
        if ("b1".equals(guideMode)) {
            return data.state() == ArenaDeploymentData.DeploymentState.READY
                    && ArenaDefinitions.B1.id().equals(data.arenaId())
                    && (data.activeBossUuid().isEmpty()
                            || B1ArenaStaging.hasWaitingBoss(player.serverLevel(), data))
                    && data.floorAnchor().map(arenaAnchor::equals).orElse(false)
                    && data.korosUuid().map(getUUID()::equals).orElse(false);
        }
        if ("b2".equals(guideMode)) {
            return data.state() == ArenaDeploymentData.DeploymentState.READY
                    && ArenaDefinitions.B2.id().equals(data.arenaId())
                    && (data.activeBossUuid().isEmpty()
                            || B2ArenaStaging.hasWaitingBoss(player.serverLevel(), data))
                    && data.floorAnchor().map(arenaAnchor::equals).orElse(false)
                    && data.korosUuid().map(getUUID()::equals).orElse(false);
        }
        if (B5EncounterManager.isActive(player.serverLevel())) return false;
        return data.state() == ArenaDeploymentData.DeploymentState.READY
                && ArenaDefinitions.B5.id().equals(data.arenaId())
                && data.floorAnchor().map(arenaAnchor::equals).orElse(false)
                && data.korosUuid().map(getUUID()::equals).orElse(false);
    }

    private void showMain(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.koros.guide.main");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.warning"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("message.finalparadox.koros.guide.option.help", "briefing"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("message.finalparadox.koros.guide.option.start", "confirm"));
        guideFooter(player);
    }

    private void showBriefing(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.koros.guide.briefing");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("message.finalparadox.koros.guide.continue", "mechanics"));
        player.sendSystemMessage(button("message.finalparadox.koros.guide.back", "main"));
        playGuideSound(player);
    }

    private void showMechanics(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.koros.guide.mechanics.intro");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.section.common"));
        player.sendSystemMessage(ability("arcane_shield", false));
        player.sendSystemMessage(ability("confidence", false));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.section.koyomi"));
        player.sendSystemMessage(ability("trident_rain", false));
        player.sendSystemMessage(ability("arcane_globe", true));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.section.gari"));
        player.sendSystemMessage(ability("multishot", false));
        player.sendSystemMessage(ability("dracotoxic_arrow", true));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.section.interphase"));
        String poisonDetail = player.server.getPlayerList().getPlayers().size() <= 1
                ? "hallucinatory_poison.solo" : "hallucinatory_poison.multi";
        player.sendSystemMessage(ability("hallucinatory_poison", poisonDetail, false));
        player.sendSystemMessage(ability("tridentstorm", false));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("message.finalparadox.koros.guide.back", "main"));
        playGuideSound(player);
    }

    private void showConfirmation(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.koros.guide.confirm");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(button("message.finalparadox.koros.guide.yes", "start"))
                .append(Component.literal("       "))
                .append(button("message.finalparadox.koros.guide.no", "main")));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showPlayersMissing(ServerPlayer player) {
        guideHeader(player, "message.finalparadox.koros.guide.players_missing");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button("message.finalparadox.koros.guide.back", "main"));
        player.sendSystemMessage(Component.empty());
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.MASTER, 1.0F, 2.0F);
    }

    /* ------------------------------ B2 pre-fight Koros ------------------------------ */

    private void showB2Main(ServerPlayer player) {
        guideHeader(player, MK + "b2.ini.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b2.ini.2", "b2_d1"));
        player.sendSystemMessage(button(MK + "b2.ini.3", "b2_d5"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b1.ini.3", "confirm"));
        guideFooter(player);
    }

    private void showB2D1(ServerPlayer player) {
        guideHeader(player, MK + "b2.d1.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b1.d1.2", "b2_d2"));
        player.sendSystemMessage(button(MK + "10_gris.d1.2", "main"));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showB2D2(ServerPlayer player) {
        guideHeader(player, MK + "b2.d2.1");
        player.sendSystemMessage(Component.empty());
        guideEntry(player, MK + "b2.d2.2",
                frag(MK + "b2.d2.3", ChatFormatting.RED, true), frag(MK + "b2.d2.4"),
                frag(MK + "b2.d2.5", ChatFormatting.RED), frag(MK + "b2.d2.6", ChatFormatting.RED),
                frag(MK + "b2.d2.7", ChatFormatting.RED), frag(MK + "b2.d2.8"),
                frag(MK + "b2.d2.9", ChatFormatting.YELLOW), frag(MK + "b2.d2.10"),
                frag(MK + "b2.d2.11", ChatFormatting.AQUA, true),
                frag("item.written_book.1.page.0.2"), frag(MK + "b2.d2.12", ChatFormatting.GOLD),
                frag(MK + "b2.d2.13"), frag(MK + "b2.d2.14", ChatFormatting.GOLD),
                frag(MK + "b2.d2.15"));
        guideEntry(player, MK + "b2.d2.16",
                frag(MK + "b2.d2.17", ChatFormatting.GOLD, true), frag(MK + "b2.d2.18"),
                frag(MK + "b2.d2.19", ChatFormatting.GOLD), frag(MK + "b2.d2.20"),
                frag(MK + "b2.d2.21", ChatFormatting.RED), frag(MK + "b2.d2.22"),
                frag(MK + "b2.d2.23", ChatFormatting.GOLD), frag(MK + "b2.d2.24"),
                frag(MK + "b2.d2.11", ChatFormatting.AQUA, true),
                frag("item.written_book.1.page.0.2"), frag(MK + "b2.d2.25", ChatFormatting.GOLD),
                frag(MK + "b2.d2.26"), frag(MK + "b2.d2.27", ChatFormatting.GOLD),
                frag(MK + "b2.d2.28"), frag(MK + "b2.d2.29", ChatFormatting.AQUA),
                frag(MK + "b2.d2.30"), frag(MK + "b2.d2.31", ChatFormatting.GOLD),
                frag(MK + "b2.d2.32"), frag(MK + "b2.d2.33", ChatFormatting.RED),
                frag("item.written_book.3.page.1.11", ChatFormatting.WHITE),
                frag("item.written_book.2.page.0.3"));
        guideEntry(player, MK + "b2.d2.34",
                frag(MK + "b2.d2.35", ChatFormatting.YELLOW, true), frag(MK + "b2.d2.36"),
                frag(MK + "b2.d2.37", ChatFormatting.GOLD), frag(MK + "b2.d2.38"),
                frag("score.escudo.name.1", ChatFormatting.RED), frag(MK + "b2.d2.39"),
                frag(MK + "b2.d2.40", ChatFormatting.RED), frag(MK + "b2.d2.41"),
                frag(MK + "b2.d2.37", ChatFormatting.GOLD), frag(MK + "b2.d2.42"));
        guideEntry(player, MK + "b2.d2.43",
                frag(MK + "b2.d2.44", ChatFormatting.YELLOW, true), frag(MK + "b2.d2.45"),
                frag(MK + "b2.d2.46", ChatFormatting.DARK_PURPLE), frag(MK + "b2.d2.47"),
                frag(MK + "b2.d2.48", ChatFormatting.RED), frag(MK + "b2.d2.49"));
        guideEntry(player, MK + "b2.d2.50",
                frag(MK + "b2.d2.51", ChatFormatting.YELLOW, true), frag(MK + "b2.d2.52"),
                frag(MK + "b2.d2.53", ChatFormatting.GOLD), frag(MK + "b2.d2.54"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable(MK + "b1.d2.49"));
        guideEntry(player, MK + "b2.d2.55",
                frag(MK + "b2.d2.56", ChatFormatting.RED, true), frag(MK + "b2.d2.57"),
                frag(MK + "b2.d2.58", ChatFormatting.GOLD), frag(MK + "b2.d2.59", ChatFormatting.GRAY),
                frag(MK + "b2.d2.60"), frag(MK + "b2.d2.61", ChatFormatting.GOLD),
                frag(MK + "b2.d2.62"), frag(MK + "b2.d2.63", ChatFormatting.RED),
                frag(MK + "b2.d2.64"), frag(MK + "b2.d2.65", ChatFormatting.LIGHT_PURPLE),
                frag(MK + "b2.d2.66"), frag(MK + "b2.d2.11", ChatFormatting.AQUA, true),
                frag(MK + "b2.d2.67"), frag(MK + "b2.d2.23", ChatFormatting.GOLD),
                frag(MK + "b2.d2.68"));
        guideEntry(player, MK + "b2.d2.69",
                frag(MK + "b2.d2.70", ChatFormatting.YELLOW, true), frag(MK + "b2.d2.71"),
                frag(MK + "b2.d2.72", ChatFormatting.GOLD), frag(MK + "b2.d2.73"),
                frag(MK + "b2.d2.74", ChatFormatting.RED),
                frag("item.written_book.3.page.1.11"));
        player.sendSystemMessage(Component.empty());
        guideEntry(player, MK + "b2.d2.75", frag(MK + "b2.d2.76"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "10_gris.d1.2", "main"));
        playGuideSound(player);
    }

    private void showB2D5(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(MK + "b2.d5.1")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                        .withBold(true).withItalic(true)));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b2.d5.2", "b2_d6"));
        player.sendSystemMessage(button(
                "luisb1202.functions.carga_lanas.11_cian.zachaia.dialogo.mosquitos.5.3", "main"));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showB2D6(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(MK + "b2.d6.1")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                        .withBold(true).withItalic(true)));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b2.d6.2", "b2_d7"));
        player.sendSystemMessage(button(
                "luisb1202.functions.carga_lanas.11_cian.zachaia.dialogo.mosquitos.5.3", "main"));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showB2D7(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable(MK + "b2.d7.1")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                        .withBold(true).withItalic(true)));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(
                "luisb1202.functions.carga_lanas.11_cian.zachaia.dialogo.mosquitos.5.3", "main"));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void showB2Confirm(ServerPlayer player) {
        guideHeader(player, MK + "b1.d3.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(button(MK + "b1.ini.3", "start"))
                .append(Component.literal("       "))
                .append(button(MK + "10_gris.d1.2", "main")));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    private void guideEntry(
            ServerPlayer player, String headingKey, B8GuideFragment... fragments) {
        MutableComponent hover = Component.empty();
        for (B8GuideFragment fragment : fragments) {
            Style style = Style.EMPTY;
            if (fragment.color() != null) style = style.withColor(fragment.color());
            if (fragment.bold()) style = style.withBold(true);
            hover.append(Component.translatable(fragment.key()).withStyle(style));
        }
        player.sendSystemMessage(Component.translatable(headingKey).withStyle(
                Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
    }

    /* ------------------------------ B8 pre-fight Koros ------------------------------ */

    private static final String MK = "luisb1202.functions.talentos.minikoros.dialogos.";
    private static final String ARCO = "luisb1202.functions.carga_lanas.0_intro.elige_tu_destino.msg_arco.";

    /** Source b8/matriz/gen_no_boss -> new_inicio -> dia_ini. */
    private void sendB8IntroLine(ServerLevel server, int step) {
        String messageKey;
        SoundEvent sound;
        float pitch;
        boolean korosSpeaker;
        switch (step) {
            case 0 -> {
                messageKey = "luisb1202.functions.bossfight.b8.dialogos.dia_ini.dia_index.dia_node_0.40";
                korosSpeaker = false;
                sound = SoundEvents.ENDERMAN_AMBIENT;
                pitch = 0.0F;
            }
            case 1 -> {
                messageKey = "luisb1202.functions.bossfight.b8.dialogos.dia_ini.dia_index.dia_node_1.31";
                korosSpeaker = false;
                sound = SoundEvents.ENDERMAN_AMBIENT;
                pitch = 0.0F;
            }
            case 2 -> {
                messageKey = "luisb1202.functions.bossfight.b8.dialogos.dia_ini.dia_index.dia_node_2.49";
                korosSpeaker = false;
                sound = SoundEvents.ENDERMAN_AMBIENT;
                pitch = 0.0F;
            }
            default -> {
                messageKey = "luisb1202.functions.bossfight.b8.dialogos.dia_ini.1";
                korosSpeaker = true;
                sound = SoundEvents.TRIDENT_RETURN;
                pitch = 1.7F;
            }
        }
        Component speaker = korosSpeaker
                ? Component.translatable("luisb1202.functions.afijos.descubrir.hd.3")
                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF))
                        .withBold(true).withItalic(true))
                : Component.translatable("luisb1202.functions.bossfight.b8.dialogos.dia4.1");
        for (ServerPlayer player : server.players()) {
            if (!b8EntranceViewers.contains(player.getUUID())) continue;
            player.sendSystemMessage(speaker.copy().append(Component.translatable(messageKey)));
            server.playSound(null, player.blockPosition(), sound, SoundSource.MASTER, 1.0F, pitch);
        }
    }

    /** Source minikoros/dialogos/b8/ini: guide + start options. */
    private void showB8Main(ServerPlayer player) {
        guideHeader(player, MK + "b3.ini.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b8.ini.1", "b8_d1"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b1.ini.3", "b8_start"));
        guideFooter(player);
    }

    /** Source b8/d1: corrupted Megamatrix lore, continue to the guide. */
    private void showB8D1(ServerPlayer player) {
        guideHeader(player, MK + "b8.d1.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "b1.d1.2", "b8_d2"));
        player.sendSystemMessage(button(MK + "10_gris.d1.2", "main"));
        playGuideSound(player);
    }

    /** Source b8/d2: the full ability guide with hover details. */
    private void showB8D2(ServerPlayer player) {
        guideHeader(player, MK + "b3.d2.1");
        player.sendSystemMessage(Component.empty());
        b8GuideEntry(player, MK + "b8.d2.1",
                frag(MK + "b8.d2.2", ChatFormatting.AQUA, true), frag(MK + "b8.d2.3"),
                frag(MK + "b8.d2.4", ChatFormatting.YELLOW), frag(MK + "b8.d2.5"),
                frag(MK + "b8.d2.6", ChatFormatting.YELLOW), frag(MK + "b8.d2.7"),
                frag(MK + "b3.d2.19", ChatFormatting.YELLOW), frag(MK + "b8.d2.8"),
                frag(MK + "b8.d2.9", ChatFormatting.RED), frag(MK + "b8.d2.10"));
        b8GuideEntry(player, MK + "b8.d2.11",
                frag(MK + "b8.d2.12", ChatFormatting.RED, true),
                frag("luisb1202.functions.carga_lanas.0_intro.lobby.ajustes.trigger_index.7",
                        ChatFormatting.RED),
                frag(MK + "b8.d2.13"), frag(MK + "b8.d2.14", ChatFormatting.GOLD),
                frag(MK + "b8.d2.15"), frag(MK + "b8.d2.16", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.17"), frag(MK + "b8.d2.14", ChatFormatting.GOLD),
                frag(MK + "b8.d2.18"), frag(MK + "b8.d2.19", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.20"), frag(MK + "b8.d2.21", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.22"), frag(MK + "b8.d2.23", ChatFormatting.RED),
                frag(MK + "b8.d2.24"), frag(MK + "b8.d2.14", ChatFormatting.GOLD),
                frag(MK + "b8.d2.25"), frag(MK + "b8.d2.26", ChatFormatting.GREEN),
                frag(MK + "b8.d2.27"));
        b8GuideEntry(player, MK + "b8.d2.28",
                frag(MK + "b8.d2.29", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.30"),
                frag(MK + "b8.d2.31", ChatFormatting.YELLOW), frag(MK + "b8.d2.32"),
                frag(MK + "b8.d2.33", ChatFormatting.YELLOW), frag(MK + "b8.d2.34"),
                frag(MK + "b8.d2.35", ChatFormatting.RED), frag(MK + "b8.d2.36"));
        b8GuideEntry(player, MK + "b8.d2.37",
                frag(MK + "b8.d2.38", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.39"),
                frag(MK + "b8.d2.40", ChatFormatting.YELLOW), frag(MK + "b8.d2.41"));
        b8GuideEntry(player, MK + "b8.d2.42",
                frag(MK + "b8.d2.43", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.44"),
                frag(MK + "b8.d2.45", ChatFormatting.YELLOW), frag(MK + "b8.d2.46"),
                frag(MK + "b8.d2.47", ChatFormatting.YELLOW), frag(MK + "b8.d2.48"),
                frag(MK + "b8.d2.49", ChatFormatting.YELLOW), frag(MK + "b8.d2.50"));
        b8GuideEntry(player, MK + "b8.d2.51",
                frag(MK + "b8.d2.52", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.53"),
                frag(MK + "b8.d2.54", ChatFormatting.YELLOW), frag(MK + "b8.d2.55"));
        b8GuideEntry(player, MK + "b8.d2.56",
                frag(MK + "b8.d2.57", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.58"),
                frag(MK + "b8.d2.59", ChatFormatting.RED), frag(MK + "b8.d2.60"),
                frag(MK + "b8.d2.61", ChatFormatting.YELLOW), frag(MK + "b8.d2.62"));
        b8GuideEntry(player, MK + "b8.d2.63",
                frag(MK + "b8.d2.64", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.65"),
                frag(MK + "b8.d2.66", ChatFormatting.GOLD),
                frag("item.written_book.3.page.1.11"));
        b8GuideEntry(player, MK + "b8.d2.67",
                frag(MK + "b8.d2.4", ChatFormatting.YELLOW, true), frag(MK + "b8.d2.68"),
                frag(MK + "b8.d2.69", ChatFormatting.RED),
                frag(MK + "b8.d2.70", ChatFormatting.DARK_AQUA), frag(MK + "b8.d2.71"),
                frag(MK + "b8.d2.72", ChatFormatting.YELLOW), frag(MK + "b8.d2.73"));
        b8GuideEntry(player, MK + "b8.d2.74",
                frag(MK + "b8.d2.75", ChatFormatting.AQUA, true), frag(MK + "b8.d2.76"),
                frag(MK + "b8.d2.4", ChatFormatting.RED), frag(MK + "b8.d2.77"),
                frag(MK + "b8.d2.78", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.79", ChatFormatting.GRAY),
                frag(MK + "b8.d2.80", ChatFormatting.YELLOW),
                frag("item.written_book.3.page.1.16", ChatFormatting.GRAY),
                frag(MK + "b8.d2.81", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.82", ChatFormatting.GRAY),
                frag(MK + "b8.d2.83", ChatFormatting.YELLOW),
                frag(MK + "b8.d2.84", ChatFormatting.GRAY));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable(MK + "b6.d2.40"));
        player.sendSystemMessage(Component.translatable(MK + "b6.d2.41"));
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "10_gris.d1.2", "main"));
        playGuideSound(player);
    }

    private record B8GuideFragment(String key, ChatFormatting color, boolean bold) {
    }

    private static B8GuideFragment frag(String key) {
        return new B8GuideFragment(key, null, false);
    }

    private static B8GuideFragment frag(String key, ChatFormatting color) {
        return new B8GuideFragment(key, color, false);
    }

    private static B8GuideFragment frag(String key, ChatFormatting color, boolean bold) {
        return new B8GuideFragment(key, color, bold);
    }

    private void b8GuideEntry(
            ServerPlayer player, String headingKey, B8GuideFragment... fragments) {
        guideEntry(player, headingKey, fragments);
    }

    /** Source b8/d3: confirm the challenge. */
    private void showB8Start(ServerPlayer player) {
        guideHeader(player, MK + "b1.d3.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(button(ARCO + "3", "b8_confirm"))
                .append(Component.literal("       "))
                .append(button(ARCO + "4", "main")));
        player.sendSystemMessage(Component.empty());
        playGuideSound(player);
    }

    /** Source b8/check_players -> b8/ini: all inside, start the encounter. */
    private void startB8FromKoros(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        ArenaDeploymentData data = ArenaDeploymentData.get(level, ArenaDefinitions.B8);
        BlockPos anchor = data.floorAnchor().orElse(null);
        if (anchor == null || !b8AllPlayersInside(level, anchor)) {
            showB8Missing(player);
            return;
        }
        depart();
        data.clearKoros();
        B8EncounterManager.begin(level, anchor);
    }

    /** Source b8/d4: some player is outside the arena. */
    private void showB8Missing(ServerPlayer player) {
        guideHeader(player, MK + "b1.d4.1");
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(button(MK + "10_gris.d1.2", "main"));
        player.sendSystemMessage(Component.empty());
        player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND,
                SoundSource.MASTER, 1.0F, 0.0F);
    }

    private static boolean b8AllPlayersInside(ServerLevel level, BlockPos anchor) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.level() != level
                    || player.getX() < anchor.getX() - 23.0D
                    || player.getX() > anchor.getX() + 24.0D
                    || player.getY() < anchor.getY() + 1.0D
                    || player.getY() > anchor.getY() + 13.0D
                    || player.getZ() < anchor.getZ() - 24.0D
                    || player.getZ() > anchor.getZ() + 24.0D) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPlayerNear(ServerLevel server, double radius) {
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR
                    && player.distanceToSqr(this) <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    private void guideHeader(ServerPlayer player, String bodyKey) {
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.empty()
                .append(Component.translatable("message.finalparadox.koros.guide.speaker").withStyle(
                        Style.EMPTY.withColor(TextColor.fromRgb(0xFBBDFF)).withBold(true).withItalic(true)))
                .append(Component.translatable(bodyKey)));
    }

    private void guideFooter(ServerPlayer player) {
        player.sendSystemMessage(Component.empty());
        player.sendSystemMessage(Component.translatable("message.finalparadox.koros.guide.instruction"));
        playGuideSound(player);
    }

    private Component button(String key, String action) {
        return Component.translatable(key).setStyle(Style.EMPTY.withClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND, "/finalparadox koros_menu " + getUUID() + " " + action)));
    }

    private static Component ability(String ability, boolean dangerous) {
        return ability(ability, ability, dangerous);
    }

    private static Component ability(String ability, String detail, boolean dangerous) {
        Style style = dangerous ? Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)) : Style.EMPTY;
        return Component.translatable("message.finalparadox.koros.guide.ability." + ability).setStyle(
                style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("message.finalparadox.koros.guide.ability." + detail + ".detail"))));
    }

    private static void playGuideSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.GRASS_BREAK, SoundSource.MASTER, 0.5F, 2.0F);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ArenaAnchor")) arenaAnchor = BlockPos.of(tag.getLong("ArenaAnchor"));
        if (tag.contains("GuideMode")) guideMode = tag.getString("GuideMode");
        if (tag.contains("B8IntroStarted")) b8IntroStarted = tag.getBoolean("B8IntroStarted");
        if (tag.contains("B8DialogueStart")) b8DialogueStart = tag.getLong("B8DialogueStart");
        if (tag.contains("B8DialogueStep")) b8DialogueStep = tag.getInt("B8DialogueStep");
        setInvulnerable(true);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("ArenaAnchor", arenaAnchor.asLong());
        tag.putString("GuideMode", guideMode);
        tag.putBoolean("B8IntroStarted", b8IntroStarted);
        tag.putLong("B8DialogueStart", b8DialogueStart);
        tag.putInt("B8DialogueStep", b8DialogueStep);
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
