package io.github.finalparadox.entity;

import io.github.finalparadox.ability.NightfallSlashFrames;
import io.github.finalparadox.arena.ArenaDeploymentData;
import io.github.finalparadox.arena.ArenaDefinitions;
import io.github.finalparadox.arena.MarawTharArenaStaging;
import io.github.finalparadox.item.AtacromGauntletItem;
import io.github.finalparadox.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-authoritative body for Final Paradox B9.
 *
 * <p>The source map separates an invisible wither-skeleton hit body, an armor-stand
 * visual body, and a composite sword. This entity keeps the source hit-body attributes
 * while exposing synchronized pose state to one compound client renderer.</p>
 */
public final class MarawTharBossEntity extends WitherSkeleton {
    public static final int ANIMATION_IDLE = 0;
    public static final int ANIMATION_WALK = 1;
    public static final int ANIMATION_HURT = 2;
    public static final int ANIMATION_TRIPLE_SLASH = 3;
    public static final int ANIMATION_LASER_COMBO = 4;
    public static final int ANIMATION_H9_LASER = 5;
    public static final int ANIMATION_H3_PLANT = 6;
    public static final int ANIMATION_H3_SLAM = 7;
    public static final int ANIMATION_H4_JUDGMENT = 8;
    public static final int ANIMATION_H6_TRANSITION = 9;
    public static final int ANIMATION_DASH = 10;
    public static final int ANIMATION_BLUE_RUSH = 11;
    public static final int ANIMATION_H1_COMBO2 = 12;
    public static final int ANIMATION_H1_COMBO3 = 13;
    public static final int ANIMATION_H1_COMBO4 = 14;
    public static final int ANIMATION_VICTORY = 15;
    public static final int THAR_KROO_VISUAL_APPEAR_TICK = 57;
    private static final double ENCOUNTER_PLAYER_MAX_HEALTH = 50.0D;
    private static final int DEFEAT_DIALOGUE_1_TICK = 40;
    private static final int DEFEAT_DIALOGUE_2_TICK = 80;
    private static final int DEFEAT_RESPAWN_TICKS = 100;
    private static final UUID ENCOUNTER_HEALTH_MODIFIER_UUID =
            UUID.fromString("9b66e40d-6ebd-4bc8-97e7-fd47c315c094");
    public static final int THAR_KROO_VISUAL_END_TICK = 950;

