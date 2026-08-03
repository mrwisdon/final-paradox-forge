package io.github.finalparadox.entity;

import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.B5ArenaStaging;
import io.github.finalparadox.registry.ModEntities;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import javax.annotation.Nullable;
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
        if (!"b8".equals(guideMode)) {
            ArenaDeploymentData data = ArenaDeploymentData.get(server);
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
        if ("b8".equals(guideMode) && b8DialogueStart >= 0L) {
            // Source timings: lines at +0s, +4s, +4s, then dia_ini at +5s.
            long elapsed = server.getGameTime() - b8DialogueStart;
            int step = elapsed >= 260 ? 3 : elapsed >= 160 ? 2 : elapsed >= 80 ? 1 : 0;
            if (step > b8DialogueStep) {
                b8DialogueStep = step;
                sendB8IntroLine(server, step);
            }
            if (elapsed >= 320) b8DialogueStart = -1L;
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
            if (!"main".equals(action)) return 0;
            startB8Intro(player.serverLevel());
            return 1;
        }
        switch (action) {
            case "main" -> showMain(player);
            case "briefing" -> showBriefing(player);
            case "mechanics" -> showMechanics(player);
            case "confirm" -> showConfirmation(player);
            case "start" -> {
                if (!B5ArenaStaging.allPlayersInside(player.serverLevel(), arenaAnchor)) {
                    showPlayersMissing(player);
                    return 0;
                }
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
        ArenaDeploymentData data = ArenaDeploymentData.get(player.serverLevel());
        if ("b8".equals(guideMode)) {
            return data.state() == ArenaDeploymentData.DeploymentState.READY
                    && ArenaDefinitions.B8.id().equals(data.arenaId())
                    && !B8EncounterManager.isActive(player.serverLevel())
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

    /* ------------------------------ B8 pre-fight intro ------------------------------ */

    /**
     * Source b8/matriz/gen_no_boss -> dialogos/new_inicio (three matrix lines)
     * then dialogos/dia_ini (Koros: "talk to me before facing the Megamatrix").
     * Played once when the player talks to the Echo placed after the reset.
     */
    private void startB8Intro(ServerLevel server) {
        if (b8DialogueStart >= 0L) return;
        b8DialogueStart = server.getGameTime();
        b8DialogueStep = 0;
        sendB8IntroLine(server, 0);
    }

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
            player.sendSystemMessage(speaker.copy().append(Component.translatable(messageKey)));
            server.playSound(null, player.blockPosition(), sound, SoundSource.MASTER, 1.0F, pitch);
        }
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
        setInvulnerable(true);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putLong("ArenaAnchor", arenaAnchor.asLong());
        tag.putString("GuideMode", guideMode);
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