    private static final int INTRO_DURATION = 80;
    private static final int ARENA_BOUNDARY_CHECK_INTERVAL = 20;
    private static final double ARENA_BOUNDARY_RADIUS_SQR = 20.0D * 20.0D;
    private static final int ARENA_BARRIER_HEIGHT = 20;
    private static final int HURT_DURATION = 10;
    private static final int COMBAT_WAITING_FOR_TARGET = 0;
    private static final int COMBAT_TRIPLE_SLASH = 1;
    private static final int COMBAT_LASER_COMBO = 2;
    private static final int COMBAT_COUNTER_WINDOW = 3;
    private static final int COMBAT_DASH_COMBO = 5;
    private static final int COMBAT_PHASE_INTERMISSION = 6;
    private static final int COMBAT_BLUE_RUSH = 7;
    private static final int COMBAT_SOURCE_LOCK = 8;
    private static final int COMBAT_ENRAGE_SLASH = 9;
    private static final int COMBAT_H3_PLANT = 10;
    private static final int COMBAT_H3_DARKNESS = 11;
    private static final int COMBAT_H4_JUDGMENT = 12;
    private static final int COMBAT_PHASE_TWO_SWORDS = 13;
    private static final int COMBAT_PHASE_TWO_LASERS = 14;
    private static final int ENRAGE_INTRO_DURATION = 40;
    private static final int ATTACK_TRIPLE_SLASH = 0;
    private static final int ATTACK_DASH_COMBO = 1;
    private static final int ATTACK_LASER_COMBO = 2;
    private static final int[] TRIPLE_SLASH_STARTS = {0, 35, 70, 105, 145, 165};
    private static final double[] TRIPLE_SLASH_ANGLES = {10.0D, -60.0D, 100.0D, 180.0D, 180.0D, 240.0D};
    private static final int[] TRIPLE_SLASH_ATTACK_DURATIONS = {38, 38, 38, 32, 22, 22};
    private static final int[] TRIPLE_SLASH_VISUAL_DURATIONS = {37, 37, 37, 35, 28, 24};
    private static final int[] H1_VARIANT_ATTACK_DURATIONS = {32, 22, 22};
    private static final int[] H1_VARIANT_VISUAL_DURATIONS = {35, 28, 24};
    private static final int[] BLUE_RUSH_STARTS = {0, 40, 80};
    private static final int[] BLUE_RUSH_ATTACK_DURATIONS = {38, 22, 22};
    private static final int[] BLUE_RUSH_VISUAL_DURATIONS = {45, 28, 24};
    private static final double[] BLUE_RUSH_ANGLES = {-20.0D, 70.0D, 120.0D};
    private static final double[] BLUE_RUSH_RADII = {5.0D, 3.0D, 3.0D};
    private static final int TRIPLE_SLASH_DURATION = 200;
    private static final int BLUE_RUSH_DURATION = 120;
    private static final int LASER_COMBO_DURATION = 260;
    private static final int DASH_COMBO_DURATION = 160;
    private static final int MUSIC_NONE = -1;
    private static final int MUSIC_INTRO = 0;
    private static final int MUSIC_INTERMISSION = 1;
    private static final int MUSIC_LOOP = 2;
    private static final int MUSIC_INTRO_TICKS = 2640;
    private static final int MUSIC_INTERMISSION_TICKS = 990;
    private static final int MUSIC_INTERMISSION_OLD_TRACK_STOP_TICK = 34;
    private static final int MUSIC_LOOP_TICKS = 2400;
    private static final int[] H5_DASH_STARTS = {0, 45, 90};
    private static final int H5_WARNING_END_TICK = 18;
    private static final int H5_BACKSTEP_TICK = 25;
    private static final int H5_CHARGE_START_TICK = 33;
    private static final int H5_CHARGE_END_TICK = 44;
    private static final int H5_RESET_TICK = 140;
    private static final double H5_RING_RADIUS = 16.0D;
    private static final double H5_SUBSTEP_DISTANCE = 1.0D;
    private static final int H5_SUBSTEPS_PER_TICK = 4;
    private static final double H5_WARNING_SPACING = 1.5D;
    private static final int H5_WARNING_SAMPLES = 23;
    private static final double H5_PLAYER_HIT_RADIUS_SQR = 1.25D * 1.25D;
    private static final double H5_ECHO_HIT_RADIUS_SQR = 1.0D;
    private static final int COUNTER_WINDOW_DURATION = 50;
    private static final int OPENING_HIT_COOLDOWN_DURATION = 96;
    /**
     * 47.5 seconds before h6/animacion_end plus its 80-tick floor-restore run.
     */
    private static final int PHASE_INTERMISSION_DURATION = 1030;
    private static final int H6_INTRO_SCORE_OFFSET = -2;
    private static final int H6_INTRO_LAST_TICK = 103;
    private static final int H6_FLOOR_BREAK_TICK = 84;
    private static final int H6_SECOND_EXPLOSION_TICK = 89;
    private static final int H6_FIRE_FIRST_TICK = 85;
    private static final int H6_FIRE_END_TICK = 950;
    private static final int H6_RESTORE_30_TICK = 990;
    private static final int H6_RESTORE_66_TICK = 1010;
    private static final int H6_RESTORE_FULL_TICK = 1030;
    private static final double[] H6_FIRE_RADII = {7.0D, 9.0D, 11.0D, 13.0D, 15.0D, 17.0D};
    private static final double H6_FIRE_ROTATION_PER_TICK = 0.3D;
    private static final double H6_OUTER_FIRE_RADIUS = 22.5D;
    private static final int[] H7_PATTERN_START_TICKS = {140, 400, 640, 880};
    private static final int[][] H7_RED_VOLLEY_TICKS = {
            {0, 2, 4, 12, 14, 22, 24, 26, 32, 34},
            {0, 2, 4, 6, 12, 14, 22, 23, 25, 31},
            {12, 14, 16, 18, 20, 22, 24, 26, 32, 34},
            {0, 2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30,
                    32, 34, 36, 38, 40, 42, 44, 46, 48, 50, 52, 54, 56, 58, 60}
    };
    private static final int[][] H7_BLUE_VOLLEY_TICKS = {
            {60}, {57}, {1, 60}, {}
    };
    private static final double[] H7_CURVE_OFFSETS = {
            0.0D,
            0.14235782507653D,
            0.260100688917088D,
            0.332869780703304D,
            0.348082663378896D,
            0.303108891324554D,
            0.205724838302366D,
            0.0727690917862158D,
            -0.0727690917862158D,
            -0.205724838302366D,
            -0.303108891324553D,
            -0.348082663378896D,
            -0.332869780703304D,
            -0.260100688917088D,
            -0.14235782507653D,
            0.0D
    };
    private static final int H7_PROJECTILE_LIFETIME = 60;
    private static final double H7_RED_HIT_RADIUS_SQR = 1.0D;
    private static final double H7_BLUE_HIT_RADIUS = 1.2D;
    private static final double H7_RED_GUARD_HALF_ANGLE = 90.0D;
    private static final double H7_BLUE_TRAVEL_PER_TICK = 0.8D;
    private static final int[] H9_START_TICKS = {220, 460, 700};
    private static final int H9_FIRST_WARNING_TICK = 49;
    private static final int H9_SECOND_WARNING_TICK = 61;
    private static final int H9_RELEASE_TICK = 73;
    private static final int H9_BEAM_DURATION = 80;
    private static final int H9_END_TICK = H9_RELEASE_TICK + H9_BEAM_DURATION;
    private static final int H9_SEGMENTS = 38;
    private static final double H9_SEGMENT_SPACING = 0.6D;
    private static final double H9_ROTATION_PER_TICK = 2.2D;
    private static final double H9_HIT_RADIUS = 1.1D;
    private static final int PUT_SWORD_DURATION = 40;
    private static final int DARKNESS_DURATION = 160;
    private static final int JUDGMENT_DURATION = 120;
    private static final int PHASE_TWO_SWORDS_DURATION = 280;
    private static final int PHASE_TWO_LASERS_DURATION = 220;
    private static final int VICTORY_STATUE_SPAWN_TICK = 36 * 20;
    private static final int VICTORY_PETRIFICATION_START_TICK = 50 * 20;
    private static final int VICTORY_STATUE_FALL_START_TICK = 59 * 20;
    private static final int VICTORY_STATUE_END_TICK = 63 * 20;
    private static final int VICTORY_REWARD_DROP_TICK = 64 * 20;
    private static final int VICTORY_FINISH_TICK = VICTORY_REWARD_DROP_TICK + 2;
    private static final int[] VICTORY_PART_FALL_DELAYS = {
            20, 24, 28, 32, 36, 36, 40, 44, 48, 48, 8, 6, 4, 2, 0
    };
    private static final double[][] VICTORY_PART_OFFSETS = {
            {0.0D, -0.45D, 0.5D}, {0.0D, -0.45D, -0.5D},
            {0.18D, 0.12D, -1.05D}, {0.18D, 0.12D, 1.05D},
            {0.0D, -0.5D, 0.0D}, {0.1D, -0.7D, 0.0D},
            {0.2D, 0.9D, 0.7D}, {0.2D, 0.9D, -0.7D},
            {0.0D, 0.8D, 0.0D}, {-0.45D, 1.3D, 0.0D},
            {-0.6D, 0.35D, -0.025D}, {-0.5D, 1.25D, -0.4D},
            {-0.5D, 1.1D, 0.3D}, {0.1D, 1.45D, -0.25D},
            {0.1D, 1.75D, 0.45D}
    };
    private static final int[] PHASE_TWO_SWORD_SLASH_STARTS = {0, 35, 70};
    private static final double[] PHASE_TWO_SWORD_SLASH_ANGLES = {10.0D, -60.0D, 100.0D};
    private static final int PHASE_TWO_SWORD_VANISH_START = 125;
    private static final int PHASE_TWO_SWORD_FIRST_BARRAGE_START = 145;
    private static final int PHASE_TWO_SWORD_ORBIT_START = 170;
    private static final int PHASE_TWO_SWORD_LAST_BARRAGE_START = 240;
    private static final int[] PHASE_TWO_LASER_SLASH_STARTS = {125, 165, 185};
    private static final double[] PHASE_TWO_LASER_SLASH_ANGLES = {0.0D, 0.0D, 60.0D};
    private static final int ENRAGE_SLASH_DURATION = 40;
    private static final int[] RED_BARRAGE_FIRE_SCORES = {9, 11, 13, 15, 17, 19};
    private static final int[] BLUE_VOLLEY_STARTS = {35, 70};
    private static final int[] H4_METEOR_WARNING_TICKS = {18, 38, 58, 78};
    private static final int LASER_ORBIT_START = 160;
    private static final int[] ORBIT_WARNING_SCORES = {1, 6, 9, 12, 15, 18, 21, 24};
    private static final double LASER_ORBIT_RADIUS = 8.0D;
    private static final Vec3[] LASER_ORBIT_OFFSETS = {
            new Vec3(0.0D, 0.0D, LASER_ORBIT_RADIUS),
            new Vec3(-5.65685424949238D, 0.0D, 5.65685424949238D),
            new Vec3(-LASER_ORBIT_RADIUS, 0.0D, 0.0D),
            new Vec3(-5.65685424949238D, 0.0D, -5.65685424949238D),
            new Vec3(0.0D, 0.0D, -LASER_ORBIT_RADIUS),
            new Vec3(5.65685424949238D, 0.0D, -5.65685424949238D),
            new Vec3(LASER_ORBIT_RADIUS, 0.0D, 0.0D),
            new Vec3(5.65685424949238D, 0.0D, 5.65685424949238D)
    };
    private static final double[] SLASH_PARTICLE_DISTANCES =
            {0.9D, 1.1D, 1.3D, 1.5D, 1.7D, 1.9D, 2.1D, 2.3D, 2.5D, 2.7D, 2.9D};
    private static final double[] H1_VARIANT_PARTICLE_DISTANCES =
            {1.4D, 1.6D, 1.8D, 2.0D, 2.2D, 2.4D, 2.6D, 2.8D, 3.0D, 3.2D, 3.4D};
    private static final float RED_ATTACK_DAMAGE = 18.0F;
    private static final float BLUE_ATTACK_DAMAGE = 40.0F;
    private static final DustParticleOptions RED_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.02F), 1.25F);
    private static final DustParticleOptions H9_WARNING_RED_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 2.0F);
    private static final DustParticleOptions H4_TRAIL_RED_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F);
    private static final DustParticleOptions H3_WHITE_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 1.4F);
    private static final DustParticleOptions H3_BLACK_DUST =
            new DustParticleOptions(new Vector3f(0.0F, 0.0F, 0.0F), 3.0F);
    private static final DustParticleOptions H7_BLACK_DUST =
            new DustParticleOptions(new Vector3f(0.0F, 0.0F, 0.0F), 1.2F);
    private static final DustParticleOptions H7_RED_DUST =
            new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 0.6F);
    private static final DustParticleOptions H7_BLUE_DUST =
            new DustParticleOptions(new Vector3f(0.0F, 0.682F, 1.0F), 1.7F);
    private static final DustParticleOptions BLUE_RUSH_DUST =
            new DustParticleOptions(new Vector3f(0.0F, 0.882F, 1.0F), 0.7F);
    private static final DustParticleOptions[] H5_TRAIL_DUST = {
            dust(0.0F, 0.0F, 0.0F, 0.72F),
            dust(0.18F, 0.0F, 0.0F, 0.72F),
            dust(0.42F, 0.0F, 0.0F, 0.72F),
            dust(0.72F, 0.0F, 0.0F, 0.72F),
            dust(1.0F, 0.0F, 0.0F, 0.72F)
    };
    /**
     * Radius and height pairs copied from b4/h3/espiral/1..35, which B9 h3 calls.
     */
    private static final double[][] H3_SPIRAL = {
            {14.8893285768853D, 0.00761422734513761D},
            {14.2798196015328D, 0.0304424152660356D},
            {13.6726333089193D, 0.0684411090095169D},
            {13.068925512663D, 0.121537975902336D},
            {12.4698454048666D, 0.189631943040533D},
            {11.8765333685647D, 0.272593389687454D},
            {11.2901188069402D, 0.370264394014185D},
            {10.7117179934406D, 0.482459033712733D},
            {10.1424319468887D, 0.608963739909706D},
            {9.58334433563021D, 0.749537703706801D},
            {9.03551941470953D, 0.903913334574226D},
            {8.5D, 1.07179676972449D},
            {7.97780548314447D, 1.25286843349691D},
            {7.46992989108536D, 1.44678364568807D},
            {6.97733999387791D, 1.65317327767012D},
            {6.50097346438845D, 1.87164445504818D},
            {6.04173709338076D, 2.10178130551901D},
            {5.60050506338834D, 2.34314575050762D},
            {5.17811728465826D, 2.59527833907472D},
            {4.77537779633431D, 2.85769912250769D},
            {4.39305323592271D, 3.12990856793024D},
            {4.03187137995412D, 3.41138850919163D},
            {3.6925197586196D, 3.70160313322541D},
            {3.37564434701786D, 4.0D},
            {3.0818483355049D, 4.30601109411973D},
            {2.8116909814869D, 4.6190539060744D},
            {2.56568654484199D, 4.93853254107928D},
            {2.34430330899728D, 5.26383885339465D},
            {2.14796268952482D, 5.59435360396582D},
            {1.97703843195304D, 5.92944763917983D},
            {1.83185590032093D, 6.26848308849518D},
            {1.71269145782909D, 6.61081457866456D},
            {1.61977194076665D, 6.95579046223959D},
            {1.55327422671556D, 7.30275405801874D},
            {1.51332489785399D, 7.65104490107731D}
    };
    private static final DustParticleOptions[] SLASH_DUST = {
            dust(0.0F, 0.0F, 0.0F, 0.20F), dust(0.0F, 0.0F, 0.0F, 0.25F),
            dust(0.0F, 0.0F, 0.0F, 0.30F), dust(0.0F, 0.0F, 0.0F, 0.35F),
            dust(0.0F, 0.0F, 0.0F, 0.40F), dust(0.0F, 0.0F, 0.0F, 0.45F),
            dust(0.173F, 0.129F, 0.129F, 0.50F),
            dust(0.267F, 0.169F, 0.169F, 0.55F),
            dust(0.478F, 0.188F, 0.188F, 0.60F),
            dust(0.694F, 0.157F, 0.157F, 0.65F),
            dust(1.0F, 0.0F, 0.0F, 0.70F)
    };
    private static final DustParticleOptions[] ENRAGE_SLASH_DUST = {
            dust(0.0F, 0.0F, 0.0F, 0.20F), dust(0.0F, 0.0F, 0.0F, 0.25F),
            dust(0.0F, 0.0F, 0.0F, 0.30F), dust(0.0F, 0.0F, 0.0F, 0.35F),
            dust(0.0F, 0.0F, 0.0F, 0.40F), dust(0.0F, 0.0F, 0.0F, 0.45F),
            dust(0.129F, 0.169F, 0.173F, 0.50F),
            dust(0.169F, 0.267F, 0.267F, 0.55F),
            dust(0.188F, 0.467F, 0.478F, 0.60F),
            dust(0.157F, 0.675F, 0.694F, 0.65F),
            dust(0.0F, 0.933F, 1.0F, 0.70F)
    };
    private static final String HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDYzZTQzYjdlODgzNjA3OWE2MWRkMzZlZjg3ZjVlZDY3NTkzOGJkMzU4NzEwYWU0MzMwYzU0MTc5YTJlZWFjNCJ9fX0=";
    private static final String H5_ECHO_HEAD_TEXTURE =
            "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjIxYTk5Y2JjYzFiYzhkMzMxNzY3NWRiMGU5OGJjMjBhODJhNzA5YWM5NzM2YWE3ZGU1YzQzZGMyOWFhZWQzNCJ9fX0=";

    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHOREOGRAPHY_STEP =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ANIMATION_TICK =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VULNERABLE =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_ENRAGED =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_TRANSITION_VISUAL_TICK =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_VICTORY_ACTIVE =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_VICTORY_TICK =
            SynchedEntityData.defineId(MarawTharBossEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.finalparadox.marawthar.bossbar"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS);

    private boolean initialized;
    private int introTick;
    private int hurtAnimationTicks;
    private int combatState = COMBAT_WAITING_FOR_TARGET;
    private int combatTick;
    private int randomBagIndex;
    private int[] randomAttackBag = {ATTACK_TRIPLE_SLASH, ATTACK_DASH_COMBO, ATTACK_LASER_COMBO};
    private int musicState = MUSIC_NONE;
    private int musicTick = -1;
    private boolean needsMusicRecovery;
    private boolean openingWasHit;
    private int openingEndTick = COUNTER_WINDOW_DURATION;
    private boolean phaseTransitionOpening;
    private MarawTharChoreography.Action sourceLockAction = MarawTharChoreography.Action.NONE;
    private int sourceLockDuration;
    private boolean orphanCleanupDone;
    private Vec3 encounterCenter = Vec3.ZERO;
    private Vec3 h3BladePosition = Vec3.ZERO;
    private Vec3 h3ChasePosition = Vec3.ZERO;
    @Nullable
    private UUID h3BladeUuid;
    @Nullable
    private UUID h3TargetUuid;
    @Nullable
    private UUID lockedTarget;
    private int h9StartTick = -1;
    private int h9SpinDirection;
    private Vec3 h9LockedDirection = Vec3.ZERO;
    @Nullable
    private UUID h9TargetUuid;
    @Nullable
    private UUID h9LaserUuid;
    private final Set<UUID> h9HitPlayers = new HashSet<>();
    private Vec3 h5DashDirection = Vec3.ZERO;
    private final List<UUID> h5EchoUuids = new ArrayList<>();
    private final Set<UUID> h5HitPlayers = new HashSet<>();
    private int blueRushTrailAge = -1;
    private Vec3 blueRushTrailPosition = Vec3.ZERO;
    private Vec3 blueRushTrailDirection = Vec3.ZERO;
    private final Set<UUID> blueRushTrailHitPlayers = new HashSet<>();
    private final float[] blueVolleyLockedYaws = new float[BLUE_VOLLEY_STARTS.length];
    private final boolean[] blueVolleyYawLocked = new boolean[BLUE_VOLLEY_STARTS.length];
    private final Set<UUID> musicListeners = new HashSet<>();
    private final Set<UUID> encounterHealthPlayers = new HashSet<>();
    private final Set<BlockPos> arenaBarrierBlocks = new HashSet<>();
    private final List<H7Projectile> h7Projectiles = new ArrayList<>();
    private final Map<BlockPos, BlockState> h6AlteredBlocks = new HashMap<>();
    private final List<SmallLaserProjectile> smallLaserProjectiles = new ArrayList<>();
    private final List<H4Meteor> h4Meteors = new ArrayList<>();
    private final Map<UUID, Integer> projectileHitCooldowns = new HashMap<>();
    private final List<Set<UUID>> slashHitTargets = Arrays.asList(
            new HashSet<>(), new HashSet<>(), new HashSet<>());
    private boolean arenaClosed;
    private boolean victoryRewardSpawned;
    private boolean defeatActive;
    private int defeatTicks;

    public MarawTharBossEntity(EntityType<? extends WitherSkeleton> type, Level level) {
        super(type, level);
        xpReward = 100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractSkeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 628.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.ATTACK_DAMAGE, 0.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.20D)
                .add(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_PHASE, 0);
        entityData.define(DATA_CHOREOGRAPHY_STEP, 0);
        entityData.define(DATA_ANIMATION, ANIMATION_IDLE);
        entityData.define(DATA_ANIMATION_TICK, 0);
        entityData.define(DATA_VULNERABLE, false);
        entityData.define(DATA_ENRAGED, false);
        entityData.define(DATA_TRANSITION_VISUAL_TICK, -1);
        entityData.define(DATA_VICTORY_ACTIVE, false);
        entityData.define(DATA_VICTORY_TICK, 0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                         MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                         @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        initializeEncounter();
        return result;
    }

    private void initializeEncounter() {
        if (initialized) {
            return;
        }
        initialized = true;
        introTick = 0;
        setPersistenceRequired();
        setCanPickUpLoot(false);
        addTag("boss");
        addTag("final_boss");
        addTag("oscuro_boss");
        addTag("hostile");
        setCustomName(Component.translatable("entity.finalparadox.marawthar"));
        setCustomNameVisible(true);
        equipSourceAppearance();
        setHealth(getMaxHealth());
        setNoAi(true);
        setInvulnerable(true);
        setInvisible(true);
        setVulnerable(false);
        setPhase(0);
        setChoreographyStep(0);
        setEnraged(false);
        combatState = COMBAT_WAITING_FOR_TARGET;
        combatTick = 0;
        randomBagIndex = 0;
        openingWasHit = false;
        openingEndTick = COUNTER_WINDOW_DURATION;
        phaseTransitionOpening = false;
        sourceLockAction = MarawTharChoreography.Action.NONE;
        sourceLockDuration = 0;
        lockedTarget = null;
        encounterCenter = position();
        h3BladePosition = Vec3.ZERO;
        h3ChasePosition = Vec3.ZERO;
        h3BladeUuid = null;
        h3TargetUuid = null;
        h9StartTick = -1;
        h9SpinDirection = 0;
        h9LockedDirection = Vec3.ZERO;
        h9TargetUuid = null;
        h9LaserUuid = null;
        h9HitPlayers.clear();
        h5DashDirection = Vec3.ZERO;
        h5EchoUuids.clear();
        h5HitPlayers.clear();
        blueRushTrailAge = -1;
        blueRushTrailPosition = Vec3.ZERO;
        blueRushTrailDirection = Vec3.ZERO;
        blueRushTrailHitPlayers.clear();
        Arrays.fill(blueVolleyLockedYaws, 0.0F);
        Arrays.fill(blueVolleyYawLocked, false);
        musicListeners.clear();
        encounterHealthPlayers.clear();
        arenaClosed = false;
        arenaBarrierBlocks.clear();
        setVictoryActive(false);
        setVictoryTick(0);
        victoryRewardSpawned = false;
        defeatActive = false;
        defeatTicks = 0;
        bossEvent.setVisible(true);
        applySourceResistance();
        if (level() instanceof ServerLevel server) {
            startIntroMusic(server);
            maintainEncounterPlayerHealth(server);
        }
    }

    private void equipSourceAppearance() {
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        setItemSlot(EquipmentSlot.FEET, dyed(new ItemStack(Items.LEATHER_BOOTS), 13697024));
        setItemSlot(EquipmentSlot.LEGS, dyed(new ItemStack(Items.LEATHER_LEGGINGS), 2236962));
        setItemSlot(EquipmentSlot.CHEST, dyed(new ItemStack(Items.LEATHER_CHESTPLATE), 2236962));
        setItemSlot(EquipmentSlot.HEAD, sourceHead());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            setDropChance(slot, 0.0F);
        }
        reassessWeaponGoal();
    }

    private static ItemStack dyed(ItemStack stack, int color) {
        if (stack.getItem() instanceof DyeableLeatherItem leather) {
            leather.setColor(stack, color);
        }
        return stack;
    }

    private static ItemStack sourceHead() {
        return texturedHead(
                UUID.fromString("94bb193e-5e99-49a4-9bf7-ef50fafc9120"), HEAD_TEXTURE);
    }

    private static ItemStack texturedHead(UUID profileId, String texture) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        CompoundTag owner = new CompoundTag();
        owner.putUUID("Id", profileId);
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag value = new CompoundTag();
        value.putString("Value", texture);
        textures.add(value);
        properties.put("textures", textures);
        owner.put("Properties", properties);
        stack.getOrCreateTag().put("SkullOwner", owner);
        return stack;
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server) || isRemoved() || isDeadOrDying()) {
            return;
        }
        if (!initialized) {
            initializeEncounter();
        }
        if (isVictoryActive()) {
            tickVictoryCinematic(server);
            return;
        }
        if (defeatActive) {
            tickDefeat(server);
            return;
        }
        if (!orphanCleanupDone) {
            if (combatState != COMBAT_DASH_COMBO && !h5EchoUuids.isEmpty()) {
                discardH5Echoes(server);
            }
            cleanupOwnedOrphanedLaserParts(server);
            orphanCleanupDone = true;
        }
        if (!arenaClosed) {
            closeArena(server);
        }
        if (needsMusicRecovery) {
            recoverMusic(server);
        } else {
            tickMusic(server);
        }
        if (tickCount % ARENA_BOUNDARY_CHECK_INTERVAL == 0) {
            returnOutOfBoundsPlayers(server);
        }

        bossEvent.setName(Component.translatable("entity.finalparadox.marawthar.bossbar"));
        bossEvent.setProgress(Mth.clamp(getHealth() / getMaxHealth(), 0.0F, 1.0F));
        tickH3BladeAura(server);

        if (tickCount % 20 == 0) {
            maintainEncounterPlayerHealth(server);
            applySourceResistance();
            for (ServerPlayer player : server.players()) {
                if (!player.isSpectator() && player.isAlive()
                        && distanceToSqr(player) <= 48.0D * 48.0D) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST, 40, 3, false, false));
                }
            }
        }

        if (getPhase() == 0) {
            introTick++;
            setAnimation(ANIMATION_IDLE, introTick);
            if (introTick == 1) {
                setInvisible(true);
                protectPlayersDuringCountdown(server);
            } else if (introTick == 20) {
                showEncounterTitle(
                        Component.literal("3").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD),
                        Component.empty(), 10, 40, 10);
                playCountdownBell(server);
            } else if (introTick == 40) {
                showEncounterTitle(
                        Component.literal("2").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD),
                        Component.empty(), 10, 40, 10);
                playCountdownBell(server);
            } else if (introTick == 60) {
                showEncounterTitle(
                        Component.literal("1").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        Component.empty(), 10, 40, 10);
                playCountdownBell(server);
            }
            if (introTick >= INTRO_DURATION) {
                setInvisible(false);
                setPhase(1);
                setNoAi(false);
                setInvulnerable(false);
                setVulnerable(true);
                setAnimation(ANIMATION_IDLE, 0);
                setChoreographyStep(0);
                resetRandomAttackBag(server);
                ServerPlayer target = chooseTarget(server);
                if (target != null) {
                    beginChoreographyAction(target);
                } else {
                    beginChoreographyWait();
                }
                combatTick = 0;
                showEncounterTitle(
                        Component.literal("☠").withStyle(ChatFormatting.DARK_RED),
                        Component.empty(), 5, 20, 5);
                server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                        SoundSource.MASTER, 1.0F, 1.5F);
                server.playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(),
                        SoundSource.MASTER, 1.0F, 1.5F);
                server.sendParticles(ParticleTypes.SQUID_INK,
                        getX(), getY() + 1.0D, getZ(),
                        18, 0.35D, 0.7D, 0.35D, 0.03D);
            }
            return;
        }

        tickCombat(server);
    }

    private void tickCombat(ServerLevel server) {
        tickSmallLaserProjectiles(server);
        combatTick++;
        switch (combatState) {
            case COMBAT_TRIPLE_SLASH -> tickTripleSlash(server);
            case COMBAT_LASER_COMBO -> tickLaserCombo(server);
            case COMBAT_COUNTER_WINDOW -> tickCounterWindow(server);
            case COMBAT_DASH_COMBO -> tickDashCombo(server);
            case COMBAT_PHASE_INTERMISSION -> tickPhaseIntermission(server);
            case COMBAT_BLUE_RUSH -> tickBlueRush(server);
            case COMBAT_SOURCE_LOCK -> tickSourceLock(server);
            case COMBAT_ENRAGE_SLASH -> tickEnrageSlash(server);
            case COMBAT_H3_PLANT -> tickH3Plant(server);
            case COMBAT_H3_DARKNESS -> tickH3Darkness(server);
            case COMBAT_H4_JUDGMENT -> tickH4Judgment(server);
            case COMBAT_PHASE_TWO_SWORDS -> tickPhaseTwoSwords(server);
            case COMBAT_PHASE_TWO_LASERS -> tickPhaseTwoLasers(server);
            default -> tickWaitingForTarget(server);
        }
    }

    private void tickWaitingForTarget(ServerLevel server) {
        tickNaturalPose();
        if (tickCount % 20 != 0) {
            return;
        }
        ServerPlayer target = chooseTarget(server);
        if (target != null) {
            setTarget(target);
            beginChoreographyAction(target);
        }
    }

    private void tickTripleSlash(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            beginChoreographyWait();
            return;
        }

        int elapsed = combatTick - 1;
        int sequenceIndex = latestStartedSequence(TRIPLE_SLASH_STARTS, elapsed);
        int localScore = elapsed - TRIPLE_SLASH_STARTS[sequenceIndex] + 1;
        if (elapsed == TRIPLE_SLASH_STARTS[sequenceIndex]) {
            teleportAround(target, 3.0D, TRIPLE_SLASH_ANGLES[sequenceIndex]);
            clearSlashHits();
        } else {
            lookAt(target);
        }

        int animation = sequenceIndex < 3
                ? ANIMATION_TRIPLE_SLASH
                : h1VariantAnimation(sequenceIndex - 3);
        setSwordComboVisual(
                animation, localScore, TRIPLE_SLASH_VISUAL_DURATIONS[sequenceIndex]);

        boolean attackActive =
                localScore <= TRIPLE_SLASH_ATTACK_DURATIONS[sequenceIndex];
        if (attackActive && sequenceIndex < 3) {
            if (localScore == 8 || localScore == 18 || localScore == 26) {
                stepTowardTarget(target, 1.0D);
            }
            if (localScore == 9 || localScore == 19 || localScore == 29) {
                server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.HOSTILE, 1.5F, 0.7F);
            }
            int frameIndex = comboFrameIndex(localScore);
            if (frameIndex >= 0) {
                drawAndDamageSlashFrame(server, frameIndex, frameIndex / 3);
            }
        } else if (attackActive) {
            tickH1VariantCombo(server, sequenceIndex - 3, localScore);
        }
        if (combatTick >= TRIPLE_SLASH_DURATION) {
            finishChoreographyAction(server);
        }
    }

    private void tickLaserCombo(ServerLevel server) {
        setAnimation(ANIMATION_LASER_COMBO, combatTick);
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            beginChoreographyWait();
            return;
        }
        int elapsed = combatTick - 1;
        if (elapsed == 0) teleportAround(target, 10.0D, -30.0D);
        if (elapsed == 35) teleportAround(target, 8.0D, -20.0D);
        if (elapsed == 70) teleportAround(target, 8.0D, 30.0D);
        if (elapsed == 105) teleportAround(target, 10.0D, 30.0D);
        if (elapsed == 125) teleportAround(target, 10.0D, -10.0D);
        if (elapsed == 230) teleportAround(target, 10.0D, 20.0D);
        lookAt(target);

        tickRedBarrage(server, target, elapsed, 0);
        tickBlueVolley(server, target, elapsed, BLUE_VOLLEY_STARTS[0], 0);
        tickBlueVolley(server, target, elapsed, BLUE_VOLLEY_STARTS[1], 1);
        tickRedBarrage(server, target, elapsed, 125);
        tickLaserOrbit(server, target, elapsed - LASER_ORBIT_START);
        tickRedBarrage(server, target, elapsed, 230);

        // h2/combo4 is an intentional ten-tick vanish/reposition beat.
        if (elapsed >= 105 && elapsed < 115) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    getX(), getY() + 1.0D, getZ(), 2, 0.2D, 0.4D, 0.2D, 0.02D);
        }
        if (combatTick >= LASER_COMBO_DURATION) {
            finishChoreographyAction(server);
        }
    }

    private void tickPhaseTwoSwords(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            beginChoreographyWait();
            return;
        }

        int elapsed = combatTick - 1;
        if (elapsed < PHASE_TWO_SWORD_VANISH_START) {
            int sequenceIndex = latestStartedSequence(PHASE_TWO_SWORD_SLASH_STARTS, elapsed);
            int localScore = elapsed - PHASE_TWO_SWORD_SLASH_STARTS[sequenceIndex] + 1;
            if (elapsed == PHASE_TWO_SWORD_SLASH_STARTS[sequenceIndex]) {
                teleportAround(target, 3.0D, PHASE_TWO_SWORD_SLASH_ANGLES[sequenceIndex]);
                clearSlashHits();
            } else {
                lookAt(target);
            }
            setAnimation(ANIMATION_TRIPLE_SLASH, Mth.clamp(localScore, 1, 38));
            if (localScore == 8 || localScore == 18 || localScore == 26) {
                stepTowardTarget(target, 1.0D);
            }
            if (localScore == 9 || localScore == 19 || localScore == 29) {
                server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.HOSTILE, 1.5F, 0.7F);
            }
            int frameIndex = comboFrameIndex(localScore);
            if (frameIndex >= 0) {
                drawAndDamageSlashFrame(server, frameIndex, frameIndex / 3);
            }
        } else {
            setAnimation(ANIMATION_LASER_COMBO, elapsed - PHASE_TWO_SWORD_VANISH_START + 1);
            lookAt(target);
        }

        if (elapsed == PHASE_TWO_SWORD_VANISH_START) {
            teleportAround(target, 10.0D, 30.0D);
        }
        if (elapsed >= PHASE_TWO_SWORD_VANISH_START
                && elapsed < PHASE_TWO_SWORD_VANISH_START + 10) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    getX(), getY() + 1.0D, getZ(),
                    2, 0.2D, 0.4D, 0.2D, 0.02D);
        }
        if (elapsed == PHASE_TWO_SWORD_FIRST_BARRAGE_START) {
            teleportAround(target, 10.0D, -10.0D);
        }
        if (elapsed == PHASE_TWO_SWORD_LAST_BARRAGE_START) {
            teleportAround(target, 10.0D, 20.0D);
        }

        tickRedBarrage(server, target, elapsed, PHASE_TWO_SWORD_FIRST_BARRAGE_START);
        tickLaserOrbit(server, target, elapsed - PHASE_TWO_SWORD_ORBIT_START);
        tickRedBarrage(server, target, elapsed, PHASE_TWO_SWORD_LAST_BARRAGE_START);

        if (combatTick >= PHASE_TWO_SWORDS_DURATION) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            finishChoreographyAction(server);
        }
    }

    private void tickPhaseTwoLasers(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            beginChoreographyWait();
            return;
        }

        int elapsed = combatTick - 1;
        if (elapsed < PHASE_TWO_LASER_SLASH_STARTS[0]) {
            setAnimation(ANIMATION_LASER_COMBO, combatTick);
            if (elapsed == 0) {
                teleportAround(target, 10.0D, -30.0D);
            } else if (elapsed == 35) {
                teleportAround(target, 8.0D, -20.0D);
                ServerPlayer fixedTarget = chooseTarget(server);
                if (fixedTarget != null) {
                    lookAt(fixedTarget);
                }
            } else if (elapsed == 70) {
                teleportAround(target, 8.0D, 30.0D);
            } else if (elapsed < 35) {
                lookAt(target);
            }
            tickRedBarrage(server, target, elapsed, 0);
            tickBlueVolleyAtYaw(server, elapsed, 35, getYRot());
            tickBlueVolleyAtYaw(server, elapsed, 70, getYRot());
        } else {
            int sequenceIndex = latestStartedSequence(PHASE_TWO_LASER_SLASH_STARTS, elapsed);
            int localScore = elapsed - PHASE_TWO_LASER_SLASH_STARTS[sequenceIndex] + 1;
            if (elapsed == PHASE_TWO_LASER_SLASH_STARTS[sequenceIndex]) {
                teleportAround(target, 3.0D, PHASE_TWO_LASER_SLASH_ANGLES[sequenceIndex]);
                clearSlashHits();
            }
            int animation = h1VariantAnimation(sequenceIndex);
            setSwordComboVisual(
                    animation, localScore, H1_VARIANT_VISUAL_DURATIONS[sequenceIndex]);
            if (localScore <= H1_VARIANT_ATTACK_DURATIONS[sequenceIndex]) {
                tickH1VariantCombo(server, sequenceIndex, localScore);
            }
        }

        if (combatTick >= PHASE_TWO_LASERS_DURATION) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            finishChoreographyAction(server);
        }
    }

    private void tickH1VariantCombo(ServerLevel server, int sequenceIndex, int localScore) {
        if ((sequenceIndex == 0 && (localScore == 8 || localScore == 20))
                || (sequenceIndex == 1 && localScore == 12)
                || (sequenceIndex == 2 && localScore == 6)) {
            stepForward(1.0D);
        }

        if (sequenceIndex == 0 && (localScore == 9 || localScore == 19 || localScore == 22)) {
            float pitch = localScore == 9 ? 1.0F : localScore == 19 ? 0.8F : 1.5F;
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.MASTER, 1.5F, 0.6F);
            server.playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT,
                    SoundSource.MASTER, 0.3F, pitch);
        } else if ((sequenceIndex == 1 && localScore == 12)
                || (sequenceIndex == 2 && localScore == 6)) {
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.MASTER, 1.5F, 0.6F);
        }

        if (sequenceIndex == 0) {
            if (localScore == 14) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[0], 0);
            } else if (localScore == 15) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[1], 0);
            } else if (localScore == 25) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[2], 1);
            } else if (localScore == 26) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[3], 1);
            } else if (localScore == 27) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[4], 1);
            } else if (localScore == 28) {
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[2], 1);
                drawAndDamageH1VariantFrame(server, MarawTharH1VariantFrames.COMBO2[3], 1);
            }
        } else if (sequenceIndex == 1 && (localScore == 15 || localScore == 16)) {
            drawAndDamageH1VariantFrame(
                    server, MarawTharH1VariantFrames.COMBO4[localScore - 15], 0);
        } else if (sequenceIndex == 2 && (localScore == 11 || localScore == 12)) {
            drawAndDamageH1VariantFrame(
                    server, MarawTharH1VariantFrames.COMBO3[localScore - 11], 0);
        }
    }

    private static int h1VariantAnimation(int sequenceIndex) {
        return switch (sequenceIndex) {
            case 0 -> ANIMATION_H1_COMBO2;
            case 1 -> ANIMATION_H1_COMBO4;
            default -> ANIMATION_H1_COMBO3;
        };
    }

    private void setSwordComboVisual(int animation, int localScore, int visualDuration) {
        if (localScore <= visualDuration) {
            setInvisible(false);
            setAnimation(animation, localScore);
            return;
        }
        setInvisible(true);
        setAnimation(ANIMATION_IDLE, 0);
    }

    private void tickCounterWindow(ServerLevel server) {
        tickNaturalPose();
        ServerPlayer target = chooseTarget(server);
        if (target != null) {
            setTarget(target);
        }
        if (combatTick >= openingEndTick) {
            boolean startFixedBlueRush = !phaseTransitionOpening;
            phaseTransitionOpening = false;
            if (startFixedBlueRush && target != null) {
                // A successful hit moves the source step back onto BEGONE. Consume
                // it when present, but always chain Blue Rush after a normal
                // counter window so a blocked or missed hit cannot hide the move.
                if (MarawTharChoreography.actionFor(getPhase(), getChoreographyStep())
                        == MarawTharChoreography.Action.BEGONE) {
                    setChoreographyStep(getChoreographyStep()
                            + MarawTharChoreography.advanceAtStart(
                            MarawTharChoreography.Action.BEGONE));
                }
                beginBlueRush(target);
                return;
            }
            finishChoreographyAction(server);
        }
    }

    private void tickDashCombo(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            discardH5Echoes(server);
            beginChoreographyWait();
            return;
        }
        int elapsed = combatTick - 1;
        int sequenceIndex = latestStartedSequence(H5_DASH_STARTS, elapsed);
        int localTick = elapsed - H5_DASH_STARTS[sequenceIndex];
        if (elapsed < H5_RESET_TICK && localTick <= H5_CHARGE_END_TICK) {
            setAnimation(ANIMATION_DASH, localTick);
        } else {
            setAnimation(ANIMATION_IDLE, 0);
        }

        if (localTick == 0 && sequenceIndex < H5_DASH_STARTS.length) {
            if (sequenceIndex > 0) {
                spawnH5Echo(server, target);
            }
            teleportToH5Ring(server);
            h5DashDirection = Vec3.ZERO;
            h5HitPlayers.clear();
            orientH5Toward(target);
        } else if (localTick >= 1 && localTick <= H5_WARNING_END_TICK) {
            Vec3 warningDirection = horizontalDirection(position(), target.position(), getYRot());
            orientH5(warningDirection);
            drawH5Warnings(server, warningDirection, sequenceIndex + 1);
            if (localTick == H5_WARNING_END_TICK) {
                // The source emits one final stationary copy when the aim locks.
                drawH5Warnings(server, warningDirection, sequenceIndex + 1);
                h5DashDirection = warningDirection;
            }
        } else if (localTick > H5_WARNING_END_TICK
                && localTick < H5_CHARGE_START_TICK) {
            ensureH5Direction(target);
            orientH5(h5DashDirection);
            if (localTick == 20) {
                server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                        SoundSource.HOSTILE, 1.0F, 0.6F);
            }
            if (localTick == H5_BACKSTEP_TICK) {
                moveH5(h5DashDirection.scale(-0.5D));
            }
            if (localTick == 32) {
                server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                        SoundSource.HOSTILE, 1.0F, 0.8F);
            }
        } else if (localTick >= H5_CHARGE_START_TICK
                && localTick <= H5_CHARGE_END_TICK) {
            ensureH5Direction(target);
            orientH5(h5DashDirection);
            if (localTick == H5_CHARGE_START_TICK) {
                server.playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT,
                        SoundSource.HOSTILE, 1.25F, 0.8F);
            }
            for (int substep = 0; substep < H5_SUBSTEPS_PER_TICK; substep++) {
                moveH5(h5DashDirection.scale(H5_SUBSTEP_DISTANCE));
                hitH5Lanes(server, target, h5DashDirection, sequenceIndex + 1);
                drawH5ChargeTrail(server, h5DashDirection, sequenceIndex + 1);
            }
            if (localTick == H5_CHARGE_END_TICK) {
                drawH5TeleportBurst(server, position());
            }
        }

        if (elapsed == H5_RESET_TICK) {
            discardH5Echoes(server);
            h5DashDirection = Vec3.ZERO;
            h5HitPlayers.clear();
        }
        if (combatTick >= DASH_COMBO_DURATION) {
            discardH5Echoes(server);
            finishChoreographyAction(server);
        }
    }

    private void teleportToH5Ring(ServerLevel server) {
        List<Vec3> candidates = new ArrayList<>(13);
        double shift = getRandom().nextBoolean() ? 5.0D : 0.0D;
        for (int angle = 0; angle <= 360; angle += 30) {
            Vec3 offset = Vec3.directionFromRotation(0.0F, (float) (angle + shift))
                    .scale(H5_RING_RADIUS);
            candidates.add(new Vec3(
                    encounterCenter.x + offset.x,
                    encounterCenter.y,
                    encounterCenter.z + offset.z));
        }
        candidates.sort((first, second) -> Double.compare(
                first.distanceToSqr(position()), second.distanceToSqr(position())));
        Vec3 destination = candidates.get(Math.min(1, candidates.size() - 1));
        Vec3 origin = position();
        Vec3 delta = destination.subtract(origin);
        double distance = delta.length();
        if (distance > 1.0E-6D) {
            Vec3 direction = delta.scale(1.0D / distance);
            for (double step = 0.0D; step <= distance; step += 0.6D) {
                Vec3 point = origin.add(direction.scale(step));
                server.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y + 1.0D, point.z,
                        1, 0.04D, 0.08D, 0.04D, 0.0D);
                server.sendParticles(RED_DUST,
                        point.x, point.y + 1.0D, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        moveTo(destination.x, destination.y, destination.z, getYRot(), 0.0F);
        setDeltaMovement(Vec3.ZERO);
        drawH5TeleportBurst(server, destination);
        server.playSound(null, blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.HOSTILE, 1.0F, 0.7F);
    }

    private void orientH5Toward(ServerPlayer target) {
        orientH5(horizontalDirection(position(), target.position(), getYRot()));
    }

    private void orientH5(Vec3 direction) {
        float targetYaw = yawFromDirection(direction);
        float bodyYaw = targetYaw + 33.0F;
        setYRot(bodyYaw);
        yBodyRot = bodyYaw;
        yHeadRot = targetYaw;
        setXRot(0.0F);
    }

    private void ensureH5Direction(ServerPlayer target) {
        if (h5DashDirection.lengthSqr() < 1.0E-6D) {
            h5DashDirection = horizontalDirection(position(), target.position(), getYRot());
        }
    }

    private void drawH5Warnings(ServerLevel server, Vec3 direction, int level) {
        Vec3 lateral = new Vec3(direction.z, 0.0D, -direction.x);
        for (double lane : h5LaneOffsets(level)) {
            Vec3 laneOrigin = position().add(lateral.scale(lane));
            for (int sample = 1; sample <= H5_WARNING_SAMPLES; sample++) {
                Vec3 point = laneOrigin.add(direction.scale(sample * H5_WARNING_SPACING));
                server.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y + 0.12D, point.z,
                        1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
        }
    }

    private static double[] h5LaneOffsets(int level) {
        if (level >= 3) {
            return new double[]{0.0D, -4.0D, 4.0D, -8.0D, 8.0D};
        }
        if (level == 2) {
            return new double[]{0.0D, -4.0D, 4.0D};
        }
        return new double[]{0.0D};
    }

    private void moveH5(Vec3 movement) {
        moveTo(
                getX() + movement.x,
                getY(),
                getZ() + movement.z,
                getYRot(),
                getXRot());
        setDeltaMovement(Vec3.ZERO);
    }

    private void hitH5Lanes(ServerLevel server, ServerPlayer lockedPlayer,
                            Vec3 direction, int level) {
        Vec3 lateral = new Vec3(direction.z, 0.0D, -direction.x);
        for (double lane : h5LaneOffsets(level)) {
            Vec3 sample = position().add(direction).add(lateral.scale(lane));
            AABB search = new AABB(sample, sample).inflate(1.25D);
            for (ServerPlayer player : server.getEntitiesOfClass(
                    ServerPlayer.class, search,
                    candidate -> candidate.isAlive() && !candidate.isSpectator()
                            && candidate.position().distanceToSqr(sample)
                            <= H5_PLAYER_HIT_RADIUS_SQR)) {
                if (h5HitPlayers.add(player.getUUID())
                        && damageRedPlayer(player, sample, RED_ATTACK_DAMAGE)) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN, 3, 1, false, false));
                }
            }
            hitH5Echoes(server, lockedPlayer, sample);
        }
    }

    private void hitH5Echoes(ServerLevel server, ServerPlayer lockedPlayer, Vec3 sample) {
        for (int index = h5EchoUuids.size() - 1; index >= 0; index--) {
            UUID echoId = h5EchoUuids.get(index);
            Entity echo = server.getEntity(echoId);
            if (echo == null || echo.isRemoved()) {
                h5EchoUuids.remove(index);
                continue;
            }
            if (echo.position().distanceToSqr(sample) > H5_ECHO_HIT_RADIUS_SQR) {
                continue;
            }
            Vec3 echoPosition = echo.position();
            echo.discard();
            h5EchoUuids.remove(index);
            drawH5TeleportBurst(server, echoPosition);
            server.playSound(null, BlockPos.containing(echoPosition), SoundEvents.PHANTOM_BITE,
                    SoundSource.HOSTILE, 1.0F, 1.8F);
            server.playSound(null, BlockPos.containing(echoPosition), SoundEvents.PHANTOM_BITE,
                    SoundSource.HOSTILE, 1.0F, 0.4F);
            server.playSound(null, BlockPos.containing(echoPosition),
                    SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                    SoundSource.HOSTILE, 0.9F, 2.0F);
            if (lockedPlayer.isAlive() && !lockedPlayer.isSpectator()
                    && h5HitPlayers.add(lockedPlayer.getUUID())) {
                lockedPlayer.removeEffect(MobEffects.DAMAGE_RESISTANCE);
                lockedPlayer.addEffect(new MobEffectInstance(
                        MobEffects.HARM, 20, 3, false, false));
            }
        }
    }

    private void drawH5ChargeTrail(ServerLevel server, Vec3 direction, int level) {
        Vec3 lateral = new Vec3(direction.z, 0.0D, -direction.x);
        for (int band = 0; band < H5_TRAIL_DUST.length; band++) {
            Vec3 center = position().subtract(direction.scale(8.2D + band * 0.2D));
            for (int height = 0; height <= 8; height++) {
                server.sendParticles(H5_TRAIL_DUST[band],
                        center.x, center.y + height * 0.2D, center.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        for (double lane : h5LaneOffsets(level)) {
            if (lane == 0.0D) {
                continue;
            }
            Vec3 point = position()
                    .subtract(direction.scale(8.6D))
                    .add(lateral.scale(lane));
            server.sendParticles(RED_DUST,
                    point.x, point.y + 0.8D, point.z,
                    2, 0.05D, 0.35D, 0.05D, 0.0D);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    point.x, point.y + 0.8D, point.z,
                    2, 0.08D, 0.35D, 0.08D, 0.0D);
        }
    }

    private void drawH5TeleportBurst(ServerLevel server, Vec3 center) {
        for (int index = 0; index < 24; index++) {
            double angle = index * Mth.TWO_PI / 24.0D;
            Vec3 radial = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            Vec3 point = center.add(radial.scale(8.0D));
            server.sendParticles(ParticleTypes.SQUID_INK,
                    point.x, point.y + 0.2D, point.z,
                    0, -radial.x * 0.18D, 0.16D, -radial.z * 0.18D, 1.0D);
        }
    }

    private void spawnH5Echo(ServerLevel server, ServerPlayer target) {
        ArmorStand echo = new ArmorStand(
                server, target.getX(), encounterCenter.y, target.getZ());
        echo.setNoGravity(false);
        echo.setInvulnerable(true);
        echo.setNoBasePlate(true);
        echo.setShowArms(false);
        echo.setCustomName(Component.translatable(
                        "entity.finalparadox.marawthar.echo", target.getDisplayName())
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        echo.setCustomNameVisible(true);
        echo.setYRot(target.getYRot());
        echo.setItemSlot(EquipmentSlot.FEET,
                dyed(new ItemStack(Items.LEATHER_BOOTS), 2236962));
        echo.setItemSlot(EquipmentSlot.LEGS,
                dyed(new ItemStack(Items.LEATHER_LEGGINGS), 2236962));
        echo.setItemSlot(EquipmentSlot.CHEST,
                dyed(new ItemStack(Items.LEATHER_CHESTPLATE), 2236962));
        echo.setItemSlot(EquipmentSlot.HEAD, texturedHead(
                UUID.nameUUIDFromBytes(("finalparadox-h5-echo-" + target.getUUID()).getBytes(
                        java.nio.charset.StandardCharsets.UTF_8)),
                H5_ECHO_HEAD_TEXTURE));
        echo.addTag("finalparadox_marawthar_" + getUUID());
        echo.addTag("finalparadox_marawthar_h5_echo");
        if (server.addFreshEntity(echo)) {
            h5EchoUuids.add(echo.getUUID());
        }
    }

    private void discardH5Echoes(ServerLevel server) {
        for (UUID echoId : h5EchoUuids) {
            Entity echo = server.getEntity(echoId);
            if (echo != null) {
                echo.discard();
            }
        }
        h5EchoUuids.clear();
    }

    private void tickBlueRush(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            beginChoreographyWait();
            return;
        }
        int elapsed = combatTick - 1;
        int sequenceIndex = latestStartedSequence(BLUE_RUSH_STARTS, elapsed);
        int localScore = elapsed - BLUE_RUSH_STARTS[sequenceIndex] + 1;
        if (elapsed == BLUE_RUSH_STARTS[sequenceIndex]) {
            teleportAround(target, BLUE_RUSH_RADII[sequenceIndex],
                    BLUE_RUSH_ANGLES[sequenceIndex]);
            clearSlashHits();
            if (sequenceIndex == 0) {
                blueRushTrailDirection = Vec3.ZERO;
            }
        } else if (sequenceIndex == 0
                && blueRushTrailDirection.lengthSqr() >= 1.0E-6D) {
            faceBlueRushDirection();
        } else {
            lookAt(target);
        }

        int animation = sequenceIndex == 0
                ? ANIMATION_BLUE_RUSH
                : h1VariantAnimation(sequenceIndex);
        setSwordComboVisual(
                animation, localScore, BLUE_RUSH_VISUAL_DURATIONS[sequenceIndex]);

        tickBlueRushTrail(server);
        boolean attackActive = localScore <= BLUE_RUSH_ATTACK_DURATIONS[sequenceIndex];
        if (attackActive && sequenceIndex == 0) {
            tickBlueRushOpeningCombo(server, localScore);
        } else if (attackActive && sequenceIndex == 1) {
            if (localScore == 12) {
                stepForward(1.0D);
                playBlueRushSlash(server);
            }
            if (localScore == 15 || localScore == 16) {
                drawAndDamageH1VariantFrame(
                        server, MarawTharH1VariantFrames.COMBO4[localScore - 15], 0);
            }
        } else if (attackActive) {
            if (localScore == 6) {
                stepForward(1.0D);
                playBlueRushSlash(server);
            }
            if (localScore == 11 || localScore == 12) {
                drawAndDamageH1VariantFrame(
                        server, MarawTharH1VariantFrames.COMBO3[localScore - 11], 0);
            }
        }

        if (combatTick >= BLUE_RUSH_DURATION) {
            finishChoreographyAction(server);
        }
    }

    private void tickBlueRushOpeningCombo(ServerLevel server, int localScore) {
        if (localScore >= 2 && localScore <= 15) {
            drawBlueRushWarning(server, localScore);
        }
        if (localScore == 15) {
            blueRushTrailDirection = Vec3.directionFromRotation(0.0F, getYRot())
                    .multiply(1.0D, 0.0D, 1.0D).normalize();
            faceBlueRushDirection();
        }
        if (localScore == 2) {
            server.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.MASTER, 0.6F, 1.1F);
        } else if (localScore == 23) {
            beginBlueRushTrail();
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                    SoundSource.MASTER, 1.5F, 0.6F);
            server.playSound(null, blockPosition(), SoundEvents.WITHER_SHOOT,
                    SoundSource.MASTER, 0.7F, 1.0F);
            server.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL,
                    SoundSource.MASTER, 0.9F, 1.0F);
        } else if (localScore == 25) {
            stepForward(0.5D);
        }
    }

    private void drawBlueRushWarning(ServerLevel server, int localScore) {
        double radius = Math.max(0.25D, 3.0D - (localScore - 2) * 0.23D);
        Vec3 center = position().add(0.0D, 0.5D, 0.0D);
        int samples = 28;
        double goldenAngle = Math.PI * (3.0D - Math.sqrt(5.0D));
        for (int index = 0; index < samples; index++) {
            double y = 1.0D - 2.0D * (index + 0.5D) / samples;
            double ring = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            double angle = index * goldenAngle;
            Vec3 point = center.add(
                    Math.cos(angle) * ring * radius,
                    y * radius,
                    Math.sin(angle) * ring * radius);
            server.sendParticles(BLUE_RUSH_DUST,
                    point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void beginBlueRushTrail() {
        Vec3 direction = blueRushTrailDirection.lengthSqr() >= 1.0E-6D
                ? blueRushTrailDirection.normalize()
                : Vec3.directionFromRotation(0.0F, getYRot())
                        .multiply(1.0D, 0.0D, 1.0D).normalize();
        blueRushTrailAge = 0;
        blueRushTrailDirection = direction;
        blueRushTrailPosition = position().add(0.0D, 0.7D, 0.0D)
                .subtract(direction.scale(4.0D));
        blueRushTrailHitPlayers.clear();
    }

    private void faceBlueRushDirection() {
        float yaw = yawFromDirection(blueRushTrailDirection);
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        yBodyRotO = yaw;
    }

    private void tickBlueRushTrail(ServerLevel server) {
        if (blueRushTrailAge < 0 || blueRushTrailAge >= 15
                || blueRushTrailDirection.lengthSqr() < 1.0E-6D) {
            return;
        }
        for (int substep = 0; substep < 6; substep++) {
            blueRushTrailPosition = blueRushTrailPosition.add(
                    blueRushTrailDirection.scale(0.35D));
            drawBlueRushTrailDust(server, blueRushTrailPosition);
            hitBlueRushTrailPlayers(server, blueRushTrailPosition);
        }
        drawBlueRushTrailRing(server, blueRushTrailPosition);
        blueRushTrailAge++;
        if (blueRushTrailAge >= 15) {
            blueRushTrailAge = -1;
        }
    }

    private void drawBlueRushTrailDust(ServerLevel server, Vec3 center) {
        for (int index = 0; index < 8; index++) {
            double angle = index * Mth.TWO_PI / 8.0D + 0.42D;
            Vec3 point = center.add(
                    Math.cos(angle) * 2.5D, 0.0D, Math.sin(angle) * 2.5D);
            server.sendParticles(BLUE_RUSH_DUST,
                    point.x, point.y, point.z, 1,
                    0.15D, 0.0D, 0.15D, 0.0D);
        }
    }

    private void drawBlueRushTrailRing(ServerLevel server, Vec3 center) {
        for (int index = 0; index < 24; index++) {
            double angle = index * Mth.TWO_PI / 24.0D;
            Vec3 point = center.add(
                    Math.cos(angle) * 2.5D, 0.0D, Math.sin(angle) * 2.5D);
            server.sendParticles(ParticleTypes.END_ROD,
                    point.x, point.y, point.z, 0,
                    0.0D, -1.0D, 0.0D, 1.0D);
        }
    }

    private void hitBlueRushTrailPlayers(ServerLevel server, Vec3 center) {
        for (ServerPlayer player : server.getEntitiesOfClass(
                ServerPlayer.class, new AABB(center, center).inflate(3.0D),
                candidate -> candidate.isAlive() && !candidate.isSpectator()
                        && !blueRushTrailHitPlayers.contains(candidate.getUUID())
                        && candidate.position().distanceToSqr(center) <= 9.0D)) {
            if (!player.hurt(damageSources().magic(), BLUE_ATTACK_DAMAGE)) {
                continue;
            }
            blueRushTrailHitPlayers.add(player.getUUID());
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
            server.playSound(null, player.blockPosition(),
                    SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                    SoundSource.MASTER, 1.0F, 2.0F);
            server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void playBlueRushSlash(ServerLevel server) {
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THROW,
                SoundSource.MASTER, 1.5F, 0.6F);
    }

    private void tickPhaseIntermission(ServerLevel server) {
        entityData.set(DATA_TRANSITION_VISUAL_TICK, combatTick);
        setAnimation(ANIMATION_IDLE, combatTick);
        tickH6Transition(server);
        tickH7Schedule(server, false);
        tickH7Projectiles(server);
        tickH7Schedule(server, true);
        for (int startTick : H9_START_TICKS) {
            if (combatTick == startTick) {
                beginH9(server);
                break;
            }
        }
        if (h9StartTick >= 0) {
            tickH9(server, combatTick - h9StartTick);
        }
        if (combatTick >= PHASE_INTERMISSION_DURATION) {
            entityData.set(DATA_TRANSITION_VISUAL_TICK, -1);
            clearH7Projectiles();
            clearH9(server, false);
            int nextPhase = getPhase() + 1;
            if (nextPhase <= 3) {
                setPhase(nextPhase);
                setChoreographyStep(0);
                setEnraged(false);
                resetRandomAttackBag(server);
                ServerPlayer target = chooseTarget(server);
                beginTransitionOpening(target);
            } else {
                beginChoreographyWait();
            }
        }
    }

    private void tickH6Transition(ServerLevel server) {
        int introScore = combatTick + H6_INTRO_SCORE_OFFSET;
        if (introScore == 48) {
            startIntermissionMusic(server);
        }
        if (combatTick <= H6_INTRO_LAST_TICK) {
            setAnimation(ANIMATION_H6_TRANSITION, combatTick);
            ServerPlayer target = chooseTarget(server);
            if (target != null) {
                lookAt(target);
            }
            drawH6IntroParticles(server, introScore);
        }
        if (combatTick == H6_FLOOR_BREAK_TICK) {
            breakH6Floor(server);
            playH6FloorBreak(server);
        }
        if (combatTick == H6_SECOND_EXPLOSION_TICK) {
            drawH6RadialExplosion(server);
        }
        if (combatTick >= H6_FIRE_FIRST_TICK && combatTick < H6_FIRE_END_TICK) {
            tickH6FireHazard(server);
        }
        if (combatTick >= H6_FIRE_FIRST_TICK
                && combatTick <= H6_RESTORE_FULL_TICK) {
            drawH6OuterFireRing(server);
        }
        if (combatTick == H6_FIRE_END_TICK) {
            playH6FireShutdown(server);
        }
        if (combatTick == H6_RESTORE_30_TICK) {
            restoreH6Floor(server, 1);
        } else if (combatTick == H6_RESTORE_66_TICK) {
            restoreH6Floor(server, 2);
        } else if (combatTick == H6_RESTORE_FULL_TICK) {
            restoreH6Floor(server, 3);
        }
    }

    private void drawH6IntroParticles(ServerLevel server, int score) {
        Vec3 origin = encounterCenter.add(0.0D, h6VisualHeight(score), 0.0D);
        Vec3 facing = horizontalFacing();
        Vec3 side = facing.yRot(Mth.HALF_PI);

        if (score == 1 || score == 4 || score == 6) {
            double hand = score == 1 ? 0.4D : score == 4 ? 0.6D : 0.7D;
            double height = score == 1 ? 0.6D : 0.7D;
            for (int sign : new int[]{-1, 1}) {
                Vec3 point = origin.add(side.scale(hand * sign)).add(0.0D, height, 0.0D);
                server.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y, point.z,
                        1, 0.05D, 0.05D, 0.05D, 0.0D);
            }
        }

        if (score >= 10 && score <= 30) {
            drawH6HandDust(server, origin, side, 0.7D, 0.7D);
        } else if (score >= 31 && score <= 37) {
            double progress = Mth.clamp((score - 31) / 6.0D, 0.0D, 1.0D);
            drawH6HandDust(server, origin, side,
                    Mth.lerp(progress, 0.8D, 0.5D),
                    Mth.lerp(progress, 0.9D, 2.2D));
        } else if (score >= 39 && score <= 70) {
            drawH6HandDust(server, origin, side, 0.1D, 2.4D);
        }

        if (score >= 45) {
            double progress = Mth.clamp((score - 45) / 9.0D, 0.0D, 1.0D);
            int layers = Math.max(1, Mth.ceil(progress * 15.0D));
            for (int layer = 0; layer < layers; layer++) {
                double vertical = layer / 14.0D;
                double radius = 1.4D * Math.sin(vertical * Math.PI);
                double y = 0.03D + vertical * 2.75D;
                for (int pointIndex = 0; pointIndex < 12; pointIndex++) {
                    double angle = (pointIndex + layer * 0.17D) * Math.PI * 2.0D / 12.0D;
                    Vec3 point = origin.add(
                            Math.cos(angle) * radius,
                            y,
                            Math.sin(angle) * radius);
                    server.sendParticles(ParticleTypes.SQUID_INK,
                            point.x, point.y, point.z,
                            0, 0.0D, 1.0D, 0.0D, 100000.0D);
                }
            }
            server.sendParticles(ParticleTypes.SQUID_INK,
                    origin.x, origin.y + 1.2D, origin.z,
                    3, 0.8D, 0.8D, 0.8D, 0.0D);
        }

        double warningYaw = Math.toRadians(combatTick * 16.0D);
        for (int quarter = 0; quarter < 4; quarter++) {
            double angle = warningYaw + quarter * Mth.HALF_PI;
            Vec3 warning = encounterCenter.add(
                    -Math.sin(angle) * 5.5D,
                    0.2D,
                    Math.cos(angle) * 5.5D);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    warning.x, warning.y, warning.z,
                    0, 0.0D, 1.0D, 0.0D, score >= 82 ? 1.0D : 0.1D);
        }
        if (score >= 82) {
            for (int quarter = 0; quarter < 4; quarter++) {
                double angle = warningYaw + quarter * Mth.HALF_PI
                        + Math.toRadians(8.0D);
                Vec3 warning = encounterCenter.add(
                        -Math.sin(angle),
                        0.2D,
                        Math.cos(angle));
                server.sendParticles(ParticleTypes.SQUID_INK,
                        warning.x, warning.y, warning.z,
                        0, 0.0D, 1.0D, 0.0D, 2.0D);
            }
            server.sendParticles(ParticleTypes.SQUID_INK,
                    encounterCenter.x, encounterCenter.y + 0.2D, encounterCenter.z,
                    0, 0.0D, 1.0D, 0.0D, 2.0D);
        }
    }

    private void drawH6HandDust(ServerLevel server, Vec3 origin, Vec3 side,
                                double distance, double height) {
        for (int sign : new int[]{-1, 1}) {
            Vec3 point = origin.add(side.scale(distance * sign)).add(0.0D, height, 0.0D);
            server.sendParticles(H7_BLACK_DUST,
                    point.x, point.y, point.z,
                    1, 0.05D, 0.05D, 0.05D, 0.0D);
            server.sendParticles(H7_RED_DUST,
                    point.x, point.y, point.z,
                    1, 0.05D, 0.05D, 0.05D, 0.0D);
        }
    }

    private static double h6VisualHeight(int score) {
        double height = 0.5D;
        if (score <= 27) {
            return height + Math.max(0, score + 2) * 0.02D;
        }
        height += 29.0D * 0.02D;
        if (score <= 60) {
            return height + (score - 27) * 0.04D;
        }
        height += 33.0D * 0.04D;
        if (score <= 65) {
            return height + (score - 60) * 0.02D;
        }
        height += 5.0D * 0.02D;
        if (score <= 68) {
            return height + (score - 65) * 0.25D;
        }
        height += 3.0D * 0.25D;
        if (score <= 74) {
            return height + (score - 68) * 0.1D;
        }
        height += 6.0D * 0.1D;
        if (score <= 76) {
            return height - (score - 74) * 0.25D;
        }
        height -= 2.0D * 0.25D;
        return height - Math.min(score - 76, 8) * 1.0D;
    }

    private void breakH6Floor(ServerLevel server) {
        BlockPos center = BlockPos.containing(encounterCenter);
        replaceH6Cuboid(server, center.offset(-5, -2, -2), center.offset(5, -1, 2),
                Blocks.AIR.defaultBlockState(), false);
        replaceH6Cuboid(server, center.offset(-2, -2, -5), center.offset(2, -1, 5),
                Blocks.AIR.defaultBlockState(), false);
        replaceH6Cuboid(server, center.offset(-4, -1, -4), center.offset(4, -1, 4),
                Blocks.AIR.defaultBlockState(), false);
        replaceH6Cuboid(server, center.offset(-6, -2, -6), center.offset(6, -2, 6),
                Blocks.LAVA.defaultBlockState(), true);
        replaceH6Cuboid(server, center.offset(-6, -1, -6), center.offset(6, -1, 6),
                Blocks.BARRIER.defaultBlockState(), true);
    }

    private void replaceH6Cuboid(ServerLevel server, BlockPos first, BlockPos second,
                                 BlockState replacement, boolean onlyAir) {
        for (BlockPos pos : BlockPos.betweenClosed(first, second)) {
            BlockState current = server.getBlockState(pos);
            if ((onlyAir && !current.isAir()) || current == replacement) {
                continue;
            }
            alterH6Block(server, pos.immutable(), replacement);
        }
    }

    private void playH6FloorBreak(ServerLevel server) {
        var lightning = EntityType.LIGHTNING_BOLT.create(server);
        if (lightning != null) {
            lightning.moveTo(encounterCenter.x, encounterCenter.y, encounterCenter.z);
            server.addFreshEntity(lightning);
        }
        server.sendParticles(ParticleTypes.FLASH,
                encounterCenter.x, encounterCenter.y, encounterCenter.z,
                3, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                encounterCenter.x, encounterCenter.y, encounterCenter.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                encounterCenter.x, encounterCenter.y + 0.5D, encounterCenter.z,
                40, 4.0D, 1.5D, 4.0D, 0.12D);
    }

    private void drawH6RadialExplosion(ServerLevel server) {
        for (int index = 0; index < 48; index++) {
            double angle = index * Math.PI * 2.0D / 48.0D;
            double radius = 1.0D + index % 6;
            Vec3 point = encounterCenter.add(
                    Math.cos(angle) * radius,
                    0.2D,
                    Math.sin(angle) * radius);
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    point.x, point.y, point.z,
                    1, 0.0D, 0.15D, 0.0D, 0.1D);
        }
    }

    private void tickH6FireHazard(ServerLevel server) {
        double yaw = -90.0D
                + (combatTick - H6_FIRE_FIRST_TICK) * H6_FIRE_ROTATION_PER_TICK;
        double yawRadians = Math.toRadians(yaw);
        BlockPos center = BlockPos.containing(encounterCenter);
        int floorY = center.getY() - 1;
        Vec3 lastPoint = encounterCenter;
        for (double radius : H6_FIRE_RADII) {
            double x = encounterCenter.x - Math.sin(yawRadians) * radius;
            double z = encounterCenter.z + Math.cos(yawRadians) * radius;
            BlockPos point = BlockPos.containing(x, floorY, z);
            lastPoint = new Vec3(x, encounterCenter.y, z);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos floor = point.offset(dx, 0, dz);
                    if (!server.getBlockState(floor).isAir()) {
                        alterH6Block(server, floor, Blocks.MAGMA_BLOCK.defaultBlockState());
                    }
                }
            }
            server.sendParticles(ParticleTypes.FLAME,
                    x, encounterCenter.y + 0.3D, z,
                    2, 0.35D, 0.8D, 0.35D, 0.02D);
        }
        applyH6EdgeCorrections(server, lastPoint);
        damageH6FloorPlayers(server, floorY);
    }

    private void applyH6EdgeCorrections(ServerLevel server, Vec3 outerFire) {
        BlockPos center = BlockPos.containing(encounterCenter);
        copyH6Block(server, center.offset(15, -1, 12), center.offset(14, -1, 13));
        copyH6Block(server, center.offset(-12, -1, -15), center.offset(-13, -1, -14));
        copyH6Block(server, center.offset(14, -1, -13), center.offset(13, -1, -14));
        copyH6Block(server, center.offset(-12, -1, 15), center.offset(-13, -1, 14));
        if (outerFire.distanceToSqr(encounterCenter.add(-16.0D, 0.0D, 9.0D)) <= 9.0D) {
            alterH6Block(server, center.offset(-16, -1, 9),
                    Blocks.MAGMA_BLOCK.defaultBlockState());
        }
        if (outerFire.distanceToSqr(encounterCenter.add(9.0D, 0.0D, 16.0D)) <= 9.0D) {
            alterH6Block(server, center.offset(9, -1, 16),
                    Blocks.MAGMA_BLOCK.defaultBlockState());
        }
    }

    private void copyH6Block(ServerLevel server, BlockPos source, BlockPos target) {
        alterH6Block(server, target, server.getBlockState(source));
    }

    private void damageH6FloorPlayers(ServerLevel server, int floorY) {
        for (ServerPlayer player : server.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            BlockState floor = server.getBlockState(
                    BlockPos.containing(player.getX(), floorY, player.getZ()));
            if (!floor.is(Blocks.MAGMA_BLOCK) && !floor.is(Blocks.BARRIER)) {
                continue;
            }
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 3, false, false));
            server.sendParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY(), player.getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void drawH6OuterFireRing(ServerLevel server) {
        int descentTick = Math.max(0, combatTick - H6_FIRE_END_TICK);
        double y = encounterCenter.y - 3.0D - descentTick * 0.15D;
        double spin = (combatTick - H6_FIRE_FIRST_TICK) * -0.5D;
        for (int index = 0; index < 24; index++) {
            double angle = Math.toRadians(spin + 15.0D * (index + 1));
            double x = encounterCenter.x - Math.sin(angle) * H6_OUTER_FIRE_RADIUS;
            double z = encounterCenter.z + Math.cos(angle) * H6_OUTER_FIRE_RADIUS;
            server.sendParticles(ParticleTypes.FLAME,
                    x, y, z,
                    2, 0.8D, 1.5D, 0.8D, 0.04D);
            if ((combatTick & 3) == 0) {
                server.sendParticles(ParticleTypes.LARGE_SMOKE,
                        x, y + 1.0D, z,
                        1, 0.5D, 1.0D, 0.5D, 0.02D);
            }
        }
    }

    private void playH6FireShutdown(ServerLevel server) {
        double yaw = -90.0D
                + (combatTick - H6_FIRE_FIRST_TICK) * H6_FIRE_ROTATION_PER_TICK;
        double yawRadians = Math.toRadians(yaw);
        for (double radius : H6_FIRE_RADII) {
            double x = encounterCenter.x - Math.sin(yawRadians) * radius;
            double z = encounterCenter.z + Math.cos(yawRadians) * radius;
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    x, encounterCenter.y + 0.3D, z,
                    20, 1.0D, 2.0D, 1.0D, 0.1D);
            server.sendParticles(ParticleTypes.EXPLOSION,
                    x, encounterCenter.y + 0.3D, z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.playSound(null, BlockPos.containing(encounterCenter), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.MASTER, 1.0F, 0.8F);
    }

    private void alterH6Block(ServerLevel server, BlockPos pos, BlockState replacement) {
        BlockState current = server.getBlockState(pos);
        if (current == replacement) {
            return;
        }
        h6AlteredBlocks.putIfAbsent(pos.immutable(), current);
        server.setBlock(pos, replacement, 2);
    }

    private void captureH6Floor(ServerLevel server) {
        h6AlteredBlocks.clear();
        BlockPos center = BlockPos.containing(encounterCenter);
        for (int dx = -18; dx <= 18; dx++) {
            for (int dy = -3; dy <= -1; dy++) {
                for (int dz = -18; dz <= 18; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    h6AlteredBlocks.put(pos, server.getBlockState(pos));
                }
            }
        }
    }

    private void restoreH6Floor(ServerLevel server, int stage) {
        for (Map.Entry<BlockPos, BlockState> entry : h6AlteredBlocks.entrySet()) {
            boolean restore = stage >= 3
                    || h6RestoreSample(entry.getKey(), 33) < 0.30D
                    || (stage >= 2 && h6RestoreSample(entry.getKey(), 66) < 0.66D);
            if (restore) {
                server.setBlock(entry.getKey(), entry.getValue(), 2);
            }
        }
        if (stage >= 3) {
            h6AlteredBlocks.clear();
        }
    }

    private static double h6RestoreSample(BlockPos pos, int salt) {
        long value = pos.asLong() ^ (salt * 0x9E3779B97F4A7C15L);
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        value ^= value >>> 31;
        return (value >>> 11) * 0x1.0p-53;
    }

    private void tickH7Schedule(ServerLevel server, boolean immediateVolley) {
        for (int pattern = 0; pattern < H7_PATTERN_START_TICKS.length; pattern++) {
            int localTick = combatTick - H7_PATTERN_START_TICKS[pattern];
            if ((localTick == 0) != immediateVolley) {
                continue;
            }
            if (containsTick(H7_RED_VOLLEY_TICKS[pattern], localTick)) {
                spawnH7Volley(server, false);
            }
            if (containsTick(H7_BLUE_VOLLEY_TICKS[pattern], localTick)) {
                spawnH7Volley(server, true);
            }
        }
    }

    private static boolean containsTick(int[] ticks, int candidate) {
        for (int tick : ticks) {
            if (tick == candidate) {
                return true;
            }
        }
        return false;
    }

    private void spawnH7Volley(ServerLevel server, boolean blue) {
        if (blue) {
            for (int index = h7Projectiles.size() - 1; index >= 0; index--) {
                H7Projectile projectile = h7Projectiles.get(index);
                if (projectile.blue) {
                    h7Projectiles.remove(index);
                }
            }
        }
        Vec3 origin = encounterCenter.add(0.0D, blue ? 1.8D : 1.85D, 0.0D);
        for (ServerPlayer player : server.players()) {
            if (!player.isAlive() || player.isSpectator()) {
                continue;
            }
            if (blue) {
                // The source aims at the eyes of a small marker spawned 0.65 blocks
                // above the player, which is effectively the player's eye height.
                float[] rotation = rotationToward(origin, player.getEyePosition());
                h7Projectiles.add(new H7Projectile(
                        true, player.getUUID(), 0, 0, origin, rotation[0], rotation[1]));
            } else {
                int curve = 0;
                if (random.nextBoolean()) curve |= 1;
                if (random.nextBoolean()) curve |= 2;
                if (random.nextBoolean()) curve |= 4;
                if (random.nextBoolean()) curve |= 8;
                if (curve == 0) curve = 1;
                h7Projectiles.add(new H7Projectile(
                        false, player.getUUID(), curve, 0, origin, 0.0F, 0.0F));
            }
        }

        server.sendParticles(ParticleTypes.FLASH,
                origin.x, origin.y, origin.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (blue) {
            server.sendParticles(ParticleTypes.EXPLOSION,
                    origin.x, origin.y, origin.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        server.sendParticles(ParticleTypes.SQUID_INK,
                origin.x, origin.y, origin.z,
                5, 0.0D, 0.0D, 0.0D, 0.2D);
        server.playSound(null, BlockPos.containing(origin), SoundEvents.PUFFER_FISH_BLOW_OUT,
                SoundSource.MASTER, 2.0F, 0.9F);
        server.playSound(null, BlockPos.containing(origin), SoundEvents.ENDER_EYE_DEATH,
                SoundSource.MASTER, 1.5F, 0.5F);
    }

    private void tickH7Projectiles(ServerLevel server) {
        for (int index = h7Projectiles.size() - 1; index >= 0; index--) {
            H7Projectile projectile = h7Projectiles.get(index);
            boolean remove = projectile.blue
                    ? tickH7BlueProjectile(server, projectile)
                    : tickH7RedProjectile(server, projectile);
            projectile.age++;
            if (remove || projectile.age >= H7_PROJECTILE_LIFETIME) {
                h7Projectiles.remove(index);
            }
        }
    }

    private boolean tickH7RedProjectile(ServerLevel server, H7Projectile projectile) {
        ServerPlayer target = h7Target(server, projectile.target);
        if (target == null) {
            return true;
        }

        Vec3 previousPosition = projectile.position;
        projectile.position = projectile.position.add(localOffset(
                projectile.pitch, projectile.yaw,
                H7_CURVE_OFFSETS[projectile.curve], 0.2D, 0.6D));
        float[] rotation = rotationToward(projectile.position, target.position());
        projectile.yaw = rotation[0];
        projectile.pitch = rotation[1];
        Vec3 position = projectile.position;
        server.sendParticles(H7_BLACK_DUST,
                position.x, position.y, position.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(H7_RED_DUST,
                position.x, position.y, position.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.END_ROD,
                position.x, position.y, position.z,
                0, 0.0D, -1.0D, 0.0D, 10000.0D);

        if (target.position().distanceToSqr(position) > H7_RED_HIT_RADIUS_SQR) {
            return false;
        }
        if (projectileHitCooldowns.getOrDefault(target.getUUID(), 0) <= tickCount
                && projectile.hitPlayers.add(target.getUUID())) {
            projectileHitCooldowns.put(target.getUUID(), tickCount + 2);
            Vec3 incoming = position.subtract(previousPosition);
            Vec3 parryOrigin = incoming.lengthSqr() > 1.0E-6D
                    ? position.subtract(incoming.normalize().scale(4.0D))
                    : position;
            if (!AtacromGauntletItem.tryParryRedAttack(
                    target, parryOrigin, H7_RED_GUARD_HALF_ANGLE)) {
                damageH7ProjectilePlayer(server, target);
            }
        }
        return true;
    }

    private boolean tickH7BlueProjectile(ServerLevel server, H7Projectile projectile) {
        Vec3 forward = Vec3.directionFromRotation(projectile.pitch, projectile.yaw)
                .normalize().scale(H7_BLUE_TRAVEL_PER_TICK * 0.5D);
        projectile.position = projectile.position.add(forward);
        server.sendParticles(H7_BLUE_DUST,
                projectile.position.x, projectile.position.y, projectile.position.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        projectile.position = projectile.position.add(forward);
        Vec3 endpoint = projectile.position;
        server.sendParticles(ParticleTypes.END_ROD,
                endpoint.x, endpoint.y, endpoint.z,
                0, 0.0D, -1.0D, 0.0D, 10000.0D);
        server.sendParticles(ParticleTypes.SQUID_INK,
                endpoint.x, endpoint.y, endpoint.z,
                0, 0.0D, -1.0D, 0.0D, 10000.0D);
        server.sendParticles(H7_BLUE_DUST,
                endpoint.x, endpoint.y, endpoint.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.END_ROD,
                endpoint.x, endpoint.y, endpoint.z,
                0, 0.0D, -1.0D, 0.0D, 10000.0D);

        for (ServerPlayer player : server.players()) {
            if (!player.isAlive() || player.isSpectator()
                    || !player.getBoundingBox().inflate(H7_BLUE_HIT_RADIUS).contains(endpoint)
                    || projectile.hitPlayers.contains(player.getUUID())
                    || projectileHitCooldowns.getOrDefault(player.getUUID(), 0) > tickCount) {
                continue;
            }
            projectile.hitPlayers.add(player.getUUID());
            projectileHitCooldowns.put(player.getUUID(), tickCount + 2);
            if (AtacromGauntletItem.isGuarding(player)) {
                Component warning =
                        Component.translatable("message.finalparadox.marawthar.blue_unblockable");
                player.sendSystemMessage(warning);
                player.displayClientMessage(warning, true);
                player.playNotifySound(SoundEvents.TRIDENT_RETURN,
                        SoundSource.MASTER, 0.5F, 2.0F);
            }
            damageH7ProjectilePlayer(server, player);
        }
        return false;
    }

    private void damageH7ProjectilePlayer(ServerLevel server, ServerPlayer player) {
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 3, false, false));
        server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                SoundSource.MASTER, 1.0F, 1.0F);
        server.sendParticles(ParticleTypes.SQUID_INK,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                6, 0.0D, 0.0D, 0.0D, 0.2D);
    }

    @Nullable
    private static ServerPlayer h7Target(ServerLevel server, UUID targetId) {
        Entity entity = server.getEntity(targetId);
        return entity instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                ? player : null;
    }

    private static Vec3 localOffset(float pitch, float yaw,
                                    double left, double up, double forward) {
        Vec3 forwardVector = Vec3.directionFromRotation(pitch, yaw).normalize();
        Vec3 upVector = Vec3.directionFromRotation(pitch - 90.0F, yaw).normalize();
        Vec3 leftVector = forwardVector.cross(upVector).scale(-1.0D).normalize();
        return leftVector.scale(left)
                .add(upVector.scale(up))
                .add(forwardVector.scale(forward));
    }

    private static float[] rotationToward(Vec3 origin, Vec3 target) {
        Vec3 delta = target.subtract(origin);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        return new float[]{
                (float) Math.toDegrees(Math.atan2(-delta.x, delta.z)),
                (float) Math.toDegrees(Math.atan2(-delta.y, horizontal))
        };
    }

    private void clearH7Projectiles() {
        h7Projectiles.clear();
    }

    private void beginH9(ServerLevel server) {
        clearH9(server, false);
        h9StartTick = combatTick;
        ServerPlayer target = chooseTarget(server);
        h9TargetUuid = target == null ? null : target.getUUID();
        if (target != null) {
            lookAt(target);
            h9LockedDirection = horizontalDirectionTo(target.position());
        } else {
            h9LockedDirection = horizontalFacing();
        }
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 1.5F);
        }
    }

    private void tickH9(ServerLevel server, int localTick) {
        if (localTick < 0) {
            return;
        }
        if (localTick >= H9_END_TICK) {
            clearH9(server, true);
            setAnimation(ANIMATION_IDLE, combatTick);
            return;
        }

        setAnimation(ANIMATION_H9_LASER, localTick);
        if (localTick <= H9_FIRST_WARNING_TICK) {
            ServerPlayer target = h9Target(server);
            if (target != null) {
                lookAt(target);
                h9LockedDirection = horizontalDirectionTo(target.position());
            } else if (h9LockedDirection.lengthSqr() < 1.0E-6D) {
                h9LockedDirection = horizontalFacing();
            }
            Vec3 previewBase = position().add(0.0D, -0.2D, 0.0D)
                    .add(h9LockedDirection.scale(0.4D));
            updateH9Laser(server, previewBase, h9LockedDirection, localTick);
            if (localTick == H9_FIRST_WARNING_TICK) {
                drawH9Warning(server);
            }
            return;
        }

        if (localTick == H9_SECOND_WARNING_TICK) {
            drawH9Warning(server);
            return;
        }
        if (localTick < H9_RELEASE_TICK) {
            return;
        }

        int beamTick = localTick - H9_RELEASE_TICK;
        if (beamTick == 0) {
            h9SpinDirection = chooseH9Spin(server);
            for (ServerPlayer player : server.players()) {
                player.playNotifySound(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS,
                        SoundSource.MASTER, 0.8F, 0.0F);
                player.playNotifySound(SoundEvents.ILLUSIONER_PREPARE_BLINDNESS,
                        SoundSource.MASTER, 0.8F, 1.0F);
                player.playNotifySound(SoundEvents.END_GATEWAY_SPAWN,
                        SoundSource.MASTER, 0.8F, 1.5F);
            }
        }

        int rotatingTicks = Mth.clamp(beamTick - 4, 0, 70);
        double degrees = h9SpinDirection * rotatingTicks * H9_ROTATION_PER_TICK;
        Vec3 direction = h9LockedDirection.yRot((float) Math.toRadians(degrees)).normalize();
        faceHorizontal(direction);
        Vec3 beamBase = position().add(0.0D, -0.2D, 0.0D)
                .add(direction.scale(0.4D));
        updateH9Laser(server, beamBase, direction, beamTick);
        tickH9PulseEffects(server, beamBase, direction, beamTick);
        damageH9Players(server, beamBase, direction);
        if (beamTick >= 5 && beamTick <= 74) {
            server.sendParticles(ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + 1.5D, getZ(),
                    1, 0.0D, 0.0D, 0.0D, 0.5D);
        }
    }

    @Nullable
    private ServerPlayer h9Target(ServerLevel server) {
        if (h9TargetUuid == null) {
            return null;
        }
        Entity raw = server.getEntity(h9TargetUuid);
        return raw instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                ? player : null;
    }

    private Vec3 horizontalDirectionTo(Vec3 target) {
        Vec3 direction = target.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-6D ? horizontalFacing() : direction.normalize();
    }

    private Vec3 horizontalFacing() {
        Vec3 direction = Vec3.directionFromRotation(0.0F, getYRot())
                .multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-6D
                ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    private void faceHorizontal(Vec3 direction) {
        float yaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        yBodyRotO = yaw;
    }

    private void drawH9Warning(ServerLevel server) {
        Vec3 origin = position().add(0.0D, 1.5D, 0.0D);
        for (int distance = 1; distance <= 30; distance++) {
            Vec3 point = origin.add(h9LockedDirection.scale(distance));
            server.sendParticles(H9_WARNING_RED_DUST, point.x, point.y, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(),
                    SoundSource.MASTER, 1.0F, 2.0F);
        }
    }

    private int chooseH9Spin(ServerLevel server) {
        ServerPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            double distance = player.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        if (nearest == null) {
            return 1;
        }
        Vec3 left = h9LockedDirection.yRot(Mth.HALF_PI);
        Vec3 toward = nearest.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        return toward.dot(left) >= 0.0D ? 1 : -1;
    }

    private void updateH9Laser(ServerLevel server, Vec3 base, Vec3 direction, int beamTick) {
        NightfallLaserEntity laser = h9Laser(server);
        if (laser == null) {
            laser = NightfallLaserEntity.spawn(
                    server, base, direction, beamTick, getUUID());
            h9LaserUuid = laser.getUUID();
        } else {
            laser.setBeam(base, direction, beamTick);
        }
    }

    @Nullable
    private NightfallLaserEntity h9Laser(ServerLevel server) {
        if (h9LaserUuid == null) {
            return null;
        }
        Entity raw = server.getEntity(h9LaserUuid);
        return raw instanceof NightfallLaserEntity laser && laser.isOwnedBy(getUUID())
                ? laser : null;
    }

    private void tickH9PulseEffects(ServerLevel server, Vec3 base, Vec3 direction, int beamTick) {
        boolean pulseNearCaster = false;
        for (int segment = 1; segment <= H9_SEGMENTS; segment++) {
            if (h9PulseValue(segment, beamTick) != 1) {
                continue;
            }
            Vec3 marker = base.add(direction.scale(segment * H9_SEGMENT_SPACING));
            if (marker.distanceToSqr(position()) <= 4.0D) {
                pulseNearCaster = true;
                break;
            }
        }
        if (pulseNearCaster) {
            server.playSound(null, blockPosition(), SoundEvents.BEACON_POWER_SELECT,
                    SoundSource.AMBIENT, 2.0F, 1.2F);
            server.sendParticles(ParticleTypes.FLASH,
                    getX(), getY() + 1.0D, getZ(),
                    1, 0.0D, 0.0D, 0.0D, 1.0D);
        }
    }

    private static int h9PulseValue(int segment, int beamTick) {
        int initial = Math.floorMod(segment - 1, 8) + 1;
        return Math.floorMod(initial - beamTick - 2, 8) + 1;
    }

    private void damageH9Players(ServerLevel server, Vec3 base, Vec3 direction) {
        double radiusSqr = H9_HIT_RADIUS * H9_HIT_RADIUS;
        for (ServerPlayer player : server.players()) {
            if (!player.isAlive() || player.isSpectator()
                    || h9HitPlayers.contains(player.getUUID())) {
                continue;
            }
            boolean hit = false;
            for (int verticalSample = 0; verticalSample <= 4 && !hit; verticalSample++) {
                Vec3 sample = player.position().add(0.0D, -verticalSample * 2.0D, 0.0D);
                for (int segment = 1; segment <= H9_SEGMENTS; segment++) {
                    Vec3 marker = base.add(direction.scale(segment * H9_SEGMENT_SPACING));
                    if (sample.distanceToSqr(marker) <= radiusSqr) {
                        hit = true;
                        break;
                    }
                }
            }
            if (!hit) {
                continue;
            }
            h9HitPlayers.add(player.getUUID());
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 4, false, false));
            server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                    SoundSource.MASTER, 1.0F, 1.0F);
            server.sendParticles(ParticleTypes.LAVA,
                    player.getX(), player.getY(), player.getZ(),
                    6, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void clearH9(ServerLevel server, boolean playEndEffects) {
        NightfallLaserEntity laser = h9Laser(server);
        if (laser != null && playEndEffects) {
            Vec3 base = laser.position();
            Vec3 direction = laser.beamDirection();
            for (int segment = 1; segment <= H9_SEGMENTS; segment++) {
                Vec3 point = base.add(direction.scale(segment * H9_SEGMENT_SPACING))
                        .add(0.0D, 1.0D, 0.0D);
                server.sendParticles(ParticleTypes.LARGE_SMOKE,
                        point.x, point.y, point.z,
                        3, 0.2D, 0.2D, 0.2D, 0.0D);
            }
        }
        if (laser != null) {
            laser.discard();
        }
        if (playEndEffects) {
            for (ServerPlayer player : server.players()) {
                player.playNotifySound(SoundEvents.FIRE_EXTINGUISH,
                        SoundSource.MASTER, 1.0F, 0.6F);
            }
        }
        h9StartTick = -1;
        h9SpinDirection = 0;
        h9LockedDirection = Vec3.ZERO;
        h9TargetUuid = null;
        h9LaserUuid = null;
        h9HitPlayers.clear();
    }

    private void tickSourceLock(ServerLevel server) {
        if (sourceLockAction == MarawTharChoreography.Action.SWORDS_PHASE_TWO) {
            combatState = COMBAT_PHASE_TWO_SWORDS;
            tickPhaseTwoSwords(server);
            return;
        }
        if (sourceLockAction == MarawTharChoreography.Action.LASERS_PHASE_TWO) {
            combatState = COMBAT_PHASE_TWO_LASERS;
            tickPhaseTwoLasers(server);
            return;
        }
        setAnimation(ANIMATION_IDLE, combatTick);
        if (combatTick >= sourceLockDuration) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            finishChoreographyAction(server);
        }
    }

    private void tickH3Plant(ServerLevel server) {
        setAnimation(ANIMATION_H3_PLANT, combatTick);
        if (combatTick >= 4 && combatTick <= 14) {
            NightfallChainBladeEntity blade = h3Blade(server);
            if (blade != null) {
                blade.moveTo(blade.getX(), blade.getY() + 0.02D, blade.getZ(),
                        blade.getYRot(), 0.0F);
            }
        }
        if (combatTick == 15) {
            NightfallChainBladeEntity blade = h3Blade(server);
            if (blade != null) {
                blade.moveTo(blade.getX(), blade.getY() - 1.8D, blade.getZ(),
                        blade.getYRot(), 0.0F);
            }
            server.sendParticles(ParticleTypes.EXPLOSION,
                    h3BladePosition.x, h3BladePosition.y, h3BladePosition.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(new ItemParticleOption(
                            ParticleTypes.ITEM, new ItemStack(Items.CYAN_TERRACOTTA)),
                    h3BladePosition.x, h3BladePosition.y, h3BladePosition.z,
                    100, 0.0D, 0.0D, 0.0D, 0.4D);
            server.playSound(null, BlockPos.containing(h3BladePosition),
                    SoundEvents.TRIDENT_HIT_GROUND, SoundSource.MASTER, 3.0F, 0.0F);
            server.playSound(null, BlockPos.containing(h3BladePosition),
                    SoundEvents.TRIDENT_RETURN, SoundSource.MASTER, 3.0F, 0.0F);
        }
        if (combatTick == 23) {
            server.sendParticles(ParticleTypes.PORTAL,
                    getX(), getY() + 2.2D, getZ(),
                    80, 0.45D, 0.7D, 0.45D, 0.2D);
            setInvisible(true);
        }
        if (combatTick >= PUT_SWORD_DURATION) {
            finishChoreographyAction(server);
        }
    }

    private void tickH3Darkness(ServerLevel server) {
        if (combatTick <= 6) {
            drawH3Ring(server, h3ChasePosition, 0.5D + combatTick * 0.5D, 16);
        } else if (combatTick <= 86) {
            drawH3Ring(server, h3ChasePosition, 4.0D, 24);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    h3ChasePosition.x, h3ChasePosition.y + 0.1D, h3ChasePosition.z,
                    2, 1.5D, 0.0D, 1.5D, 0.0D);
        }

        if (combatTick <= 84) {
            ServerPlayer target = h3Target(server);
            if (target != null) {
                Vec3 toward = target.position().subtract(h3ChasePosition)
                        .multiply(1.0D, 0.0D, 1.0D);
                if (toward.lengthSqr() <= 1.0D) {
                    h3ChasePosition = new Vec3(
                            target.getX(), encounterCenter.y, target.getZ());
                } else {
                    h3ChasePosition = h3ChasePosition.add(
                            toward.normalize().scale(0.5D));
                }
                h3ChasePosition = new Vec3(
                        h3ChasePosition.x, encounterCenter.y, h3ChasePosition.z);
            }
        }

        if (combatTick == 85) {
            setInvisible(false);
            lookAtPoint(encounterCenter);
        }
        if (combatTick == 119) {
            moveTo(h3ChasePosition.x, h3ChasePosition.y, h3ChasePosition.z,
                    getYRot(), 0.0F);
            lookAtPoint(encounterCenter);
        }
        if (combatTick >= 85 && combatTick <= 142) {
            setAnimation(ANIMATION_H3_SLAM, combatTick - 84);
        } else if (combatTick > 142) {
            setInvisible(true);
            setAnimation(ANIMATION_IDLE, combatTick);
        }

        int warningScore = combatTick - 85;
        if (warningScore >= 1 && warningScore <= 37) {
            drawH3Warning(server, warningScore);
        }
        if (combatTick >= 95 && combatTick <= 122 && h3Blade(server) != null) {
            if (combatTick == 95) {
                playH3ChainStart(server);
            }
            tickH3Chain(server);
        }
        if (combatTick == 122) {
            triggerH3WarningEnd(server);
        }

        int explosionScore = combatTick - 122;
        if (explosionScore == 1) {
            triggerH3Damage(server);
        }
        if (explosionScore >= 1 && explosionScore <= 6) {
            drawH3InnerBurst(server);
        }
        if (explosionScore >= 6 && explosionScore <= 15) {
            drawH3OuterBurst(server);
        }

        if (combatTick >= DARKNESS_DURATION) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            h3TargetUuid = null;
            finishChoreographyAction(server);
        }
    }

    private void beginH3Plant(ServerLevel server) {
        discardH3Blade();
        List<Vec3> allCandidates = new ArrayList<>(36);
        List<Vec3> validCandidates = new ArrayList<>(36);
        Vec3 bossPosition = position();
        for (int angle = 0; angle < 360; angle += 10) {
            double sourceAngle = angle + (server.random.nextBoolean() ? 5.0D : 0.0D);
            double radians = Math.toRadians(sourceAngle);
            Vec3 candidate = new Vec3(
                    encounterCenter.x - Math.sin(radians) * 15.0D,
                    encounterCenter.y,
                    encounterCenter.z + Math.cos(radians) * 15.0D);
            allCandidates.add(candidate);
            boolean valid = candidate.distanceToSqr(bossPosition) > 100.0D;
            if (valid) {
                for (ServerPlayer player : server.players()) {
                    if (!player.isSpectator()
                            && candidate.distanceToSqr(player.position()) <= 100.0D) {
                        valid = false;
                        break;
                    }
                }
            }
            if (valid) {
                validCandidates.add(candidate);
            }
        }

        Vec3 selected;
        ServerPlayer focus = chooseTarget(server);
        if (!validCandidates.isEmpty() && focus != null) {
            selected = validCandidates.stream()
                    .min((first, second) -> Double.compare(
                            first.distanceToSqr(focus.position()),
                            second.distanceToSqr(focus.position())))
                    .orElse(validCandidates.get(0));
        } else {
            List<Vec3> pool = validCandidates.isEmpty() ? allCandidates : validCandidates;
            selected = pool.get(server.random.nextInt(pool.size()));
        }

        ServerPlayer nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) {
                continue;
            }
            double distance = selected.distanceToSqr(player.position());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = player;
            }
        }
        float yaw = nearest == null
                ? getYRot()
                : yawFromDirection(nearest.position().subtract(selected));
        yaw -= 80.0F;

        h3BladePosition = selected;
        NightfallChainBladeEntity blade = NightfallChainBladeEntity.spawnBossBlade(
                server, selected.add(0.0D, 1.8D, 0.0D), yaw, getUUID());
        h3BladeUuid = blade.getUUID();
        sourceLockAction = MarawTharChoreography.Action.PUT_SWORD;
        sourceLockDuration = PUT_SWORD_DURATION;
        beginScriptedAction(COMBAT_H3_PLANT, nearest);
        moveTo(selected.x, selected.y, selected.z, yaw, 0.0F);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        setInvisible(false);
        setAnimation(ANIMATION_H3_PLANT, 0);
    }

    private void beginH3Darkness(ServerLevel server) {
        sourceLockAction = MarawTharChoreography.Action.DARKNESS;
        sourceLockDuration = DARKNESS_DURATION;
        ServerPlayer target = chooseTarget(server);
        beginScriptedAction(COMBAT_H3_DARKNESS, null);
        h3TargetUuid = target == null ? null : target.getUUID();
        h3ChasePosition = new Vec3(getX(), encounterCenter.y, getZ());
        Vec3 platform = judgmentPlatformPosition();
        moveTo(platform.x, platform.y, platform.z, getYRot(), 0.0F);
        lookAtPoint(encounterCenter.add(0.0D, 1.62D, 0.0D));
        setInvisible(false);
        setAnimation(ANIMATION_IDLE, 0);
        server.playSound(null, BlockPos.containing(h3ChasePosition),
                SoundEvents.SHULKER_TELEPORT, SoundSource.MASTER, 2.0F, 1.2F);
    }

    private void drawH3Ring(ServerLevel server, Vec3 center, double radius, int points) {
        for (int index = 0; index < points; index++) {
            double angle = Mth.TWO_PI * index / points;
            server.sendParticles(ParticleTypes.SQUID_INK,
                    center.x + Math.sin(angle) * radius,
                    center.y,
                    center.z + Math.cos(angle) * radius,
                    0, 0.0D, 1.0D, 0.0D, 10000.0D);
        }
    }

    private void drawH3Warning(ServerLevel server, int score) {
        for (int marker = 0; marker < 3; marker++) {
            float yaw = marker * 30.0F + score * 2.0F;
            for (int arm = 0; arm < 4; arm++) {
                double angle = Math.toRadians(yaw + arm * 90.0F);
                Vec3 point = h3ChasePosition.add(
                        Math.sin(angle) * 28.0D, 0.15D, Math.cos(angle) * 28.0D);
                server.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y, point.z,
                        0, 0.0D, 0.3D, 0.0D, 1.0D);
                server.sendParticles(H3_BLACK_DUST,
                        point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        server.sendParticles(ParticleTypes.SQUID_INK,
                h3ChasePosition.x, h3ChasePosition.y + 0.3D, h3ChasePosition.z,
                12, 11.0D, 0.0D, 12.0D, 0.0D);
        drawH3Spiral(server, score);
    }

    private void drawH3Spiral(ServerLevel server, int warningScore) {
        int cycleScore = Math.floorMod(warningScore - 1, 36) + 1;
        float yaw = (warningScore - 1) * 7.0F;
        if (cycleScore <= H3_SPIRAL.length) {
            double radius = H3_SPIRAL[cycleScore - 1][0];
            double up = H3_SPIRAL[cycleScore - 1][1] + 0.15D;
            double hexZ = radius * Math.sqrt(3.0D) * 0.5D;
            double[][] points = {
                    {radius * 0.5D - 0.25D, hexZ - Math.sqrt(3.0D) * 0.25D},
                    {-radius * 0.5D, hexZ},
                    {-radius, 0.0D},
                    {-radius * 0.5D, -hexZ},
                    {radius * 0.5D, -hexZ},
                    {radius, 0.0D}
            };
            for (double[] point : points) {
                Vec3 offset = localOffset(point[0], point[1], yaw);
                server.sendParticles(ParticleTypes.SQUID_INK,
                        h3ChasePosition.x + offset.x,
                        h3ChasePosition.y + up,
                        h3ChasePosition.z + offset.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        for (int arm = 0; arm < 4; arm++) {
            double angle = Math.toRadians(yaw + arm * 90.0F);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    h3ChasePosition.x + Math.sin(angle) * 16.0D,
                    h3ChasePosition.y + 0.1D,
                    h3ChasePosition.z + Math.cos(angle) * 16.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private static Vec3 localOffset(double left, double forward, float yaw) {
        Vec3 facing = Vec3.directionFromRotation(0.0F, yaw)
                .multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 localLeft = facing.yRot((float) (Math.PI / 2.0D));
        return localLeft.scale(left).add(facing.scale(forward));
    }

    private void tickH3Chain(ServerLevel server) {
        NightfallChainBladeEntity blade = h3Blade(server);
        if (blade == null) {
            return;
        }
        Vec3 bladePoint = h3BladePosition.add(0.0D, 0.7D, 0.0D);
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) {
                continue;
            }
            Vec3 playerPoint = player.position().add(0.0D, 1.0D, 0.0D);
            Vec3 chain = bladePoint.subtract(playerPoint);
            double length = chain.length();
            if (length > 1.0E-6D) {
                Vec3 direction = chain.scale(1.0D / length);
                for (double distance = 0.0D; distance <= length; distance += 1.0D) {
                    Vec3 point = playerPoint.add(direction.scale(distance));
                    server.sendParticles(ParticleTypes.END_ROD,
                            point.x, point.y, point.z,
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }
            pullH3Player(player, h3BladePosition);
        }
    }

    private static void pullH3Player(ServerPlayer player, Vec3 bladePosition) {
        double[] thresholds = {10.0D, 6.0D, 4.0D};
        double[] distances = {1.0D, 1.0D, 0.25D};
        for (int index = 0; index < thresholds.length; index++) {
            Vec3 toward = bladePosition.subtract(player.position());
            double length = toward.length();
            if (length < thresholds[index] || length < 1.0E-6D) {
                continue;
            }
            Vec3 destination = player.position().add(
                    toward.scale(Math.min(distances[index], length) / length));
            player.teleportTo(destination.x, destination.y, destination.z);
        }
    }

    private void playH3ChainStart(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator()) {
                continue;
            }
            BlockPos position = player.blockPosition();
            server.playSound(null, position, SoundEvents.CHAIN_PLACE,
                    SoundSource.MASTER, 3.0F, 0.4F);
            server.playSound(null, position, SoundEvents.CHAIN_STEP,
                    SoundSource.MASTER, 3.0F, 0.7F);
            server.playSound(null, position, SoundEvents.TRIDENT_RETURN,
                    SoundSource.MASTER, 3.0F, 0.4F);
        }
    }

    private void triggerH3WarningEnd(ServerLevel server) {
        server.playSound(null, BlockPos.containing(h3ChasePosition),
                SoundEvents.WITHER_SHOOT, SoundSource.MASTER, 4.0F, 0.0F);
        server.playSound(null, BlockPos.containing(h3ChasePosition),
                SoundEvents.TOTEM_USE, SoundSource.MASTER, 4.0F, 1.4F);
        sendH3Dialogue(server);
        shatterH3Blade(server);
    }

    private void sendH3Dialogue(ServerLevel server) {
        int choice = server.random.nextInt(5);
        if (choice < 3) {
            Component message = Component.translatable("dialogue.finalparadox.marawthar.speaker")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.ITALIC)
                    .append(Component.translatable(
                            "dialogue.finalparadox.marawthar.h3." + (choice + 1)));
            for (ServerPlayer player : server.players()) {
                player.sendSystemMessage(message);
            }
        }
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(SoundEvents.RAVAGER_CELEBRATE,
                    SoundSource.MASTER, 0.5F, 0.9F);
            player.playNotifySound(SoundEvents.ELDER_GUARDIAN_AMBIENT,
                    SoundSource.MASTER, 0.4F, 0.9F);
        }
    }

    private void shatterH3Blade(ServerLevel server) {
        NightfallChainBladeEntity blade = h3Blade(server);
        if (blade == null) {
            return;
        }
        Vec3 position = h3BladePosition;
        server.sendParticles(ParticleTypes.EXPLOSION,
                position.x, position.y, position.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.FLASH,
                position.x, position.y + 0.2D, position.z,
                2, 0.0D, 0.0D, 0.0D, 1.0D);
        server.sendParticles(new ItemParticleOption(
                        ParticleTypes.ITEM, new ItemStack(Items.SNOW)),
                position.x, position.y + 0.2D, position.z,
                100, 0.0D, 0.0D, 0.0D, 0.4D);
        server.sendParticles(ParticleTypes.CLOUD,
                position.x, position.y, position.z,
                20, 0.0D, 0.0D, 0.0D, 0.4D);
        server.playSound(null, BlockPos.containing(position),
                SoundEvents.GENERIC_EXPLODE, SoundSource.MASTER, 2.0F, 1.5F);
        server.playSound(null, BlockPos.containing(position),
                SoundEvents.GLASS_BREAK, SoundSource.MASTER, 2.0F, 0.8F);
        blade.discard();
        h3BladeUuid = null;
        h3BladePosition = Vec3.ZERO;
    }

    private void triggerH3Damage(ServerLevel server) {
        for (int index = 1; index <= 90; index++) {
            double angle = Math.toRadians(index * 4.0D);
            server.sendParticles(ParticleTypes.EXPLOSION,
                    h3ChasePosition.x + Math.sin(angle) * 28.0D,
                    h3ChasePosition.y,
                    h3ChasePosition.z + Math.cos(angle) * 28.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || !player.isAlive()
                    || player.position().distanceToSqr(h3ChasePosition) > 28.0D * 28.0D) {
                continue;
            }
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.addEffect(new MobEffectInstance(
                    MobEffects.HARM, 20, 4, false, false));
        }
    }

    private void drawH3InnerBurst(ServerLevel server) {
        for (int index = 0; index < 180; index++) {
            double angle = Mth.TWO_PI * index / 180.0D;
            double x = Math.sin(angle) * 8.0D;
            double z = Math.cos(angle) * 8.0D;
            server.sendParticles(ParticleTypes.SQUID_INK,
                    h3ChasePosition.x, h3ChasePosition.y, h3ChasePosition.z,
                    0, x, 0.0D, z, 0.5D);
        }
    }

    private void drawH3OuterBurst(ServerLevel server) {
        for (int index = 0; index < 180; index++) {
            double angle = Mth.TWO_PI * index / 180.0D;
            server.sendParticles(ParticleTypes.SQUID_INK,
                    h3ChasePosition.x + Math.sin(angle) * 28.0D,
                    h3ChasePosition.y,
                    h3ChasePosition.z + Math.cos(angle) * 28.0D,
                    0, 0.0D, 1.2D, 0.0D, 1.0D);
        }
    }

    private void tickH3BladeAura(ServerLevel server) {
        NightfallChainBladeEntity blade = h3Blade(server);
        if (blade == null || (combatState == COMBAT_H3_PLANT && combatTick < 15)) {
            return;
        }
        float yaw = (tickCount * 8.0F) % 360.0F;
        if (server.random.nextInt(10) == 0) {
            server.sendParticles(H3_BLACK_DUST,
                    h3BladePosition.x, h3BladePosition.y + 0.1D, h3BladePosition.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        for (int arm = 0; arm < 4; arm++) {
            double angle = Math.toRadians(yaw + arm * 90.0F);
            server.sendParticles(H3_WHITE_DUST,
                    h3BladePosition.x + Math.sin(angle) * 4.0D,
                    h3BladePosition.y + 0.1D,
                    h3BladePosition.z + Math.cos(angle) * 4.0D,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    @Nullable
    private ServerPlayer h3Target(ServerLevel server) {
        if (h3TargetUuid != null) {
            Entity entity = server.getEntity(h3TargetUuid);
            if (entity instanceof ServerPlayer player && player.isAlive()
                    && !player.isSpectator()) {
                return player;
            }
        }
        ServerPlayer target = chooseTarget(server);
        h3TargetUuid = target == null ? null : target.getUUID();
        return target;
    }

    @Nullable
    private NightfallChainBladeEntity h3Blade(ServerLevel server) {
        if (h3BladeUuid == null) {
            return null;
        }
        Entity entity = server.getEntity(h3BladeUuid);
        if (entity instanceof NightfallChainBladeEntity blade
                && blade.isBossOwnedBy(getUUID())) {
            return blade;
        }
        return null;
    }

    private void discardH3Blade() {
        if (level() instanceof ServerLevel server) {
            NightfallChainBladeEntity blade = h3Blade(server);
            if (blade != null) {
                blade.discard();
            }
        }
        h3BladeUuid = null;
        h3BladePosition = Vec3.ZERO;
    }

    private void lookAtPoint(Vec3 target) {
        Vec3 direction = target.subtract(position());
        float yaw = yawFromDirection(direction);
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        yBodyRotO = yaw;
    }

    private void beginH4Judgment(ServerLevel server) {
        h4Meteors.clear();
        sourceLockAction = MarawTharChoreography.Action.JUDGMENT;
        sourceLockDuration = JUDGMENT_DURATION;
        beginScriptedAction(COMBAT_H4_JUDGMENT, null);

        Vec3 origin = position();
        Vec3 destination = judgmentPlatformPosition();
        drawSourceTeleportBurst(server, origin);
        drawH4TeleportTrail(server, origin, destination);
        moveTo(destination.x, destination.y, destination.z, getYRot(), 0.0F);
        lookAtPoint(encounterCenter.add(0.0D, 1.62D, 0.0D));
        drawSourceTeleportBurst(server, destination);
        setInvisible(false);
        setAnimation(ANIMATION_H4_JUDGMENT, 0);
    }

    private Vec3 judgmentPlatformPosition() {
        return encounterCenter.add(21.0D, 6.0D, 0.0D);
    }

    private void tickH4Judgment(ServerLevel server) {
        setAnimation(ANIMATION_H4_JUDGMENT, combatTick);
        tickH4Meteors(server);
        if (combatTick == 1) {
            drawSourceTeleportBurst(server, position());
        }
        for (int warningTick : H4_METEOR_WARNING_TICKS) {
            if (combatTick == warningTick) {
                warnH4Meteor(server);
                break;
            }
        }
        if (combatTick == 116) {
            drawSourceTeleportBurst(server, position());
            setInvisible(true);
            drawSourceTeleportBurst(server, position());
            h4Meteors.clear();
        }
        if (combatTick >= JUDGMENT_DURATION) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
            sourceLockDuration = 0;
            finishChoreographyAction(server);
        }
    }

    private void warnH4Meteor(ServerLevel server) {
        Vec3 target = selectH4MeteorTarget(server);
        drawH4MeteorWarning(server, target);
        h4Meteors.add(new H4Meteor(target));
    }

    private Vec3 selectH4MeteorTarget(ServerLevel server) {
        List<Vec3> candidates = new ArrayList<>(76);
        addH4CandidateRing(server, candidates, 5.0D, 0, 330, 30, 15.0D);
        addH4Candidate(server, candidates, 5.0D, 0.0D, 15.0D);
        candidates.add(encounterCenter);
        addH4CandidateRing(server, candidates, 11.0D, 0, 345, 15, 7.5D);
        addH4Candidate(server, candidates, 11.0D, 0.0D, 7.5D);
        addH4CandidateRing(server, candidates, 15.0D, 0, 360, 10, 5.0D);

        List<Vec3> valid = new ArrayList<>(candidates.size());
        for (Vec3 candidate : candidates) {
            boolean allowed = candidate.distanceToSqr(position()) > 25.0D;
            if (allowed) {
                for (ServerPlayer player : server.players()) {
                    if (!player.isSpectator()
                            && candidate.distanceToSqr(player.position()) <= 25.0D) {
                        allowed = false;
                        break;
                    }
                }
            }
            if (allowed) {
                valid.add(candidate);
            }
        }
        List<Vec3> pool = valid.isEmpty() ? candidates : valid;
        return pool.get(server.random.nextInt(pool.size()));
    }

    private void addH4CandidateRing(ServerLevel server, List<Vec3> output, double radius,
                                    int firstAngle, int lastAngle, int step, double randomOffset) {
        for (int angle = firstAngle; angle <= lastAngle; angle += step) {
            addH4Candidate(server, output, radius, angle, randomOffset);
        }
    }

    private void addH4Candidate(ServerLevel server, List<Vec3> output, double radius,
                                double angle, double randomOffset) {
        double adjusted = angle + (server.random.nextBoolean() ? randomOffset : 0.0D);
        double radians = Math.toRadians(adjusted);
        output.add(new Vec3(
                encounterCenter.x - Math.sin(radians) * radius,
                encounterCenter.y,
                encounterCenter.z + Math.cos(radians) * radius));
    }

    private void drawH4MeteorWarning(ServerLevel server, Vec3 center) {
        for (int index = 1; index <= 64; index++) {
            double angle = Mth.TWO_PI * index / 64.0D;
            double x = Math.sin(angle) * 20.0D;
            double z = Math.cos(angle) * 20.0D;
            server.sendParticles(ParticleTypes.SQUID_INK,
                    center.x + x, center.y + 0.4D, center.z + z,
                    0, -x * 0.4D, 0.0D, -z * 0.4D, 0.3D);
        }
        BlockPos soundPosition = BlockPos.containing(center);
        server.playSound(null, soundPosition, SoundEvents.VEX_CHARGE,
                SoundSource.MASTER, 2.0F, 1.2F);
        server.playSound(null, soundPosition, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.MASTER, 2.0F, 0.7F);
    }

    private void tickH4Meteors(ServerLevel server) {
        for (int index = h4Meteors.size() - 1; index >= 0; index--) {
            H4Meteor meteor = h4Meteors.get(index);
            if (!meteor.falling) {
                meteor.warningAge++;
                if (meteor.warningAge >= 5) {
                    meteor.falling = true;
                    meteor.position = meteor.target.add(16.0D, 32.0D, 0.0D);
                }
                continue;
            }

            meteor.fallAge++;
            server.sendParticles(ParticleTypes.EXPLOSION,
                    meteor.position.x, meteor.position.y, meteor.position.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            for (int step = 0; step < 3; step++) {
                stepH4Meteor(server, meteor);
            }
            server.sendParticles(ParticleTypes.EXPLOSION,
                    meteor.position.x, meteor.position.y, meteor.position.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            for (int step = 0; step < 3; step++) {
                stepH4Meteor(server, meteor);
            }
            if (meteor.fallAge >= 11) {
                explodeH4Meteor(server, meteor.position.add(0.4D, 1.3D, 0.0D));
                h4Meteors.remove(index);
            }
        }
    }

    private void stepH4Meteor(ServerLevel server, H4Meteor meteor) {
        server.sendParticles(ParticleTypes.SQUID_INK,
                meteor.position.x, meteor.position.y, meteor.position.z,
                3, 0.2D, 0.2D, 0.2D, 0.0D);
        if (server.random.nextBoolean()) {
            server.sendParticles(ParticleTypes.FLAME,
                    meteor.position.x, meteor.position.y, meteor.position.z,
                    0, 0.0D, 1.0D, 0.0D, 0.0D);
        }
        if (server.random.nextBoolean()) {
            server.sendParticles(ParticleTypes.FLAME,
                    meteor.position.x, meteor.position.y, meteor.position.z,
                    1, 0.2D, 0.2D, 0.2D, 0.0D);
        }
        meteor.position = meteor.position.add(-0.25D, -0.5D, 0.0D);
    }

    private void explodeH4Meteor(ServerLevel server, Vec3 impact) {
        for (int index = 1; index <= 24; index++) {
            double angle = Mth.TWO_PI * index / 24.0D;
            server.sendParticles(ParticleTypes.SQUID_INK,
                    impact.x, impact.y, impact.z,
                    0, Math.sin(angle), 0.0D, Math.cos(angle), 1.2D);
        }
        server.sendParticles(ParticleTypes.EXPLOSION,
                impact.x, impact.y, impact.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(ParticleTypes.SQUID_INK,
                impact.x, impact.y, impact.z,
                40, 0.0D, 0.0D, 0.0D, 0.6D);
        server.sendParticles(ParticleTypes.LAVA,
                impact.x, impact.y, impact.z,
                2, 0.0D, 0.0D, 0.0D, 0.0D);
        server.playSound(null, BlockPos.containing(impact), SoundEvents.GENERIC_EXPLODE,
                SoundSource.MASTER, 2.0F, 0.8F);

        Vec3 projectileOrigin = impact.add(0.0D, 0.5D, 0.0D);
        for (int index = 1; index <= 24; index++) {
            float yaw = index * 15.0F;
            Vec3 direction = Vec3.directionFromRotation(0.0F, yaw)
                    .multiply(1.0D, 0.0D, 1.0D).normalize();
            Vec3 warningOrigin = projectileOrigin.add(0.0D, 0.5D, 0.0D);
            for (int distance = 1; distance <= 35; distance++) {
                Vec3 point = warningOrigin.add(direction.scale(distance));
                server.sendParticles(ParticleTypes.SQUID_INK,
                        point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            spawnSmallLaserProjectile(server, projectileOrigin, direction, true, true);
        }
    }

    private void drawH4TeleportTrail(ServerLevel server, Vec3 origin, Vec3 destination) {
        if (origin.distanceToSqr(destination) <= 1.0E-12D) {
            return;
        }

        Vec3 point = origin;
        for (int index = 0; index < 100; index++) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    point.x, point.y + 1.0D, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(H4_TRAIL_RED_DUST,
                    point.x, point.y + 1.0D, point.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);

            Vec3 remaining = destination.subtract(point);
            if (remaining.lengthSqr() <= 1.0E-12D) {
                break;
            }
            point = point.add(remaining.normalize().scale(0.6D));
            if (point.distanceTo(destination) <= 1.0D) {
                break;
            }
        }
    }

    private void drawSourceTeleportBurst(ServerLevel server, Vec3 center) {
        server.sendParticles(ParticleTypes.SQUID_INK,
                center.x, center.y + 1.2D, center.z,
                15, 0.3D, 0.8D, 0.3D, 0.0D);
        server.playSound(null, BlockPos.containing(center), SoundEvents.SHULKER_TELEPORT,
                SoundSource.MASTER, 2.0F, 1.2F);
        for (int index = 1; index <= 12; index++) {
            double angle = Math.toRadians(90.0D - index * 30.0D);
            double horizontal = Math.sin(angle);
            double vertical = Math.cos(angle);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    center.x + horizontal * 2.5D,
                    center.y + 1.4D + vertical * 2.5D,
                    center.z,
                    0, -horizontal, -vertical, 0.0D, 0.45D);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    center.x,
                    center.y + 1.4D + vertical * 2.5D,
                    center.z + horizontal * 2.5D,
                    0, 0.0D, -vertical, -horizontal, 0.45D);
        }
    }

    private void tickEnrageSlash(ServerLevel server) {
        ServerPlayer target = lockedTarget(server);
        if (target == null) {
            beginChoreographyWait();
            return;
        }
        if (combatTick <= 0) {
            setAnimation(ANIMATION_IDLE, 0);
            return;
        }
        if (combatTick == 1) {
            teleportAround(target, 4.0D, 10.0D);
            clearSlashHits();
        } else {
            lookAt(target);
        }
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                getX(), getY() + 1.3D, getZ(),
                1, 0.1D, 0.5D, 0.1D, 0.1D);
        server.sendParticles(ParticleTypes.SOUL,
                getX(), getY() + 1.3D, getZ(),
                1, 0.1D, 0.1D, 0.1D, 0.1D);
        int localScore = Mth.clamp(combatTick, 1, 38);
        setAnimation(ANIMATION_TRIPLE_SLASH, localScore);
        if (localScore == 8 || localScore == 18 || localScore == 26) {
            stepTowardTarget(target, 1.0D);
        }
        int frameIndex = comboFrameIndex(localScore);
        if (frameIndex >= 0) {
            drawAndDamageSlashFrame(server, frameIndex, frameIndex / 3);
        }
        if (combatTick >= ENRAGE_SLASH_DURATION) {
            finishChoreographyAction(server);
        }
    }

    private void tickNaturalPose() {
        if (hurtAnimationTicks > 0) {
            setAnimation(ANIMATION_HURT, HURT_DURATION - hurtAnimationTicks);
            hurtAnimationTicks--;
            return;
        }
        double horizontalSpeed = getDeltaMovement().horizontalDistanceSqr();
        setAnimation(horizontalSpeed > 0.0004D ? ANIMATION_WALK : ANIMATION_IDLE,
                tickCount % 28);
    }

    private void beginTripleSlash(ServerPlayer target) {
        beginScriptedAction(COMBAT_TRIPLE_SLASH, target);
        setAnimation(ANIMATION_TRIPLE_SLASH, 0);
    }

    private void beginLaserCombo(ServerPlayer target) {
        Arrays.fill(blueVolleyLockedYaws, 0.0F);
        Arrays.fill(blueVolleyYawLocked, false);
        beginScriptedAction(COMBAT_LASER_COMBO, target);
        setAnimation(ANIMATION_LASER_COMBO, 0);
    }

    private void beginOpening(@Nullable ServerPlayer target, boolean transitionOpening) {
        combatState = COMBAT_COUNTER_WINDOW;
        combatTick = 0;
        openingWasHit = false;
        openingEndTick = COUNTER_WINDOW_DURATION;
        phaseTransitionOpening = transitionOpening;
        lockedTarget = null;
        setNoAi(false);
        setNoGravity(false);
        setInvulnerable(false);
        setVulnerable(true);
        if (target != null) {
            teleportAround(target, transitionOpening ? 10.0D : 5.0D, 180.0D);
            setTarget(target);
        }
        setAnimation(ANIMATION_IDLE, 0);
    }

    private void beginTransitionOpening(@Nullable ServerPlayer target) {
        beginOpening(target, true);
    }

    private void beginDashCombo(ServerPlayer target) {
        if (level() instanceof ServerLevel server) {
            discardH5Echoes(server);
        }
        h5DashDirection = Vec3.ZERO;
        h5HitPlayers.clear();
        beginScriptedAction(COMBAT_DASH_COMBO, target);
        setAnimation(ANIMATION_DASH, 0);
    }

    private void beginBlueRush(@Nullable ServerPlayer target) {
        blueRushTrailAge = -1;
        blueRushTrailPosition = Vec3.ZERO;
        blueRushTrailDirection = Vec3.ZERO;
        blueRushTrailHitPlayers.clear();
        beginScriptedAction(COMBAT_BLUE_RUSH, target);
        setAnimation(ANIMATION_BLUE_RUSH, 0);
    }

    private void beginChoreographyWait() {
        combatState = COMBAT_WAITING_FOR_TARGET;
        combatTick = 0;
        lockedTarget = null;
        setNoGravity(false);
        setAnimation(ANIMATION_IDLE, 0);
    }

    private void finishChoreographyAction(ServerLevel server) {
        beginChoreographyWait();
        if (tickCount % 20 == 0) {
            ServerPlayer target = chooseTarget(server);
            if (target != null) {
                setTarget(target);
                beginChoreographyAction(target);
            }
        }
    }

    private void beginChoreographyAction(ServerPlayer target) {
        MarawTharChoreography.Action action =
                MarawTharChoreography.actionFor(getPhase(), getChoreographyStep());
        int advance = MarawTharChoreography.advanceAtStart(action);
        if (advance != 0) {
            setChoreographyStep(getChoreographyStep() + advance);
        }
        switch (action) {
            case PUT_SWORD -> beginH3Plant((ServerLevel) level());
            case RANDOM_PHASE_ONE -> beginRandomAttack(target, false);
            case RANDOM_PHASE_TWO -> beginRandomAttack(target, true);
            case OPENING -> beginOpening(target, false);
            case BEGONE -> beginBlueRush(target);
            case DARKNESS -> beginH3Darkness((ServerLevel) level());
            case JUDGMENT -> beginH4Judgment((ServerLevel) level());
            case INTERMISSION -> beginPhaseIntermission();
            case ENRAGE -> beginEnrageSlash(target);
            default -> beginChoreographyWait();
        }
    }

    private void beginRandomAttack(ServerPlayer target, boolean phaseTwoSet) {
        if (randomBagIndex >= randomAttackBag.length) {
            beginChoreographyWait();
            return;
        }
        int attack = randomAttackBag[randomBagIndex++];
        if (attack == ATTACK_DASH_COMBO) {
            beginDashCombo(target);
        } else if (!phaseTwoSet && attack == ATTACK_TRIPLE_SLASH) {
            beginTripleSlash(target);
        } else if (!phaseTwoSet && attack == ATTACK_LASER_COMBO) {
            beginLaserCombo(target);
        } else if (attack == ATTACK_TRIPLE_SLASH) {
            beginPhaseTwoSwords(target);
        } else {
            beginPhaseTwoLasers(target);
        }
    }

    private void beginPhaseTwoSwords(ServerPlayer target) {
        sourceLockAction = MarawTharChoreography.Action.SWORDS_PHASE_TWO;
        sourceLockDuration = PHASE_TWO_SWORDS_DURATION;
        beginScriptedAction(COMBAT_PHASE_TWO_SWORDS, target);
        setAnimation(ANIMATION_TRIPLE_SLASH, 0);
    }

    private void beginPhaseTwoLasers(ServerPlayer target) {
        sourceLockAction = MarawTharChoreography.Action.LASERS_PHASE_TWO;
        sourceLockDuration = PHASE_TWO_LASERS_DURATION;
        beginScriptedAction(COMBAT_PHASE_TWO_LASERS, target);
        setAnimation(ANIMATION_LASER_COMBO, 0);
    }

    private void beginSourceLock(MarawTharChoreography.Action action, int duration) {
        sourceLockAction = action;
        sourceLockDuration = duration;
        beginScriptedAction(COMBAT_SOURCE_LOCK, null);
        setAnimation(ANIMATION_IDLE, 0);
    }

    private void beginPhaseIntermission() {
        if (level() instanceof ServerLevel server) {
            if (!h6AlteredBlocks.isEmpty()) {
                restoreH6Floor(server, 3);
            }
            captureH6Floor(server);
            clearH9(server, false);
        }
        clearH7Projectiles();
        setChoreographyStep(999);
        sourceLockAction = MarawTharChoreography.Action.INTERMISSION;
        sourceLockDuration = PHASE_INTERMISSION_DURATION;
        beginScriptedAction(COMBAT_PHASE_INTERMISSION, null);
        entityData.set(DATA_TRANSITION_VISUAL_TICK, 0);
        moveTo(encounterCenter.x, encounterCenter.y, encounterCenter.z, getYRot(), 0.0F);
        if (level() instanceof ServerLevel server) {
            for (ServerPlayer player : server.players()) {
                Vec3 delta = encounterCenter.subtract(player.position());
                float yaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
                player.connection.teleport(
                        player.getX(), player.getY(), player.getZ(), yaw, 0.0F);
            }
        }
        setAnimation(ANIMATION_IDLE, 0);
    }

    private void beginEnrageSlash(ServerPlayer target) {
        boolean firstEnrage = !isEnraged();
        setEnraged(true);
        beginScriptedAction(COMBAT_ENRAGE_SLASH, target);
        if (firstEnrage && level() instanceof ServerLevel server) {
            combatTick = 1 - ENRAGE_INTRO_DURATION;
            showEnrageAnnouncement(server);
        }
        setAnimation(firstEnrage ? ANIMATION_IDLE : ANIMATION_TRIPLE_SLASH, 0);
    }

    private void resetRandomAttackBag(ServerLevel server) {
        randomAttackBag = new int[]{ATTACK_TRIPLE_SLASH, ATTACK_DASH_COMBO, ATTACK_LASER_COMBO};
        for (int index = randomAttackBag.length - 1; index > 0; index--) {
            int swap = server.random.nextInt(index + 1);
            int value = randomAttackBag[index];
            randomAttackBag[index] = randomAttackBag[swap];
            randomAttackBag[swap] = value;
        }
        randomBagIndex = 0;
        openingWasHit = false;
    }

    private void beginScriptedAction(int state, @Nullable ServerPlayer target) {
        combatState = state;
        combatTick = 0;
        if (state != COMBAT_PHASE_INTERMISSION) {
            entityData.set(DATA_TRANSITION_VISUAL_TICK, -1);
        }
        lockedTarget = target == null ? null : target.getUUID();
        setNoAi(true);
        setNoGravity(true);
        setInvulnerable(true);
        setVulnerable(false);
        setInvisible(false);
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        if (target != null) {
            setTarget(target);
            lookAt(target);
        }
    }

    @Nullable
    private ServerPlayer chooseTarget(ServerLevel server) {
        List<ServerPlayer> candidates = new ArrayList<>();
        for (ServerPlayer player : server.players()) {
            if (!player.isSpectator() && player.isAlive()) {
                candidates.add(player);
            }
        }
        return candidates.isEmpty() ? null : candidates.get(server.random.nextInt(candidates.size()));
    }

    @Nullable
    private ServerPlayer lockedTarget(ServerLevel server) {
        if (lockedTarget != null) {
            Entity raw = server.getEntity(lockedTarget);
            if (raw instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()) {
                return player;
            }
        }
        ServerPlayer replacement = chooseTarget(server);
        lockedTarget = replacement == null ? null : replacement.getUUID();
        return replacement;
    }

    private void teleportAround(ServerPlayer target, double radius, double angleOffsetDegrees) {
        double angle = Math.toRadians(target.getYRot() + angleOffsetDegrees);
        double x = target.getX() + Math.sin(angle) * radius;
        double z = target.getZ() + Math.cos(angle) * radius;
        moveTo(x, target.getY(), z, getYRot(), 0.0F);
        setDeltaMovement(Vec3.ZERO);
        lookAt(target);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    getX(), getY() + 1.0D, getZ(),
                    12, 0.25D, 0.45D, 0.25D, 0.12D);
            server.playSound(null, blockPosition(), SoundEvents.ENDER_EYE_DEATH,
                    SoundSource.HOSTILE, 0.8F, 1.25F);
        }
    }

    private void lookAt(LivingEntity target) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        setYRot(yaw);
        setYHeadRot(yaw);
        yBodyRot = yaw;
        yBodyRotO = yaw;
    }

    private static int latestStartedSequence(int[] starts, int elapsed) {
        for (int index = starts.length - 1; index >= 0; index--) {
            if (elapsed >= starts[index]) return index;
        }
        return 0;
    }

    private static int comboFrameIndex(int score) {
        if (score >= 12 && score <= 14) return score - 12;
        if (score >= 20 && score <= 22) return 3 + score - 20;
        if (score >= 30 && score <= 32) return 6 + score - 30;
        return -1;
    }

    private void clearSlashHits() {
        for (Set<UUID> targets : slashHitTargets) targets.clear();
    }

    private void drawAndDamageSlashFrame(ServerLevel server, int frameIndex, int strikeIndex) {
        boolean blueAttack = isEnraged();
        DustParticleOptions[] slashDust =
                blueAttack ? ENRAGE_SLASH_DUST : SLASH_DUST;
        float baseYaw = getYRot();
        Vec3 forward = Vec3.directionFromRotation(0.0F, baseYaw)
                .multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 left = forward.yRot((float) (Math.PI / 2.0D));
        Vec3 origin = position();
        List<Vec3> hitCenters = new ArrayList<>();
        for (NightfallSlashFrames.Sample sample : NightfallSlashFrames.FRAMES[frameIndex]) {
            Vec3 base = origin.add(left.scale(sample.left()))
                    .add(0.0D, sample.up(), 0.0D)
                    .add(forward.scale(sample.forward()));
            Vec3 ray = Vec3.directionFromRotation(sample.pitch(), baseYaw + sample.yaw());
            for (int index = 0; index < SLASH_PARTICLE_DISTANCES.length; index++) {
                Vec3 point = base.add(ray.scale(SLASH_PARTICLE_DISTANCES[index]));
                server.sendParticles(slashDust[index], point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            Vec3 tip = base.add(ray.scale(3.0D));
            server.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z,
                    0, 0.0D, -999999.0D, 0.0D, 1.0D);
            hitCenters.add(base.add(0.0D, -1.0D, 0.0D).add(ray.scale(2.0D)));
        }

        Set<UUID> alreadyHit = slashHitTargets.get(strikeIndex);
        double hitRadiusSqr = blueAttack ? 9.0D : 6.25D;
        for (ServerPlayer player : server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(8.0D, 5.0D, 8.0D),
                candidate -> candidate.isAlive() && !candidate.isSpectator()
                        && !alreadyHit.contains(candidate.getUUID()))) {
            boolean inside = hitCenters.stream()
                    .anyMatch(center -> player.position().distanceToSqr(center) <= hitRadiusSqr);
            if (!inside) continue;
            alreadyHit.add(player.getUUID());
            if (blueAttack) {
                damageBluePlayer(server, player);
            } else {
                damageRedPlayer(player, origin, RED_ATTACK_DAMAGE);
            }
        }
    }

    private void drawAndDamageH1VariantFrame(
            ServerLevel server, MarawTharH1VariantFrames.Sample[] samples, int strikeIndex) {
        float baseYaw = getYRot();
        Vec3 forward = Vec3.directionFromRotation(0.0F, baseYaw)
                .multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 left = forward.yRot((float) (Math.PI / 2.0D));
        Vec3 origin = position();
        List<Vec3> hitCenters = new ArrayList<>(samples.length);
        for (MarawTharH1VariantFrames.Sample sample : samples) {
            Vec3 base = origin.add(left.scale(sample.left()))
                    .add(0.0D, sample.up(), 0.0D)
                    .add(forward.scale(sample.forward()));
            Vec3 ray = Vec3.directionFromRotation(sample.pitch(), baseYaw + sample.yaw());
            for (int index = 0; index < H1_VARIANT_PARTICLE_DISTANCES.length; index++) {
                Vec3 point = base.add(ray.scale(H1_VARIANT_PARTICLE_DISTANCES[index]));
                server.sendParticles(SLASH_DUST[index], point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            Vec3 tip = base.add(ray.scale(3.6D));
            server.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z,
                    0, 0.0D, -999999.0D, 0.0D, 1.0D);
            if (server.random.nextInt(20) == 0) {
                server.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z,
                        1, 0.0D, 0.0D, 0.0D, 0.1D);
            }
            hitCenters.add(base.add(0.0D, -1.0D, 0.0D).add(ray.scale(2.5D)));
        }

        Set<UUID> alreadyHit = slashHitTargets.get(strikeIndex);
        for (ServerPlayer player : server.getEntitiesOfClass(
                ServerPlayer.class, getBoundingBox().inflate(10.0D, 6.0D, 10.0D),
                candidate -> candidate.isAlive() && !candidate.isSpectator()
                        && !alreadyHit.contains(candidate.getUUID()))) {
            boolean inside = hitCenters.stream()
                    .anyMatch(center -> player.position().distanceToSqr(center) <= 9.0D);
            if (!inside) continue;
            alreadyHit.add(player.getUUID());
            damageRedPlayer(player, origin, RED_ATTACK_DAMAGE);
        }
    }

    private void stepTowardTarget(ServerPlayer target, double distance) {
        Vec3 toward = target.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
        if (toward.lengthSqr() < 1.0E-6D) return;
        Vec3 movement = toward.normalize().scale(Math.min(distance, Math.sqrt(toward.lengthSqr())));
        Vec3 destination = position().add(movement);
        AABB moved = getBoundingBox().move(destination.subtract(position()));
        if (level().noCollision(this, moved)) {
            moveTo(destination.x, destination.y, destination.z, getYRot(), getXRot());
        }
    }

    private void stepForward(double distance) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot())
                .multiply(1.0D, 0.0D, 1.0D);
        if (forward.lengthSqr() < 1.0E-6D) return;
        Vec3 destination = position().add(forward.normalize().scale(distance));
        AABB moved = getBoundingBox().move(destination.subtract(position()));
        if (level().noCollision(this, moved)) {
            moveTo(destination.x, destination.y, destination.z, getYRot(), getXRot());
        }
    }

    private void tickRedBarrage(ServerLevel server, ServerPlayer target, int elapsed, int start) {
        int localScore = elapsed - start + 1;
        for (int fireScore : RED_BARRAGE_FIRE_SCORES) {
            if (localScore == fireScore) {
                Vec3 direction = horizontalDirection(position(), target.getEyePosition(), getYRot());
                Vec3 origin = position().add(0.0D, 0.5D, 0.0D).add(direction.scale(3.3D));
                spawnSmallLaserProjectile(server, origin, direction, true);
                break;
            }
        }
    }

    private void tickBlueVolley(ServerLevel server, ServerPlayer target, int elapsed,
                                int start, int volleyIndex) {
        int localScore = elapsed - start + 1;
        if (localScore < 1 || localScore > 27
                || volleyIndex < 0 || volleyIndex >= blueVolleyLockedYaws.length) {
            return;
        }
        if (!blueVolleyYawLocked[volleyIndex]) {
            Vec3 baseDirection = horizontalDirection(position(), target.getEyePosition(), getYRot());
            blueVolleyLockedYaws[volleyIndex] = yawFromDirection(baseDirection);
            blueVolleyYawLocked[volleyIndex] = true;
        }
        tickBlueVolleyAtYaw(server, elapsed, start, blueVolleyLockedYaws[volleyIndex]);
    }

    private void tickBlueVolleyAtYaw(ServerLevel server, int elapsed, int start, float baseYaw) {
        int localScore = elapsed - start + 1;
        if (localScore >= 5 && localScore <= 10) {
            int angle = Math.floorMod(120 + (localScore - 5) * 60, 360);
            spawnBlueProjectileAtAngle(server, baseYaw, angle, true);
            return;
        }
        if (localScore < 19 || localScore > 27) return;
        for (int angle = 130; angle <= 470; angle += 10) {
            if (Math.floorMod(angle, 60) == 0) continue;
            int sourceFireScore = 19 + (angle - 120) / 40;
            if (sourceFireScore == localScore) {
                spawnBlueProjectileAtAngle(server, baseYaw, angle, false);
            }
        }
    }

    private void spawnBlueProjectileAtAngle(ServerLevel server, float baseYaw,
                                            int angle, boolean warning) {
        Vec3 direction = Vec3.directionFromRotation(0.0F, baseYaw + angle)
                .multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 origin = position().add(0.0D, 0.5D, 0.0D).add(direction.scale(1.7D));
        if (warning) {
            Vec3 raised = origin.add(0.0D, 0.9D, 0.0D);
            for (int distance = 1; distance <= 45; distance++) {
                Vec3 point = raised.add(direction.scale(distance));
                server.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
        spawnSmallLaserProjectile(server, origin, direction, false);
    }

    private void tickLaserOrbit(ServerLevel server, ServerPlayer target, int localScore) {
        if (localScore < 0 || localScore > 64) return;
        int teleportIndex = orbitTeleportIndex(localScore);
        if (teleportIndex >= 0) {
            Vec3 offset = LASER_ORBIT_OFFSETS[teleportIndex];
            Vec3 inward = offset.scale(-1.0D).normalize();
            Vec3 localLeft = inward.yRot((float) (Math.PI / 2.0D));
            Vec3 destination = target.position().add(offset)
                    .add(inward).add(localLeft.scale(-0.3D));
            moveTo(destination.x, target.getY(), destination.z, getYRot(), 0.0F);
            lookAt(target);
        }

        Vec3 center = target.position().add(0.0D, 0.5D, 0.0D);
        for (int index = 0; index < LASER_ORBIT_OFFSETS.length; index++) {
            int warningStart = ORBIT_WARNING_SCORES[index];
            int warningEnd = index == 0 ? 43 : warningStart + 40;
            Vec3 origin = center.add(LASER_ORBIT_OFFSETS[index]);
            if (localScore >= warningStart && localScore <= warningEnd) {
                server.sendParticles(RED_DUST, origin.x, origin.y, origin.z,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
                server.sendParticles(ParticleTypes.SQUID_INK, origin.x, origin.y, origin.z,
                        0, 0.0D, -1.0D, 0.0D, 999999.0D);
            }
            if (localScore != 43 + index * 3) continue;
            Vec3 direction = horizontalDirection(origin, target.getEyePosition(), getYRot());
            spawnSmallLaserProjectile(server, origin, direction, true);
            server.sendParticles(ParticleTypes.EXPLOSION,
                    origin.x, origin.y + 1.0D, origin.z,
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.sendParticles(ParticleTypes.SQUID_INK,
                    origin.x, origin.y + 1.0D, origin.z,
                    10, 0.0D, 0.0D, 0.0D, 0.4D);
        }
    }

    private static int orbitTeleportIndex(int localScore) {
        if (localScore >= 0 && localScore <= 3) return 0;
        for (int index = 1; index < ORBIT_WARNING_SCORES.length; index++) {
            if (localScore == ORBIT_WARNING_SCORES[index]) return index;
        }
        return localScore >= 27 && localScore <= 40 ? 0 : -1;
    }

    private void spawnSmallLaserProjectile(ServerLevel server, Vec3 origin,
                                           Vec3 direction, boolean red) {
        spawnSmallLaserProjectile(server, origin, direction, red, false);
    }

    private void spawnSmallLaserProjectile(ServerLevel server, Vec3 origin,
                                           Vec3 direction, boolean red, boolean h4Judgment) {
        UUID[] parts = new UUID[3];
        for (int index = 0; index < parts.length; index++) {
            Vec3 partPosition = origin.add(direction.scale(index * 0.35D));
            ArmorStand part = createSmallLaserPart(server, partPosition, direction, red);
            parts[index] = part.getUUID();
        }
        smallLaserProjectiles.add(new SmallLaserProjectile(
                red ? 2 : -3, red, h4Judgment, origin, direction, parts));
        SoundSource source = h4Judgment ? SoundSource.MASTER : SoundSource.HOSTILE;
        server.playSound(null, BlockPos.containing(origin), SoundEvents.ENDER_EYE_DEATH,
                source, 1.0F, 1.4F);
        server.playSound(null, BlockPos.containing(origin), SoundEvents.TRIDENT_RETURN,
                source, 1.5F, 1.7F);
    }

    private ArmorStand createSmallLaserPart(ServerLevel server, Vec3 position,
                                            Vec3 direction, boolean red) {
        ArmorStand part = new ArmorStand(server, position.x, position.y, position.z);
        part.setInvisible(true);
        part.setNoGravity(true);
        part.setInvulnerable(true);
        part.setSilent(true);
        CompoundTag data = new CompoundTag();
        part.saveWithoutId(data);
        data.putBoolean("Marker", true);
        data.putBoolean("Small", false);
        part.load(data);
        part.noPhysics = true;
        part.setGlowingTag(true);
        part.setRightArmPose(new Rotations(345.0F, 225.0F, 0.0F));
        part.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BLACK_CONCRETE));
        part.addTag("finalparadox_marawthar_small_laser");
        part.addTag("finalparadox_marawthar_" + getUUID());
        part.moveTo(position.x, position.y, position.z, yawFromDirection(direction), 0.0F);
        server.addFreshEntity(part);
        if (red) addLaserPartToTeam(server, part, "fp_b9_red", ChatFormatting.RED);
        return part;
    }

    private static void addLaserPartToTeam(ServerLevel server, Entity part,
                                           String name, ChatFormatting color) {
        var team = server.getScoreboard().getPlayerTeam(name);
        if (team == null) team = server.getScoreboard().addPlayerTeam(name);
        team.setColor(color);
        server.getScoreboard().addPlayerToTeam(part.getScoreboardName(), team);
    }

    private void tickSmallLaserProjectiles(ServerLevel server) {
        int now = tickCount;
        projectileHitCooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
        for (int index = smallLaserProjectiles.size() - 1; index >= 0; index--) {
            SmallLaserProjectile projectile = smallLaserProjectiles.get(index);
            projectile.age++;
            if (projectile.age == 4) {
                server.playSound(null, BlockPos.containing(projectile.origin),
                        SoundEvents.PUFFER_FISH_BLOW_OUT,
                        projectile.h4Judgment ? SoundSource.MASTER : SoundSource.HOSTILE,
                        0.4F, 0.9F);
            }
            if (projectile.age == 6) {
                if (!projectile.red) {
                    for (UUID partId : projectile.parts) {
                        Entity part = server.getEntity(partId);
                        if (part != null) addLaserPartToTeam(
                                server, part, "fp_b9_aqua", ChatFormatting.AQUA);
                    }
                }
                projectile.origin = projectile.origin.subtract(projectile.direction.scale(0.2D));
                moveSmallLaserParts(server, projectile);
            }
            if (projectile.age >= 9 && projectile.age <= 20) {
                for (int substep = 0; substep < 4; substep++) {
                    damageSmallLaserStep(server, projectile);
                    projectile.origin = projectile.origin.add(projectile.direction);
                    moveSmallLaserParts(server, projectile);
                }
            }
            if (projectile.age >= 20) {
                discardSmallLaserProjectile(server, projectile);
                smallLaserProjectiles.remove(index);
            }
        }
    }

    private static void moveSmallLaserParts(ServerLevel server, SmallLaserProjectile projectile) {
        for (int index = 0; index < projectile.parts.length; index++) {
            Entity part = server.getEntity(projectile.parts[index]);
            if (part == null) continue;
            Vec3 point = projectile.origin.add(projectile.direction.scale(index * 0.35D));
            part.moveTo(point.x, point.y, point.z, yawFromDirection(projectile.direction), 0.0F);
        }
    }

    private void damageSmallLaserStep(ServerLevel server, SmallLaserProjectile projectile) {
        for (int part = 0; part < projectile.parts.length; part++) {
            Vec3 center = projectile.origin.add(projectile.direction.scale(part * 0.35D))
                    .subtract(projectile.direction);
            AABB hitBox = new AABB(center, center).inflate(1.0D);
            for (ServerPlayer player : server.getEntitiesOfClass(ServerPlayer.class, hitBox,
                    target -> target.isAlive() && !target.isSpectator()
                            && !projectile.hitPlayers.contains(target.getUUID())
                            && projectileHitCooldowns.getOrDefault(
                                    target.getUUID(), 0) <= tickCount)) {
                projectileHitCooldowns.put(player.getUUID(), tickCount + 2);
                boolean damaged;
                if (projectile.red) {
                    damaged = projectile.h4Judgment
                            ? damageH4ProjectilePlayer(player, center)
                            : damageRedPlayer(player, center, RED_ATTACK_DAMAGE);
                } else {
                    damaged = player.hurt(damageSources().magic(), BLUE_ATTACK_DAMAGE);
                }
                if (!damaged) {
                    continue;
                }
                projectile.hitPlayers.add(player.getUUID());
                server.playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_ON_FIRE,
                        projectile.h4Judgment ? SoundSource.MASTER : SoundSource.HOSTILE,
                        1.0F, 1.0F);
                server.sendParticles(ParticleTypes.SQUID_INK,
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        6, 0.0D, 0.0D, 0.0D, 0.2D);
            }
        }
    }

    private boolean damageH4ProjectilePlayer(ServerPlayer player, Vec3 attackOrigin) {
        if (AtacromGauntletItem.tryParryRedAttack(player, attackOrigin)) {
            return false;
        }
        player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        player.addEffect(new MobEffectInstance(MobEffects.HARM, 20, 3, false, false));
        return true;
    }

    private static void discardSmallLaserProjectile(
            ServerLevel server, SmallLaserProjectile projectile) {
        for (UUID partId : projectile.parts) {
            Entity part = server.getEntity(partId);
            if (part != null) part.discard();
        }
    }

    private void discardAllSmallLaserProjectiles() {
        if (level() instanceof ServerLevel server) {
            for (SmallLaserProjectile projectile : smallLaserProjectiles) {
                discardSmallLaserProjectile(server, projectile);
            }
        }
        smallLaserProjectiles.clear();
        projectileHitCooldowns.clear();
    }

    private void cleanupOwnedOrphanedLaserParts(ServerLevel server) {
        String ownerTag = "finalparadox_marawthar_" + getUUID();
        Set<UUID> trackedParts = new HashSet<>();
        for (SmallLaserProjectile projectile : smallLaserProjectiles) {
            trackedParts.addAll(Arrays.asList(projectile.parts));
        }
        trackedParts.addAll(h5EchoUuids);
        Set<UUID> trackedLaserVisuals = new HashSet<>();
        if (h9StartTick >= 0 && h9LaserUuid != null) {
            trackedLaserVisuals.add(h9LaserUuid);
        }
        for (ArmorStand part : server.getEntitiesOfClass(
                ArmorStand.class, getBoundingBox().inflate(96.0D),
                candidate -> candidate.getTags().contains(ownerTag)
                        && !trackedParts.contains(candidate.getUUID()))) {
            part.discard();
        }
        for (NightfallLaserEntity laser : server.getEntitiesOfClass(
                NightfallLaserEntity.class, getBoundingBox().inflate(96.0D),
                candidate -> candidate.isOwnedBy(getUUID())
                        && !trackedLaserVisuals.contains(candidate.getUUID()))) {
            laser.discard();
        }
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, float fallbackYaw) {
        Vec3 direction = to.subtract(from).multiply(1.0D, 0.0D, 1.0D);
        return direction.lengthSqr() < 1.0E-6D
                ? Vec3.directionFromRotation(0.0F, fallbackYaw).normalize()
                : direction.normalize();
    }

    private static float yawFromDirection(Vec3 direction) {
        return (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
    }

    private static DustParticleOptions dust(float red, float green, float blue, float size) {
        return new DustParticleOptions(new Vector3f(red, green, blue), size);
    }

    private boolean damageRedPlayer(ServerPlayer player, Vec3 attackOrigin, float damage) {
        if (AtacromGauntletItem.tryParryRedAttack(player, attackOrigin)) {
            return false;
        }
        return player.hurt(damageSources().magic(), damage);
    }

    private boolean damageBluePlayer(ServerLevel server, ServerPlayer player) {
        if (!player.hurt(damageSources().magic(), BLUE_ATTACK_DAMAGE)) {
            return false;
        }
        player.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN, 60, 1, false, false));
        server.playSound(null, player.blockPosition(),
                SoundEvents.ZOMBIE_ATTACK_IRON_DOOR,
                SoundSource.MASTER, 1.0F, 2.0F);
        server.sendParticles(ParticleTypes.SWEEP_ATTACK,
                player.getX(), player.getY() + 1.0D, player.getZ(),
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        server.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.REDSTONE_BLOCK)),
                player.getX(), player.getY() + 1.0D, player.getZ(),
                20, 0.0D, 0.0D, 0.0D, 0.3D);
        return true;
    }

    private void maintainEncounterPlayerHealth(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            if (player.isAlive() && !player.isSpectator()) {
                applyEncounterPlayerHealth(player);
            }
        }
    }

    private void applyEncounterPlayerHealth(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }
        boolean newlyApplied =
                maxHealth.getModifier(ENCOUNTER_HEALTH_MODIFIER_UUID) == null;
        maxHealth.removeModifier(ENCOUNTER_HEALTH_MODIFIER_UUID);
        double naturalMaximum = maxHealth.getValue();
        if (!Double.isFinite(naturalMaximum) || naturalMaximum <= 0.0D) {
            return;
        }
        double multiplier = ENCOUNTER_PLAYER_MAX_HEALTH / naturalMaximum - 1.0D;
        maxHealth.addTransientModifier(new AttributeModifier(
                ENCOUNTER_HEALTH_MODIFIER_UUID,
                "MarawThar encounter max health",
                multiplier,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
        encounterHealthPlayers.add(player.getUUID());
        if (newlyApplied) {
            player.setHealth((float) ENCOUNTER_PLAYER_MAX_HEALTH);
        } else if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private void restoreEncounterPlayerHealth() {
        if (!(level() instanceof ServerLevel server)) {
            encounterHealthPlayers.clear();
            return;
        }
        for (UUID playerId : new HashSet<>(encounterHealthPlayers)) {
            ServerPlayer player = server.getServer().getPlayerList().getPlayer(playerId);
            if (player != null) {
                restoreEncounterPlayerHealth(player);
            }
        }
        encounterHealthPlayers.clear();
    }

    private void restoreEncounterPlayerHealth(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.removeModifier(ENCOUNTER_HEALTH_MODIFIER_UUID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
        encounterHealthPlayers.remove(player.getUUID());
    }

    private void applySourceResistance() {
        MobEffectInstance resistance = getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance == null || resistance.getAmplifier() < 3 || resistance.getDuration() < 30) {
            addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 3, false, false));
        }
    }

    private void protectPlayersDuringCountdown(ServerLevel server) {
        for (ServerPlayer player : encounterPlayers(server, 96.0D)) {
            if (!player.isSpectator() && player.isAlive()) {
                player.addEffect(new MobEffectInstance(
                        MobEffects.DAMAGE_RESISTANCE, 120, 11, false, false));
            }
        }
    }

    private void playCountdownBell(ServerLevel server) {
        server.playSound(null, BlockPos.containing(encounterCenter),
                SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.MASTER, 1.0F, 1.5F);
    }

    private void startIntroMusic(ServerLevel server) {
        musicState = MUSIC_INTRO;
        musicTick = 0;
        stopAllRecordAudio(server);
        playMusicToAll(server, ModSounds.MARAWTHAR_INTRO.get());
    }

    private void startIntermissionMusic(ServerLevel server) {
        musicState = MUSIC_INTERMISSION;
        musicTick = 0;
        // The source deliberately overlaps the old track for 34 ticks.
        playMusicToAll(server, ModSounds.MARAWTHAR_INTERMISSION.get());
    }

    private void startLoopMusic(ServerLevel server) {
        musicState = MUSIC_LOOP;
        musicTick = 0;
        stopAllRecordAudio(server);
        playMusicToAll(server, ModSounds.MARAWTHAR_LOOP.get());
    }

    private void tickMusic(ServerLevel server) {
        if (musicState == MUSIC_NONE) {
            return;
        }
        musicTick++;
        if (tickCount % 20 == 0) {
            Set<UUID> onlinePlayers = new HashSet<>();
            for (ServerPlayer player : server.players()) {
                onlinePlayers.add(player.getUUID());
            }
            musicListeners.retainAll(onlinePlayers);
        }
        if (musicState == MUSIC_INTRO && musicTick >= MUSIC_INTRO_TICKS) {
            startLoopMusic(server);
        } else if (musicState == MUSIC_INTERMISSION) {
            if (musicTick == MUSIC_INTERMISSION_OLD_TRACK_STOP_TICK) {
                stopOldMarawTharTracks(server);
            }
            if (musicTick >= MUSIC_INTERMISSION_TICKS) {
                startLoopMusic(server);
            }
        } else if (musicState == MUSIC_LOOP && musicTick >= MUSIC_LOOP_TICKS) {
            startLoopMusic(server);
        }
    }

    private void recoverMusic(ServerLevel server) {
        needsMusicRecovery = false;
        musicListeners.clear();
        stopOwnedMusicAudio(server);
        SoundEvent current = currentMusic();
        if (current != null) {
            musicTick = 0;
            playMusicToAll(server, current);
        }
    }

    @Nullable
    private SoundEvent currentMusic() {
        return switch (musicState) {
            case MUSIC_INTRO -> ModSounds.MARAWTHAR_INTRO.get();
            case MUSIC_INTERMISSION -> ModSounds.MARAWTHAR_INTERMISSION.get();
            case MUSIC_LOOP -> ModSounds.MARAWTHAR_LOOP.get();
            default -> null;
        };
    }

    private void playMusicToAll(ServerLevel server, SoundEvent sound) {
        for (ServerPlayer player : server.players()) {
            player.playNotifySound(sound, SoundSource.RECORDS, 1.0F, 1.0F);
            musicListeners.add(player.getUUID());
        }
    }

    private void playCurrentMusicTo(ServerPlayer player) {
        SoundEvent current = currentMusic();
        if (current != null && musicListeners.add(player.getUUID())) {
            player.playNotifySound(current, SoundSource.RECORDS, 1.0F, 1.0F);
        }
    }

    private static void stopAllRecordAudio(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundStopSoundPacket(null, SoundSource.RECORDS));
        }
    }

    private static void stopOldMarawTharTracks(ServerLevel server) {
        ResourceLocation intro = ModSounds.MARAWTHAR_INTRO.get().getLocation();
        ResourceLocation loop = ModSounds.MARAWTHAR_LOOP.get().getLocation();
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundStopSoundPacket(intro, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(loop, SoundSource.RECORDS));
        }
    }

    private static void stopOwnedMusicAudio(ServerLevel server) {
        ResourceLocation intro = ModSounds.MARAWTHAR_INTRO.get().getLocation();
        ResourceLocation intermission = ModSounds.MARAWTHAR_INTERMISSION.get().getLocation();
        ResourceLocation loop = ModSounds.MARAWTHAR_LOOP.get().getLocation();
        ResourceLocation victory = ModSounds.MARAWTHAR_VICTORY.get().getLocation();
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundStopSoundPacket(intro, SoundSource.RECORDS));
            player.connection.send(
                    new ClientboundStopSoundPacket(intermission, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(loop, SoundSource.RECORDS));
            player.connection.send(new ClientboundStopSoundPacket(victory, SoundSource.RECORDS));
        }
    }

    private void stopMarawTharMusic() {
        if (level() instanceof ServerLevel server) {
            stopOwnedMusicAudio(server);
        }
        musicState = MUSIC_NONE;
        musicTick = -1;
        needsMusicRecovery = false;
        musicListeners.clear();
    }

    private void showEncounterTitle(
            Component title,
            Component subtitle,
            int fadeIn,
            int stay,
            int fadeOut
    ) {
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        for (ServerPlayer player : encounterPlayers(server, 96.0D)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
            player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
            player.connection.send(new ClientboundSetTitleTextPacket(title));
        }
    }

    private void showEnrageAnnouncement(ServerLevel server) {
        showEncounterTitle(
                Component.literal("☠").withStyle(ChatFormatting.DARK_RED),
                Component.translatable("dialogue.finalparadox.marawthar.enrage.subtitle"),
                5, 40, 5);
        Component dialogue = Component.translatable("dialogue.finalparadox.marawthar.speaker")
                .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.ITALIC)
                .append(Component.translatable("dialogue.finalparadox.marawthar.enrage.line")
                        .withStyle(ChatFormatting.RED));
        for (ServerPlayer player : encounterPlayers(server, 96.0D)) {
            player.sendSystemMessage(dialogue);
        }
        server.playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_GROWL,
                SoundSource.MASTER, 1.0F, 1.5F);
        server.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR,
                SoundSource.MASTER, 0.5F, 0.9F);
        server.playSound(null, blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                SoundSource.MASTER, 0.4F, 0.9F);
    }

    private List<ServerPlayer> encounterPlayers(ServerLevel server, double radius) {
        double radiusSqr = radius * radius;
        return server.getPlayers(player ->
                player.distanceToSqr(encounterCenter) <= radiusSqr);
    }

    private void returnOutOfBoundsPlayers(ServerLevel server) {
        Vec3 destination = encounterCenter.add(5.0D, 0.0D, -9.0D);
        for (ServerPlayer player : server.players()) {
            if (player.isSpectator() || !player.isAlive()
                    || player.distanceToSqr(encounterCenter) < ARENA_BOUNDARY_RADIUS_SQR) {
                continue;
            }
            player.connection.teleport(
                    destination.x, destination.y, destination.z,
                    -37.0F, 5.0F);
        }
    }

    public static void onPlayerDeath(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel server)) return;
        ArenaDeploymentData data = ArenaDeploymentData.get(server);
        if (!ArenaDefinitions.MARAWTHAR.id().equals(data.arenaId())
                || !data.marawTharTriggered()) {
            return;
        }
        data.activeBossUuid().map(server::getEntity)
                .filter(MarawTharBossEntity.class::isInstance)
                .map(MarawTharBossEntity.class::cast)
                .filter(Entity::isAlive)
                .ifPresent(boss -> boss.handlePlayerDeath(player));
    }

    public void handlePlayerDeath(ServerPlayer player) {
        if (!(level() instanceof ServerLevel server) || isVictoryActive() || defeatActive) {
            return;
        }
        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        }
        Vec3 above = position().add(0.0D, 10.0D, 0.0D);
        player.teleportTo(server, above.x, above.y, above.z, player.getYRot(), player.getXRot());
        if (allPlayersSpectator(server)) {
            startDefeat(server);
        }
    }

    private static boolean allPlayersSpectator(ServerLevel server) {
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
                return false;
            }
        }
        return true;
    }

    private void startDefeat(ServerLevel server) {
        defeatActive = true;
        defeatTicks = 0;
        showEncounterTitle(
                Component.translatable("luisb1202.functions.bossfight.b1.derrota.1"),
                Component.translatable("luisb1202.functions.bossfight.b1.derrota.2"),
                5, 40, 5);
        for (ServerPlayer player : server.players()) {
            server.playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH,
                    SoundSource.MASTER, 1.0F, 1.8F);
        }
    }

    private void tickDefeat(ServerLevel server) {
        defeatTicks++;
        if (defeatTicks == DEFEAT_DIALOGUE_1_TICK) {
            Component line = Component.empty()
                    .append(Component.translatable(
                                    "luisb1202.functions.bossfight.b9.dialogos.frases_h1.1")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xAA0000))
                                    .withBold(true).withItalic(true)))
                    .append(Component.translatable("b9_dialogo_17"));
            for (ServerPlayer player : server.players()) {
                player.sendSystemMessage(line);
            }
            server.playSound(null, blockPosition(), SoundEvents.RAVAGER_ROAR,
                    SoundSource.MASTER, 0.5F, 0.9F);
            server.playSound(null, blockPosition(), SoundEvents.ELDER_GUARDIAN_AMBIENT,
                    SoundSource.MASTER, 0.4F, 0.9F);
        } else if (defeatTicks == DEFEAT_DIALOGUE_2_TICK) {
            Component line = Component.empty()
                    .append(Component.translatable("eothar")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x94E4FF))
                                    .withBold(true).withItalic(true)))
                    .append(Component.translatable("b9_dialogo_19"));
            for (ServerPlayer player : server.players()) {
                player.sendSystemMessage(line);
            }
        }
        if (defeatTicks >= DEFEAT_RESPAWN_TICKS) {
            restartAfterDefeat(server);
        }
    }

    private void restartAfterDefeat(ServerLevel server) {
        defeatActive = false;
        defeatTicks = 0;
        Vec3 destination = encounterCenter.add(5.0D, 0.0D, -9.0D);
        for (ServerPlayer player : server.players()) {
            if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                player.teleportTo(server, destination.x, destination.y, destination.z,
                        -37.0F, 5.0F);
                player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
            }
            player.removeEffect(MobEffects.WITHER);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 2020, 1, false, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.HEAL, 20, 10, true, false));
        }
        ArenaDeploymentData data = ArenaDeploymentData.get(server);
        BlockPos anchor = data.floorAnchor().orElse(
                BlockPos.containing(encounterCenter).offset(0, -1, 0));
        discard();
        MarawTharArenaStaging.spawnBoss(server, data, anchor);
    }

    private void closeArena(ServerLevel server) {
        BlockPos center = BlockPos.containing(encounterCenter);
        Set<BlockPos> columns = arenaBarrierColumns(center);
        for (BlockPos column : columns) {
            for (int yOffset = 0; yOffset < ARENA_BARRIER_HEIGHT; yOffset++) {
                BlockPos position = new BlockPos(column.getX(), center.getY() + yOffset, column.getZ());
                if (!server.isInWorldBounds(position) || !server.getBlockState(position).isAir()) {
                    continue;
                }
                if (server.setBlock(position, Blocks.BARRIER.defaultBlockState(), 3)) {
                    arenaBarrierBlocks.add(position.immutable());
                }
            }
        }
        arenaClosed = true;
    }

    private void openArena(ServerLevel server) {
        for (BlockPos position : arenaBarrierBlocks) {
            if (server.getBlockState(position).is(Blocks.BARRIER)) {
                server.setBlock(position, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        arenaBarrierBlocks.clear();
        arenaClosed = false;
    }

    private static Set<BlockPos> arenaBarrierColumns(BlockPos center) {
        Set<BlockPos> columns = new HashSet<>();
        for (int offset = -6; offset <= 6; offset++) {
            addArenaColumn(columns, center, -19, offset);
            addArenaColumn(columns, center, 19, offset);
            addArenaColumn(columns, center, offset, -19);
            addArenaColumn(columns, center, offset, 19);
        }
        for (int first = 9; first <= 18; first++) {
            int second = 27 - first;
            addSymmetricArenaColumns(columns, center, first, second);
        }
        for (int first = 9; first <= 17; first++) {
            int second = 26 - first;
            addSymmetricArenaColumns(columns, center, first, second);
        }
        addSymmetricArenaColumns(columns, center, 17, 6);
        addSymmetricArenaColumns(columns, center, 17, 8);
        addSymmetricArenaColumns(columns, center, 6, 17);
        addSymmetricArenaColumns(columns, center, 8, 17);
        return columns;
    }

    private static void addSymmetricArenaColumns(
            Set<BlockPos> columns,
            BlockPos center,
            int xOffset,
            int zOffset
    ) {
        addArenaColumn(columns, center, xOffset, zOffset);
        addArenaColumn(columns, center, xOffset, -zOffset);
        addArenaColumn(columns, center, -xOffset, zOffset);
        addArenaColumn(columns, center, -xOffset, -zOffset);
    }

    private static void addArenaColumn(
            Set<BlockPos> columns,
            BlockPos center,
            int xOffset,
            int zOffset
    ) {
        columns.add(center.offset(xOffset, 0, zOffset));
    }

    private void startVictoryCinematic(ServerLevel server) {
        setHealth(1.0F);
        setVictoryActive(true);
        setVictoryTick(0);
        victoryRewardSpawned = false;
        setNoAi(true);
        setInvulnerable(true);
        setVulnerable(false);
        setTarget(null);
        setDeltaMovement(Vec3.ZERO);
        setEnraged(false);
        setCustomNameVisible(true);
        setInvisible(false);
        moveTo(
                encounterCenter.x + 5.0D,
                encounterCenter.y,
                encounterCenter.z,
                90.0F,
                0.0F);
        setAnimation(ANIMATION_VICTORY, 0);
        bossEvent.setVisible(false);
        stopMarawTharMusic();
        restoreEncounterPlayerHealth();
        clearH7Projectiles();
        discardAllSmallLaserProjectiles();
        discardH3Blade();
        discardH5Echoes(server);
        clearH9(server, false);
        if (!h6AlteredBlocks.isEmpty()) {
            restoreH6Floor(server, 3);
        }
        stopAllRecordAudio(server);
        playMusicToAll(server, ModSounds.MARAWTHAR_VICTORY.get());
        server.playSound(null, BlockPos.containing(encounterCenter),
                SoundEvents.ENDER_DRAGON_HURT, SoundSource.MASTER, 1.0F, 0.65F);
    }

    private void tickVictoryCinematic(ServerLevel server) {
        int tick = getVictoryTick();
        setDeltaMovement(Vec3.ZERO);
        setAnimation(ANIMATION_VICTORY, tick);
        bossEvent.setVisible(false);
        tickVictoryEchoParticles(server, tick);

        if (tick < VICTORY_STATUE_SPAWN_TICK) {
            tickDefeatedVictoryActor(tick);
        } else {
            setInvisible(false);
            setCustomNameVisible(false);
            moveTo(
                    encounterCenter.x + 4.0D,
                    encounterCenter.y + 0.5D,
                    encounterCenter.z,
                    90.0F,
                    0.0F);
            tickVictoryStatue(server, tick);
        }

        if (tick == 3 * 20) sendVictoryDialogue(server, true, 0);
        if (tick == 6 * 20) sendVictoryDialogue(server, true, 1);
        if (tick == 9 * 20) {
            server.sendParticles(ParticleTypes.SQUID_INK,
                    getX() - 0.8D, getY() + 0.8D, getZ() - 0.7D,
                    30, 0.7D, 0.05D, 0.05D, 0.1D);
            server.playSound(null, blockPosition(), SoundEvents.TRIDENT_THUNDER,
                    SoundSource.MASTER, 1.0F, 1.2F);
        }
        if (tick == 11 * 20 || tick == 16 * 20 || tick == 18 * 20) {
            server.playSound(null, blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT,
                    SoundSource.MASTER, 0.6F, tick == 11 * 20 ? 0.6F : 0.4F);
        }
        if (tick == 12 * 20) sendVictoryDialogue(server, true, 2);
        if (tick == 21 * 20) sendVictoryDialogue(server, false, 3);
        if (tick == 26 * 20) sendVictoryDialogue(server, false, 4);
        if (tick == 31 * 20) sendVictoryDialogue(server, false, 5);
        if (tick == 38 * 20) sendVictoryDialogue(server, true, 6);
        if (tick == 43 * 20) sendVictoryDialogue(server, false, 7);
        if (tick == 47 * 20) sendVictoryDialogue(server, true, 8);
        if (tick == 50 * 20) sendVictoryDialogue(server, true, 9);
        if (tick == 56 * 20) sendVictoryDialogue(server, true, 10);

        if (tick == VICTORY_REWARD_DROP_TICK && !victoryRewardSpawned) {
            victoryRewardSpawned = true;
            Vec3 rewardGround = findVictoryRewardGround(server);
            NightfallChainBladeEntity.spawnVictoryReward(
                    server, rewardGround, 90.0F);
            if (arenaClosed) {
                openArena(server);
            }
        }
        if (tick >= VICTORY_FINISH_TICK) {
            stopVictoryMusic(server);
            setVictoryActive(false);
            discard();
            return;
        }
        setVictoryTick(tick + 1);
    }

    private Vec3 findVictoryRewardGround(ServerLevel server) {
        double x = encounterCenter.x + 4.0D;
        double z = encounterCenter.z;
        BlockPos start = BlockPos.containing(x, encounterCenter.y + 4.0D, z);
        int minimumY = Math.max(server.getMinBuildHeight(), start.getY() - 16);
        for (int y = start.getY(); y >= minimumY; y--) {
            BlockPos position = new BlockPos(start.getX(), y, start.getZ());
            BlockState state = server.getBlockState(position);
            if (!state.getCollisionShape(server, position).isEmpty()) {
                return new Vec3(x, y + 1.02D, z);
            }
        }
        return encounterCenter.add(4.0D, 0.02D, 0.0D);
    }

    private void tickDefeatedVictoryActor(int tick) {
        double xOffset = 5.0D;
        double yOffset = 0.0D;
        if (tick >= 18 * 20) {
            xOffset = 3.6D;
            yOffset = -1.2D
                    - Mth.clamp((tick - 18 * 20 - 10) * 0.004D, 0.0D, 0.76D);
        } else if (tick >= 16 * 20) {
            xOffset = 4.1D;
            yOffset = -0.8D;
        } else if (tick >= 11 * 20) {
            xOffset = 4.7D;
            yOffset = -0.3D;
        }
        moveTo(
                encounterCenter.x + xOffset,
                encounterCenter.y + yOffset,
                encounterCenter.z,
                90.0F,
                0.0F);
        setInvisible(false);
        setCustomNameVisible(true);
    }

    private void tickVictoryEchoParticles(ServerLevel server, int tick) {
        if ((tick & 1) != 0 || tick >= VICTORY_FINISH_TICK) {
            return;
        }
        Vec3 echo = encounterCenter.add(-3.0D, 2.3D, 0.0D);
        server.sendParticles(
                new DustParticleOptions(new Vector3f(0.58F, 0.89F, 1.0F), 0.8F),
                echo.x, echo.y, echo.z,
                3, 0.25D, 0.55D, 0.25D, 0.015D);
        server.sendParticles(ParticleTypes.END_ROD,
                echo.x, echo.y, echo.z,
                1, 0.16D, 0.45D, 0.16D, 0.01D);
    }

    private void sendVictoryDialogue(ServerLevel server, boolean marawthar, int line) {
        Component speaker = marawthar
                ? Component.translatable("dialogue.finalparadox.marawthar.victory.speaker")
                        .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD)
                : Component.translatable("dialogue.finalparadox.marawthar.victory.eothar")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD, ChatFormatting.ITALIC);
        Component message = speaker.copy().append(
                Component.translatable("dialogue.finalparadox.marawthar.victory." + line)
                        .withStyle(ChatFormatting.WHITE));
        for (ServerPlayer player : server.players()) {
            player.sendSystemMessage(message);
        }
        server.playSound(null, BlockPos.containing(encounterCenter),
                marawthar ? SoundEvents.RAVAGER_AMBIENT : SoundEvents.ALLAY_AMBIENT_WITH_ITEM,
                SoundSource.MASTER, marawthar ? 0.55F : 0.45F, marawthar ? 0.75F : 1.25F);
    }

    private static Vec3 victoryPartPosition(Vec3 center, double[] local) {
        double yaw = Math.toRadians(90.0D);
        double x = center.x + local[0] * Math.cos(yaw) - local[2] * Math.sin(yaw);
        double z = center.z + local[0] * Math.sin(yaw) + local[2] * Math.cos(yaw);
        return new Vec3(x, center.y + local[1], z);
    }

    private void tickVictoryStatue(ServerLevel server, int tick) {
        int petrificationTick = tick - VICTORY_PETRIFICATION_START_TICK;
        int[] blackstoneTicks = {1, 20, 40, 50, 60, -1, 70, 75, 80, -1};
        int[] stoneTicks = {83, 86, 89, 91, 93, -1, 95, 97, 99, -1};
        for (int index = 0; index < 10; index++) {
            if (petrificationTick == blackstoneTicks[index]) {
                emitVictoryPartConversion(server, index, 1);
            }
            if (petrificationTick == stoneTicks[index]) {
                emitVictoryPartConversion(server, index, 2);
            }
        }
        if (petrificationTick == 140) {
            for (int index = 10; index < VICTORY_PART_OFFSETS.length; index++) {
                emitVictoryPartConversion(server, index, 3);
            }
            Vec3 center = encounterCenter.add(4.0D, 2.0D, 0.0D);
            server.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                    center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            server.playSound(null, BlockPos.containing(center),
                    SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0F, 0.0F);
            server.playSound(null, BlockPos.containing(center),
                    SoundEvents.ENDER_DRAGON_DEATH, SoundSource.MASTER, 1.0F, 1.0F);
        }

        if (tick < VICTORY_STATUE_FALL_START_TICK) {
            return;
        }
        for (int index = 0; index < VICTORY_PART_OFFSETS.length; index++) {
            int fallAge = tick - VICTORY_STATUE_FALL_START_TICK
                    - VICTORY_PART_FALL_DELAYS[index];
            if (fallAge == 6) {
                emitVictoryPartBreak(server, index);
            }
        }
    }

    private void emitVictoryPartConversion(ServerLevel server, int index, int stage) {
        Vec3 position = victoryPartPosition(
                encounterCenter.add(4.0D, 0.5D, 0.0D),
                VICTORY_PART_OFFSETS[index]).add(0.0D, 1.5D, 0.0D);
        ItemStack particleStack = new ItemStack(
                stage >= 2 ? Items.STONE : stage == 1 ? Items.BLACKSTONE : Items.FIREWORK_STAR);
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, particleStack),
                position.x, position.y, position.z,
                10, 0.08D, 0.08D, 0.08D, 0.1D);
        server.sendParticles(ParticleTypes.CRIT,
                position.x, position.y, position.z,
                10, 0.05D, 0.05D, 0.05D, 0.1D);
        server.playSound(null, BlockPos.containing(position), SoundEvents.NETHER_BRICKS_BREAK,
                SoundSource.MASTER, 1.0F, 1.0F);
    }

    private void emitVictoryPartBreak(ServerLevel server, int index) {
        Vec3 position = victoryPartPosition(
                encounterCenter.add(4.0D, 0.5D, 0.0D),
                VICTORY_PART_OFFSETS[index]).add(0.0D, -1.0D, 0.0D);
        server.playSound(null, BlockPos.containing(position), SoundEvents.ANCIENT_DEBRIS_BREAK,
                SoundSource.MASTER, 1.0F, 1.0F);
        server.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.STONE)),
                position.x, position.y, position.z,
                10, 0.12D, 0.12D, 0.12D, 0.1D);
    }

    private static void stopVictoryMusic(ServerLevel server) {
        ResourceLocation victory = ModSounds.MARAWTHAR_VICTORY.get().getLocation();
        for (ServerPlayer player : server.players()) {
            player.connection.send(new ClientboundStopSoundPacket(victory, SoundSource.RECORDS));
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isVictoryActive()) {
            return false;
        }
        boolean damaged = super.hurt(source, amount);
        if (isVictoryActive()) {
            return damaged;
        }
        if (damaged && !level().isClientSide) {
            if (combatState == COMBAT_COUNTER_WINDOW && !openingWasHit) {
                openingWasHit = true;
                openingEndTick = combatTick + OPENING_HIT_COOLDOWN_DURATION;
                if (!phaseTransitionOpening) {
                    setChoreographyStep(Math.max(0, getChoreographyStep() - 1));
                }
            }
            hurtAnimationTicks = HURT_DURATION;
            setAnimation(ANIMATION_HURT, 0);
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SQUID_INK,
                        getX(), getY() + 1.15D, getZ(),
                        10, 0.28D, 0.45D, 0.28D, 0.02D);
                server.playSound(null, blockPosition(), SoundEvents.RAVAGER_HURT,
                        SoundSource.HOSTILE, 0.75F, 1.0F);
            }
        }
        return damaged;
    }

    @Override
    public void die(DamageSource source) {
        if (level() instanceof ServerLevel server && !isVictoryActive()) {
            startVictoryCinematic(server);
            return;
        }
        stopMarawTharMusic();
        restoreEncounterPlayerHealth();
        super.die(source);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy()) {
            if (level() instanceof ServerLevel server && isVictoryActive()) {
                stopVictoryMusic(server);
            }
            stopMarawTharMusic();
            restoreEncounterPlayerHealth();
            if (level() instanceof ServerLevel server && arenaClosed) {
                openArena(server);
            }
            if (level() instanceof ServerLevel server && !h6AlteredBlocks.isEmpty()) {
                restoreH6Floor(server, 3);
            }
            clearH7Projectiles();
            discardAllSmallLaserProjectiles();
            discardH3Blade();
            if (level() instanceof ServerLevel server) {
                discardH5Echoes(server);
                clearH9(server, false);
            }
        }
        super.remove(reason);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return false;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return target instanceof Player && super.canAttack(target);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (isVictoryActive()) {
            bossEvent.removePlayer(player);
            if (musicListeners.add(player.getUUID())) {
                player.playNotifySound(
                        ModSounds.MARAWTHAR_VICTORY.get(), SoundSource.RECORDS, 1.0F, 1.0F);
            }
            return;
        }
        bossEvent.addPlayer(player);
        playCurrentMusicTo(player);
        if (initialized && !player.isSpectator()) {
            applyEncounterPlayerHealth(player);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
        restoreEncounterPlayerHealth(player);
    }

    public int getPhase() {
        return entityData.get(DATA_PHASE);
    }

    public void setPhase(int phase) {
        entityData.set(DATA_PHASE, Math.max(0, phase));
    }

    public int getChoreographyStep() {
        return entityData.get(DATA_CHOREOGRAPHY_STEP);
    }

    public void setChoreographyStep(int step) {
        entityData.set(DATA_CHOREOGRAPHY_STEP, Math.max(0, step));
    }

    public int getAnimation() {
        return entityData.get(DATA_ANIMATION);
    }

    public int getAnimationTick() {
        return entityData.get(DATA_ANIMATION_TICK);
    }

    private void setAnimation(int animation, int animationTick) {
        entityData.set(DATA_ANIMATION, animation);
        entityData.set(DATA_ANIMATION_TICK, Math.max(0, animationTick));
    }

    public boolean isVulnerableWindow() {
        return entityData.get(DATA_VULNERABLE);
    }

    public void setVulnerable(boolean vulnerable) {
        entityData.set(DATA_VULNERABLE, vulnerable);
    }

    public boolean isEnraged() {
        return entityData.get(DATA_ENRAGED);
    }

    public void setEnraged(boolean enraged) {
        entityData.set(DATA_ENRAGED, enraged);
    }

    public int getTransitionVisualTick() {
        return entityData.get(DATA_TRANSITION_VISUAL_TICK);
    }

    public boolean usesTharKrooTransitionModel() {
        int transitionTick = getTransitionVisualTick();
        return transitionTick >= THAR_KROO_VISUAL_APPEAR_TICK
                && transitionTick < PHASE_INTERMISSION_DURATION;
    }

    public boolean isVictoryActive() {
        return entityData.get(DATA_VICTORY_ACTIVE);
    }

    private void setVictoryActive(boolean active) {
        entityData.set(DATA_VICTORY_ACTIVE, active);
    }

    public int getVictoryTick() {
        return entityData.get(DATA_VICTORY_TICK);
    }

    private void setVictoryTick(int tick) {
        entityData.set(DATA_VICTORY_TICK, Math.max(0, tick));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("MarawTharInitialized", initialized);
        tag.putInt("MarawTharIntroTick", introTick);
        tag.putInt("MarawTharPhase", getPhase());
        tag.putInt("MarawTharChoreographyStep", getChoreographyStep());
        tag.putBoolean("MarawTharVulnerable", isVulnerableWindow());
        tag.putBoolean("MarawTharEnraged", isEnraged());
        tag.putInt("MarawTharCombatState", combatState);
        tag.putInt("MarawTharCombatTick", combatTick);
        tag.putInt("MarawTharMusicState", musicState);
        tag.putInt("MarawTharMusicTick", musicTick);
        tag.putInt("MarawTharRandomBagIndex", randomBagIndex);
        tag.putIntArray("MarawTharRandomAttackBag", randomAttackBag);
        tag.putBoolean("MarawTharOpeningWasHit", openingWasHit);
        tag.putInt("MarawTharOpeningEndTick", openingEndTick);
        tag.putBoolean("MarawTharPhaseTransitionOpening", phaseTransitionOpening);
        tag.putString("MarawTharSourceLockAction", sourceLockAction.name());
        tag.putInt("MarawTharSourceLockDuration", sourceLockDuration);
        if (lockedTarget != null) {
            tag.putUUID("MarawTharLockedTarget", lockedTarget);
        }
        putVec3(tag, "MarawTharEncounterCenter", encounterCenter);
        putVec3(tag, "MarawTharH3BladePosition", h3BladePosition);
        putVec3(tag, "MarawTharH3ChasePosition", h3ChasePosition);
        if (h3BladeUuid != null) {
            tag.putUUID("MarawTharH3Blade", h3BladeUuid);
        }
        if (h3TargetUuid != null) {
            tag.putUUID("MarawTharH3Target", h3TargetUuid);
        }
        tag.putInt("MarawTharH9StartTick", h9StartTick);
        tag.putInt("MarawTharH9SpinDirection", h9SpinDirection);
        putVec3(tag, "MarawTharH9LockedDirection", h9LockedDirection);
        if (h9TargetUuid != null) {
            tag.putUUID("MarawTharH9Target", h9TargetUuid);
        }
        if (h9LaserUuid != null) {
            tag.putUUID("MarawTharH9Laser", h9LaserUuid);
        }
        ListTag h9HitTags = new ListTag();
        for (UUID playerId : h9HitPlayers) {
            CompoundTag hitTag = new CompoundTag();
            hitTag.putUUID("Player", playerId);
            h9HitTags.add(hitTag);
        }
        tag.put("MarawTharH9HitPlayers", h9HitTags);
        putVec3(tag, "MarawTharH5DashDirection", h5DashDirection);
        ListTag h5EchoTags = new ListTag();
        for (UUID echoId : h5EchoUuids) {
            h5EchoTags.add(NbtUtils.createUUID(echoId));
        }
        tag.put("MarawTharH5Echoes", h5EchoTags);
        ListTag h5HitTags = new ListTag();
        for (UUID playerId : h5HitPlayers) {
            h5HitTags.add(NbtUtils.createUUID(playerId));
        }
        tag.put("MarawTharH5HitPlayers", h5HitTags);
        tag.putInt("MarawTharBlueRushTrailAge", blueRushTrailAge);
        putVec3(tag, "MarawTharBlueRushTrailPosition", blueRushTrailPosition);
        putVec3(tag, "MarawTharBlueRushTrailDirection", blueRushTrailDirection);
        putUuidSet(tag, "MarawTharBlueRushTrailHitPlayers", blueRushTrailHitPlayers);
        for (int index = 0; index < blueVolleyLockedYaws.length; index++) {
            tag.putFloat("MarawTharBlueVolleyLockedYaw" + index, blueVolleyLockedYaws[index]);
            tag.putBoolean("MarawTharBlueVolleyYawLocked" + index, blueVolleyYawLocked[index]);
        }
        for (int strike = 0; strike < slashHitTargets.size(); strike++) {
            putUuidSet(tag, "MarawTharSlashHitPlayers" + strike, slashHitTargets.get(strike));
        }
        ListTag projectileCooldownTags = new ListTag();
        for (Map.Entry<UUID, Integer> entry : projectileHitCooldowns.entrySet()) {
            int remainingTicks = entry.getValue() - tickCount;
            if (remainingTicks <= 0) {
                continue;
            }
            CompoundTag cooldownTag = new CompoundTag();
            cooldownTag.putUUID("Player", entry.getKey());
            cooldownTag.putInt("RemainingTicks", remainingTicks);
            projectileCooldownTags.add(cooldownTag);
        }
        tag.put("MarawTharProjectileHitCooldowns", projectileCooldownTags);
        ListTag encounterHealthTags = new ListTag();
        for (UUID playerId : encounterHealthPlayers) {
            encounterHealthTags.add(NbtUtils.createUUID(playerId));
        }
        tag.put("MarawTharEncounterHealthPlayers", encounterHealthTags);
        tag.putBoolean("MarawTharArenaClosed", arenaClosed);
        tag.putBoolean("MarawTharVictoryActive", isVictoryActive());
        tag.putInt("MarawTharVictoryTick", getVictoryTick());
        tag.putBoolean("MarawTharVictoryRewardSpawned", victoryRewardSpawned);
        tag.putBoolean("MarawTharDefeatActive", defeatActive);
        tag.putInt("MarawTharDefeatTicks", defeatTicks);
        long[] arenaBarrierPositions = new long[arenaBarrierBlocks.size()];
        int arenaBarrierIndex = 0;
        for (BlockPos position : arenaBarrierBlocks) {
            arenaBarrierPositions[arenaBarrierIndex++] = position.asLong();
        }
        tag.putLongArray("MarawTharArenaBarrierBlocks", arenaBarrierPositions);
        ListTag h7ProjectileTags = new ListTag();
        for (H7Projectile projectile : h7Projectiles) {
            CompoundTag projectileTag = new CompoundTag();
            projectileTag.putBoolean("Blue", projectile.blue);
            projectileTag.putUUID("Target", projectile.target);
            projectileTag.putInt("Curve", projectile.curve);
            projectileTag.putInt("Age", projectile.age);
            putVec3(projectileTag, "Position", projectile.position);
            projectileTag.putFloat("Yaw", projectile.yaw);
            projectileTag.putFloat("Pitch", projectile.pitch);
            putUuidSet(projectileTag, "HitPlayers", projectile.hitPlayers);
            h7ProjectileTags.add(projectileTag);
        }
        tag.put("MarawTharH7Projectiles", h7ProjectileTags);
        ListTag h6BlockTags = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : h6AlteredBlocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putLong("Pos", entry.getKey().asLong());
            blockTag.put("State", NbtUtils.writeBlockState(entry.getValue()));
            h6BlockTags.add(blockTag);
        }
        tag.put("MarawTharH6AlteredBlocks", h6BlockTags);
        ListTag meteorTags = new ListTag();
        for (H4Meteor meteor : h4Meteors) {
            CompoundTag meteorTag = new CompoundTag();
            putVec3(meteorTag, "Target", meteor.target);
            putVec3(meteorTag, "Position", meteor.position);
            meteorTag.putInt("WarningAge", meteor.warningAge);
            meteorTag.putInt("FallAge", meteor.fallAge);
            meteorTag.putBoolean("Falling", meteor.falling);
            meteorTags.add(meteorTag);
        }
        tag.put("MarawTharH4Meteors", meteorTags);

        ListTag projectileTags = new ListTag();
        for (SmallLaserProjectile projectile : smallLaserProjectiles) {
            CompoundTag projectileTag = new CompoundTag();
            projectileTag.putInt("Age", projectile.age);
            projectileTag.putBoolean("Red", projectile.red);
            projectileTag.putBoolean("H4Judgment", projectile.h4Judgment);
            putVec3(projectileTag, "Origin", projectile.origin);
            putVec3(projectileTag, "Direction", projectile.direction);
            for (int index = 0; index < projectile.parts.length; index++) {
                projectileTag.putUUID("Part" + index, projectile.parts[index]);
            }
            putUuidSet(projectileTag, "HitPlayers", projectile.hitPlayers);
            projectileTags.add(projectileTag);
        }
        tag.put("MarawTharSmallLaserProjectiles", projectileTags);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        migrateLegacySaveKeys(tag);
        initialized = tag.getBoolean("MarawTharInitialized");
        introTick = tag.getInt("MarawTharIntroTick");
        setPhase(tag.getInt("MarawTharPhase"));
        setChoreographyStep(tag.getInt("MarawTharChoreographyStep"));
        setVulnerable(tag.getBoolean("MarawTharVulnerable"));
        setEnraged(tag.getBoolean("MarawTharEnraged"));
        combatState = tag.getInt("MarawTharCombatState");
        combatTick = tag.getInt("MarawTharCombatTick");
        if (tag.contains("MarawTharMusicState")) {
            musicState = Mth.clamp(tag.getInt("MarawTharMusicState"), MUSIC_NONE, MUSIC_LOOP);
            musicTick = tag.getInt("MarawTharMusicTick");
        } else if (initialized) {
            musicState = MUSIC_INTRO;
            musicTick = 0;
        } else {
            musicState = MUSIC_NONE;
            musicTick = -1;
        }
        musicListeners.clear();
        needsMusicRecovery = !isAddedToWorld() && initialized && musicState != MUSIC_NONE;
        entityData.set(DATA_TRANSITION_VISUAL_TICK,
                combatState == COMBAT_PHASE_INTERMISSION ? combatTick : -1);
        randomBagIndex = tag.getInt("MarawTharRandomBagIndex");
        int[] savedBag = tag.getIntArray("MarawTharRandomAttackBag");
        if (savedBag.length == 3) randomAttackBag = savedBag;
        openingWasHit = tag.getBoolean("MarawTharOpeningWasHit");
        openingEndTick = tag.contains("MarawTharOpeningEndTick")
                ? tag.getInt("MarawTharOpeningEndTick") : COUNTER_WINDOW_DURATION;
        phaseTransitionOpening = tag.getBoolean("MarawTharPhaseTransitionOpening");
        sourceLockDuration = tag.getInt("MarawTharSourceLockDuration");
        try {
            sourceLockAction = MarawTharChoreography.Action.valueOf(
                    tag.getString("MarawTharSourceLockAction"));
        } catch (IllegalArgumentException exception) {
            sourceLockAction = MarawTharChoreography.Action.NONE;
        }
        lockedTarget = tag.hasUUID("MarawTharLockedTarget")
                ? tag.getUUID("MarawTharLockedTarget") : null;
        encounterCenter = tag.contains("MarawTharEncounterCenterX")
                ? getVec3(tag, "MarawTharEncounterCenter") : position();
        h3BladePosition = getVec3(tag, "MarawTharH3BladePosition");
        h3ChasePosition = getVec3(tag, "MarawTharH3ChasePosition");
        h3BladeUuid = tag.hasUUID("MarawTharH3Blade")
                ? tag.getUUID("MarawTharH3Blade") : null;
        h3TargetUuid = tag.hasUUID("MarawTharH3Target")
                ? tag.getUUID("MarawTharH3Target") : null;
        h9StartTick = tag.contains("MarawTharH9StartTick")
                ? tag.getInt("MarawTharH9StartTick") : -1;
        h9SpinDirection = tag.getInt("MarawTharH9SpinDirection");
        h9LockedDirection = getVec3(tag, "MarawTharH9LockedDirection");
        h9TargetUuid = tag.hasUUID("MarawTharH9Target")
                ? tag.getUUID("MarawTharH9Target") : null;
        h9LaserUuid = tag.hasUUID("MarawTharH9Laser")
                ? tag.getUUID("MarawTharH9Laser") : null;
        h9HitPlayers.clear();
        ListTag h9HitTags = tag.getList("MarawTharH9HitPlayers", Tag.TAG_COMPOUND);
        for (int index = 0; index < h9HitTags.size(); index++) {
            CompoundTag hitTag = h9HitTags.getCompound(index);
            if (hitTag.hasUUID("Player")) {
                h9HitPlayers.add(hitTag.getUUID("Player"));
            }
        }
        h5DashDirection = getVec3(tag, "MarawTharH5DashDirection");
        h5EchoUuids.clear();
        ListTag h5EchoTags = tag.getList("MarawTharH5Echoes", Tag.TAG_INT_ARRAY);
        for (int index = 0; index < h5EchoTags.size(); index++) {
            h5EchoUuids.add(NbtUtils.loadUUID(h5EchoTags.get(index)));
        }
        h5HitPlayers.clear();
        ListTag h5HitTags = tag.getList("MarawTharH5HitPlayers", Tag.TAG_INT_ARRAY);
        for (int index = 0; index < h5HitTags.size(); index++) {
            h5HitPlayers.add(NbtUtils.loadUUID(h5HitTags.get(index)));
        }
        blueRushTrailAge = tag.contains("MarawTharBlueRushTrailAge")
                ? tag.getInt("MarawTharBlueRushTrailAge") : -1;
        blueRushTrailPosition = getVec3(tag, "MarawTharBlueRushTrailPosition");
        blueRushTrailDirection = getVec3(tag, "MarawTharBlueRushTrailDirection");
        loadUuidSet(tag, "MarawTharBlueRushTrailHitPlayers", blueRushTrailHitPlayers);
        for (int index = 0; index < blueVolleyLockedYaws.length; index++) {
            blueVolleyLockedYaws[index] = tag.getFloat("MarawTharBlueVolleyLockedYaw" + index);
            blueVolleyYawLocked[index] = tag.getBoolean("MarawTharBlueVolleyYawLocked" + index);
        }
        for (int strike = 0; strike < slashHitTargets.size(); strike++) {
            loadUuidSet(tag, "MarawTharSlashHitPlayers" + strike, slashHitTargets.get(strike));
        }
        projectileHitCooldowns.clear();
        ListTag projectileCooldownTags =
                tag.getList("MarawTharProjectileHitCooldowns", Tag.TAG_COMPOUND);
        for (int index = 0; index < projectileCooldownTags.size(); index++) {
            CompoundTag cooldownTag = projectileCooldownTags.getCompound(index);
            if (!cooldownTag.hasUUID("Player")) {
                continue;
            }
            int remainingTicks = Mth.clamp(cooldownTag.getInt("RemainingTicks"), 0, 2);
            if (remainingTicks > 0) {
                projectileHitCooldowns.put(
                        cooldownTag.getUUID("Player"), tickCount + remainingTicks);
            }
        }
        encounterHealthPlayers.clear();
        ListTag encounterHealthTags =
                tag.getList("MarawTharEncounterHealthPlayers", Tag.TAG_INT_ARRAY);
        for (int index = 0; index < encounterHealthTags.size(); index++) {
            encounterHealthPlayers.add(NbtUtils.loadUUID(encounterHealthTags.get(index)));
        }
        arenaClosed = tag.getBoolean("MarawTharArenaClosed");
        setVictoryActive(tag.getBoolean("MarawTharVictoryActive"));
        setVictoryTick(tag.getInt("MarawTharVictoryTick"));
        victoryRewardSpawned = tag.getBoolean("MarawTharVictoryRewardSpawned");
        defeatActive = tag.getBoolean("MarawTharDefeatActive");
        defeatTicks = tag.getInt("MarawTharDefeatTicks");
        arenaBarrierBlocks.clear();
        for (long position : tag.getLongArray("MarawTharArenaBarrierBlocks")) {
            arenaBarrierBlocks.add(BlockPos.of(position));
        }
        h7Projectiles.clear();
        ListTag h7ProjectileTags =
                tag.getList("MarawTharH7Projectiles", Tag.TAG_COMPOUND);
        for (int index = 0; index < h7ProjectileTags.size(); index++) {
            CompoundTag projectileTag = h7ProjectileTags.getCompound(index);
            if (!projectileTag.hasUUID("Target")) {
                continue;
            }
            int curve = Mth.clamp(projectileTag.getInt("Curve"), 0, 15);
            boolean blue = projectileTag.getBoolean("Blue");
            if (!blue && curve == 0) {
                curve = 1;
            }
            H7Projectile projectile = new H7Projectile(
                    blue,
                    projectileTag.getUUID("Target"),
                    curve,
                    Mth.clamp(projectileTag.getInt("Age"), 0, H7_PROJECTILE_LIFETIME - 1),
                    getVec3(projectileTag, "Position"),
                    projectileTag.getFloat("Yaw"),
                    projectileTag.getFloat("Pitch"));
            loadUuidSet(projectileTag, "HitPlayers", projectile.hitPlayers);
            h7Projectiles.add(projectile);
        }
        h6AlteredBlocks.clear();
        if (level() instanceof ServerLevel server) {
            ListTag h6BlockTags =
                    tag.getList("MarawTharH6AlteredBlocks", Tag.TAG_COMPOUND);
            for (int index = 0; index < h6BlockTags.size(); index++) {
                CompoundTag blockTag = h6BlockTags.getCompound(index);
                h6AlteredBlocks.put(
                        BlockPos.of(blockTag.getLong("Pos")),
                        NbtUtils.readBlockState(
                                server.holderLookup(
                                        net.minecraft.core.registries.Registries.BLOCK),
                                blockTag.getCompound("State")));
            }
        }
        h4Meteors.clear();
        ListTag meteorTags = tag.getList("MarawTharH4Meteors", Tag.TAG_COMPOUND);
        for (int index = 0; index < meteorTags.size(); index++) {
            CompoundTag meteorTag = meteorTags.getCompound(index);
            h4Meteors.add(new H4Meteor(
                    getVec3(meteorTag, "Target"),
                    getVec3(meteorTag, "Position"),
                    meteorTag.getInt("WarningAge"),
                    meteorTag.getInt("FallAge"),
                    meteorTag.getBoolean("Falling")));
        }
        smallLaserProjectiles.clear();
        ListTag projectileTags =
                tag.getList("MarawTharSmallLaserProjectiles", Tag.TAG_COMPOUND);
        for (int index = 0; index < projectileTags.size(); index++) {
            CompoundTag projectileTag = projectileTags.getCompound(index);
            UUID[] parts = new UUID[3];
            boolean complete = true;
            for (int part = 0; part < parts.length; part++) {
                if (!projectileTag.hasUUID("Part" + part)) {
                    complete = false;
                    break;
                }
                parts[part] = projectileTag.getUUID("Part" + part);
            }
            if (complete) {
                SmallLaserProjectile projectile = new SmallLaserProjectile(
                        projectileTag.getInt("Age"),
                        projectileTag.getBoolean("Red"),
                        projectileTag.getBoolean("H4Judgment"),
                        getVec3(projectileTag, "Origin"),
                        getVec3(projectileTag, "Direction"),
                        parts);
                loadUuidSet(projectileTag, "HitPlayers", projectile.hitPlayers);
                smallLaserProjectiles.add(projectile);
            }
        }
        if (initialized) {
            bossEvent.setVisible(!isVictoryActive());
        }
    }

    /**
     * Copies save keys written by releases that used the former internal boss
     * name. New saves only write MarawThar-prefixed keys.
     */
    private static void migrateLegacySaveKeys(CompoundTag tag) {
        for (String key : Set.copyOf(tag.getAllKeys())) {
            if (!key.startsWith("Marota")) {
                continue;
            }
            String migratedKey = "MarawThar" + key.substring("Marota".length());
            Tag value = tag.get(key);
            if (!tag.contains(migratedKey) && value != null) {
                tag.put(migratedKey, value.copy());
            }
        }
    }

    private static void putVec3(CompoundTag tag, String key, Vec3 value) {
        tag.putDouble(key + "X", value.x);
        tag.putDouble(key + "Y", value.y);
        tag.putDouble(key + "Z", value.z);
    }

    private static void putUuidSet(CompoundTag tag, String key, Set<UUID> values) {
        ListTag entries = new ListTag();
        for (UUID value : values) {
            entries.add(NbtUtils.createUUID(value));
        }
        tag.put(key, entries);
    }

    private static void loadUuidSet(CompoundTag tag, String key, Set<UUID> output) {
        output.clear();
        ListTag entries = tag.getList(key, Tag.TAG_INT_ARRAY);
        for (int index = 0; index < entries.size(); index++) {
            output.add(NbtUtils.loadUUID(entries.get(index)));
        }
    }

    private static Vec3 getVec3(CompoundTag tag, String key) {
        return new Vec3(
                tag.getDouble(key + "X"),
                tag.getDouble(key + "Y"),
                tag.getDouble(key + "Z"));
    }

    private static final class SmallLaserProjectile {
        private int age;
        private final boolean red;
        private final boolean h4Judgment;
        private Vec3 origin;
        private final Vec3 direction;
        private final UUID[] parts;
        private final Set<UUID> hitPlayers = new HashSet<>();

        private SmallLaserProjectile(int age, boolean red, boolean h4Judgment, Vec3 origin,
                                     Vec3 direction, UUID[] parts) {
            this.age = age;
            this.red = red;
            this.h4Judgment = h4Judgment;
            this.origin = origin;
            this.direction = direction;
            this.parts = parts;
        }
    }

    private static final class H7Projectile {
        private final boolean blue;
        private final UUID target;
        private final int curve;
        private int age;
        private Vec3 position;
        private float yaw;
        private float pitch;
        private final Set<UUID> hitPlayers = new HashSet<>();

        private H7Projectile(boolean blue, UUID target, int curve, int age,
                             Vec3 position, float yaw, float pitch) {
            this.blue = blue;
            this.target = target;
            this.curve = curve;
            this.age = age;
            this.position = position;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private static final class H4Meteor {
        private final Vec3 target;
        private Vec3 position = Vec3.ZERO;
        private int warningAge;
        private int fallAge;
        private boolean falling;

        private H4Meteor(Vec3 target) {
            this.target = target;
        }

        private H4Meteor(Vec3 target, Vec3 position, int warningAge,
                         int fallAge, boolean falling) {
            this.target = target;
            this.position = position;
            this.warningAge = warningAge;
            this.fallAge = fallAge;
            this.falling = falling;
        }
    }
}
