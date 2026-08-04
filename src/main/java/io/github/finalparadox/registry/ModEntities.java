package io.github.finalparadox.registry;

import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.GlaivorusBladeEntity;
import io.github.finalparadox.entity.StygianArrowEntity;
import io.github.finalparadox.entity.SummonedBeeEntity;
import io.github.finalparadox.entity.WindTornadoEntity;
import io.github.finalparadox.entity.PaladinHammerSwingEntity;
import io.github.finalparadox.entity.ConsecrationEntity;
import io.github.finalparadox.entity.CosmicHammerSwingEntity;
import io.github.finalparadox.entity.VoidSingularityEntity;
import io.github.finalparadox.entity.BladeRingEntity;
import io.github.finalparadox.entity.TeslaCoreEntity;
import io.github.finalparadox.entity.FrostStormEntity;
import io.github.finalparadox.entity.PicomerangEntity;
import io.github.finalparadox.entity.TerrastalkerRoverEntity;
import io.github.finalparadox.entity.HostileTerrastalkerEntity;
import io.github.finalparadox.entity.ZombieSupermatrixEntity;
import io.github.finalparadox.entity.B8H2ModuleEntity;
import io.github.finalparadox.entity.B8SniperBulletEntity;
import io.github.finalparadox.entity.B8DetonatorBombEntity;
import io.github.finalparadox.entity.B8CrushingWaveEntity;
import io.github.finalparadox.entity.KoyomiBossEntity;
import io.github.finalparadox.entity.GariBossEntity;
import io.github.finalparadox.entity.HarvesterEntity;
import io.github.finalparadox.entity.SoulSplitEntity;
import io.github.finalparadox.entity.FireRainEntity;
import io.github.finalparadox.entity.PumpkinBombEntity;
import io.github.finalparadox.entity.PumpkinBarrageEntity;
import io.github.finalparadox.entity.PumpkinHammerSwingEntity;
import io.github.finalparadox.entity.ChestLaserEntity;
import io.github.finalparadox.entity.FrostBreathEntity;
import io.github.finalparadox.entity.FrozenPrisonEntity;
import io.github.finalparadox.entity.RipperConeEntity;
import io.github.finalparadox.entity.HeavyArbalestBurstEntity;
import io.github.finalparadox.entity.ElectricHammerSwingEntity;
import io.github.finalparadox.entity.GreatHookEntity;
import io.github.finalparadox.entity.BamboomerangEntity;
import io.github.finalparadox.entity.ElectricArcEntity;
import io.github.finalparadox.entity.RuinHammerSwingEntity;
import io.github.finalparadox.entity.RuinWaveEntity;
import io.github.finalparadox.entity.RuinPillarEntity;
import io.github.finalparadox.entity.ArcaneTechniqueEntity;
import io.github.finalparadox.entity.EctronTowerEntity;
import io.github.finalparadox.entity.EchoingShieldEntity;
import io.github.finalparadox.entity.ProfaneTechniqueEntity;
import io.github.finalparadox.entity.OmegaTechniqueEntity;
import io.github.finalparadox.entity.ApigloBossEntity;
import io.github.finalparadox.entity.TharKrooBossEntity;
import io.github.finalparadox.entity.ConquerorShadowBossEntity;
import io.github.finalparadox.entity.NightfallLaserEntity;
import io.github.finalparadox.entity.NightfallChainBladeEntity;
import io.github.finalparadox.entity.AtacromGauntletEntity;
import io.github.finalparadox.entity.MarawTharBossEntity;
import io.github.finalparadox.entity.EotharEchoEntity;
import io.github.finalparadox.entity.KorosEchoEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FinalParadox.MOD_ID);

    public static final RegistryObject<EntityType<AtacromGauntletEntity>> ATACROM_GAUNTLET_VISUAL =
            REGISTER.register(
                    "atacrom_gauntlet_visual",
                    () -> EntityType.Builder
                            .<AtacromGauntletEntity>of(AtacromGauntletEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F)
                            .clientTrackingRange(32)
                            .updateInterval(1)
                            .build("atacrom_gauntlet_visual")
            );

    public static final RegistryObject<EntityType<NightfallLaserEntity>> NIGHTFALL_LASER = REGISTER.register(
            "nightfall_laser",
            () -> EntityType.Builder.<NightfallLaserEntity>of(NightfallLaserEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("nightfall_laser")
    );

    public static final RegistryObject<EntityType<NightfallChainBladeEntity>> NIGHTFALL_CHAIN_BLADE =
            REGISTER.register(
                    "nightfall_chain_blade",
                    () -> EntityType.Builder
                            .<NightfallChainBladeEntity>of(
                                    NightfallChainBladeEntity::new, MobCategory.MISC)
                            .sized(0.8F, 2.6F)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("nightfall_chain_blade")
            );

    public static final RegistryObject<EntityType<GlaivorusBladeEntity>> GLAIVORUS_BLADE = REGISTER.register(
            "glaivorus_blade",
            () -> EntityType.Builder.<GlaivorusBladeEntity>of(GlaivorusBladeEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F)
                    .clientTrackingRange(32)
                    .updateInterval(1)
                    .build("glaivorus_blade")
    );

    public static final RegistryObject<EntityType<StygianArrowEntity>> STYGIAN_ARROW = REGISTER.register(
            "stygian_arrow",
            () -> EntityType.Builder.<StygianArrowEntity>of(StygianArrowEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("stygian_arrow")
    );

    public static final RegistryObject<EntityType<SummonedBeeEntity>> SUMMONED_BEE = REGISTER.register(
            "summoned_bee",
            () -> EntityType.Builder.<SummonedBeeEntity>of(SummonedBeeEntity::new, MobCategory.CREATURE)
                    .sized(0.7F, 0.6F).clientTrackingRange(64).updateInterval(1).build("summoned_bee")
    );

    public static final RegistryObject<EntityType<WindTornadoEntity>> WIND_TORNADO = REGISTER.register(
            "wind_tornado",
            () -> EntityType.Builder.<WindTornadoEntity>of(WindTornadoEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1).build("wind_tornado")
    );

    public static final RegistryObject<EntityType<PaladinHammerSwingEntity>> PALADIN_HAMMER_SWING = REGISTER.register(
            "paladin_hammer_swing",
            () -> EntityType.Builder.<PaladinHammerSwingEntity>of(PaladinHammerSwingEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(32).updateInterval(1).build("paladin_hammer_swing")
    );

    public static final RegistryObject<EntityType<ConsecrationEntity>> CONSECRATION = REGISTER.register(
            "consecration",
            () -> EntityType.Builder.<ConsecrationEntity>of(ConsecrationEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(10).build("consecration")
    );

    public static final RegistryObject<EntityType<CosmicHammerSwingEntity>> COSMIC_HAMMER_SWING = REGISTER.register(
            "cosmic_hammer_swing", () -> EntityType.Builder.<CosmicHammerSwingEntity>of(CosmicHammerSwingEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(32).updateInterval(1).build("cosmic_hammer_swing"));
    public static final RegistryObject<EntityType<VoidSingularityEntity>> VOID_SINGULARITY = REGISTER.register(
            "void_singularity", () -> EntityType.Builder.<VoidSingularityEntity>of(VoidSingularityEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1).build("void_singularity"));
    public static final RegistryObject<EntityType<BladeRingEntity>> BLADE_RING = REGISTER.register(
            "blade_ring", () -> EntityType.Builder.<BladeRingEntity>of(BladeRingEntity::new, MobCategory.MISC)
                    .sized(4.8F, 1.0F).clientTrackingRange(64).updateInterval(1).build("blade_ring"));
    public static final RegistryObject<EntityType<TeslaCoreEntity>> TESLA_CORE = REGISTER.register(
            "tesla_core", () -> EntityType.Builder.<TeslaCoreEntity>of(TeslaCoreEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).clientTrackingRange(64).updateInterval(1).build("tesla_core"));
    public static final RegistryObject<EntityType<FrostStormEntity>> FROST_STORM = REGISTER.register(
            "frost_storm", () -> EntityType.Builder.<FrostStormEntity>of(FrostStormEntity::new, MobCategory.MISC)
                    .sized(0.1F, 0.1F).clientTrackingRange(64).updateInterval(1).build("frost_storm"));
    public static final RegistryObject<EntityType<PicomerangEntity>> PICOMERANG = REGISTER.register(
            "picomerang", () -> EntityType.Builder.<PicomerangEntity>of(PicomerangEntity::new, MobCategory.MISC)
                    .sized(0.8F,0.8F).clientTrackingRange(64).updateInterval(1).build("picomerang"));
    public static final RegistryObject<EntityType<HarvesterEntity>> HARVESTER = REGISTER.register(
            "harvester", () -> EntityType.Builder.<HarvesterEntity>of(HarvesterEntity::new, MobCategory.MISC)
                    .sized(1.8F,0.8F).clientTrackingRange(64).updateInterval(1).build("harvester"));
    public static final RegistryObject<EntityType<SoulSplitEntity>> SOUL_SPLIT = REGISTER.register(
            "soul_split", () -> EntityType.Builder.<SoulSplitEntity>of(SoulSplitEntity::new, MobCategory.MISC)
                    .sized(0.1F,0.1F).clientTrackingRange(64).updateInterval(1).build("soul_split"));
    public static final RegistryObject<EntityType<FireRainEntity>> FIRE_RAIN = REGISTER.register(
            "fire_rain", () -> EntityType.Builder.<FireRainEntity>of(FireRainEntity::new, MobCategory.MISC)
                    .sized(0.1F,0.1F).clientTrackingRange(96).updateInterval(1).build("fire_rain"));
    public static final RegistryObject<EntityType<PumpkinBombEntity>> PUMPKIN_BOMB=REGISTER.register("pumpkin_bomb",()->EntityType.Builder.<PumpkinBombEntity>of(PumpkinBombEntity::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(64).updateInterval(1).build("pumpkin_bomb"));
    public static final RegistryObject<EntityType<PumpkinBarrageEntity>> PUMPKIN_BARRAGE=REGISTER.register("pumpkin_barrage",()->EntityType.Builder.<PumpkinBarrageEntity>of(PumpkinBarrageEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("pumpkin_barrage"));
    public static final RegistryObject<EntityType<PumpkinHammerSwingEntity>> PUMPKIN_HAMMER_SWING=REGISTER.register("pumpkin_hammer_swing",()->EntityType.Builder.<PumpkinHammerSwingEntity>of(PumpkinHammerSwingEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("pumpkin_hammer_swing"));
    public static final RegistryObject<EntityType<ChestLaserEntity>> CHEST_LASER=REGISTER.register("chest_laser",()->EntityType.Builder.<ChestLaserEntity>of(ChestLaserEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(96).updateInterval(1).build("chest_laser"));
    public static final RegistryObject<EntityType<FrostBreathEntity>> FROST_BREATH=REGISTER.register("frost_breath",()->EntityType.Builder.<FrostBreathEntity>of(FrostBreathEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("frost_breath"));
    public static final RegistryObject<EntityType<FrozenPrisonEntity>> FROZEN_PRISON=REGISTER.register("frozen_prison",()->EntityType.Builder.<FrozenPrisonEntity>of(FrozenPrisonEntity::new,MobCategory.MISC).sized(1.5F,2F).clientTrackingRange(64).updateInterval(1).build("frozen_prison"));
    public static final RegistryObject<EntityType<RipperConeEntity>> RIPPER_CONE=REGISTER.register("ripper_cone",()->EntityType.Builder.<RipperConeEntity>of(RipperConeEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("ripper_cone"));
    public static final RegistryObject<EntityType<HeavyArbalestBurstEntity>> HEAVY_ARBALEST_BURST=REGISTER.register("heavy_arbalest_burst",()->EntityType.Builder.<HeavyArbalestBurstEntity>of(HeavyArbalestBurstEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("heavy_arbalest_burst"));
    public static final RegistryObject<EntityType<ElectricHammerSwingEntity>> ELECTRIC_HAMMER_SWING=REGISTER.register("electric_hammer_swing",()->EntityType.Builder.<ElectricHammerSwingEntity>of(ElectricHammerSwingEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("electric_hammer_swing"));
    public static final RegistryObject<EntityType<GreatHookEntity>> GREAT_HOOK=REGISTER.register("great_hook",()->EntityType.Builder.<GreatHookEntity>of(GreatHookEntity::new,MobCategory.MISC).sized(.5F,.5F).clientTrackingRange(64).updateInterval(1).build("great_hook"));
    public static final RegistryObject<EntityType<BamboomerangEntity>> BAMBOOMERANG=REGISTER.register("bamboomerang",()->EntityType.Builder.<BamboomerangEntity>of(BamboomerangEntity::new,MobCategory.MISC).sized(2F,.8F).clientTrackingRange(64).updateInterval(1).build("bamboomerang"));
    public static final RegistryObject<EntityType<ElectricArcEntity>> ELECTRIC_ARC=REGISTER.register("electric_arc",()->EntityType.Builder.<ElectricArcEntity>of(ElectricArcEntity::new,MobCategory.MISC).sized(4F,2F).clientTrackingRange(64).updateInterval(1).build("electric_arc"));
    public static final RegistryObject<EntityType<RuinHammerSwingEntity>> RUIN_HAMMER_SWING=REGISTER.register("ruin_hammer_swing",()->EntityType.Builder.<RuinHammerSwingEntity>of(RuinHammerSwingEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("ruin_hammer_swing"));
    public static final RegistryObject<EntityType<RuinWaveEntity>> RUIN_WAVE=REGISTER.register("ruin_wave",()->EntityType.Builder.<RuinWaveEntity>of(RuinWaveEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("ruin_wave"));
    public static final RegistryObject<EntityType<RuinPillarEntity>> RUIN_PILLAR=REGISTER.register("ruin_pillar",()->EntityType.Builder.<RuinPillarEntity>of(RuinPillarEntity::new,MobCategory.MISC).sized(1F,3F).clientTrackingRange(64).updateInterval(1).build("ruin_pillar"));
    public static final RegistryObject<EntityType<ArcaneTechniqueEntity>> ARCANE_TECHNIQUE=REGISTER.register("arcane_technique",()->EntityType.Builder.<ArcaneTechniqueEntity>of(ArcaneTechniqueEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("arcane_technique"));
    public static final RegistryObject<EntityType<EctronTowerEntity>> ECTRON_TOWER=REGISTER.register("ectron_tower",()->EntityType.Builder.<EctronTowerEntity>of(EctronTowerEntity::new,MobCategory.MISC).sized(1.5F,4F).clientTrackingRange(64).updateInterval(1).build("ectron_tower"));
    public static final RegistryObject<EntityType<EchoingShieldEntity>> ECHOING_SHIELD=REGISTER.register("echoing_shield",()->EntityType.Builder.<EchoingShieldEntity>of(EchoingShieldEntity::new,MobCategory.MISC).sized(1.4F,1.4F).clientTrackingRange(64).updateInterval(1).build("echoing_shield"));
    public static final RegistryObject<EntityType<ProfaneTechniqueEntity>> PROFANE_TECHNIQUE=REGISTER.register("profane_technique",()->EntityType.Builder.<ProfaneTechniqueEntity>of(ProfaneTechniqueEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("profane_technique"));
    public static final RegistryObject<EntityType<OmegaTechniqueEntity>> OMEGA_TECHNIQUE=REGISTER.register("omega_technique",()->EntityType.Builder.<OmegaTechniqueEntity>of(OmegaTechniqueEntity::new,MobCategory.MISC).sized(.1F,.1F).clientTrackingRange(64).updateInterval(1).build("omega_technique"));
    public static final RegistryObject<EntityType<TerrastalkerRoverEntity>> TERRASTALKER_ROVER=REGISTER.register("terrastalker_rover",()->EntityType.Builder.<TerrastalkerRoverEntity>of(TerrastalkerRoverEntity::new,MobCategory.MISC).sized(.6F,.8F).clientTrackingRange(64).updateInterval(1).build("terrastalker_rover"));
    public static final RegistryObject<EntityType<HostileTerrastalkerEntity>> HOSTILE_TERRASTALKER = REGISTER.register(
            "hostile_terrastalker",
            () -> EntityType.Builder.<HostileTerrastalkerEntity>of(
                            HostileTerrastalkerEntity::new, MobCategory.MONSTER)
                    .sized(2.8F, 1.8F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("hostile_terrastalker"));
    public static final RegistryObject<EntityType<ZombieSupermatrixEntity>> ZOMBIE_SUPERMATRIX = REGISTER.register(
            "zombie_supermatrix", () -> EntityType.Builder
                    .<ZombieSupermatrixEntity>of(ZombieSupermatrixEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(128)
                    .updateInterval(1)
                    .build("zombie_supermatrix"));
    public static final RegistryObject<EntityType<B8H2ModuleEntity>> B8_H2_MODULE = REGISTER.register(
            "b8_h2_module", () -> EntityType.Builder
                    .<B8H2ModuleEntity>of(B8H2ModuleEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("b8_h2_module"));
    public static final RegistryObject<EntityType<B8SniperBulletEntity>> B8_SNIPER_BULLET = REGISTER.register(
            "b8_sniper_bullet", () -> EntityType.Builder
                    .<B8SniperBulletEntity>of(B8SniperBulletEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("b8_sniper_bullet"));
    public static final RegistryObject<EntityType<B8DetonatorBombEntity>> B8_DETONATOR_BOMB = REGISTER.register(
            "b8_detonator_bomb", () -> EntityType.Builder
                    .<B8DetonatorBombEntity>of(B8DetonatorBombEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 0.7F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("b8_detonator_bomb"));
    public static final RegistryObject<EntityType<B8CrushingWaveEntity>> B8_CRUSHING_WAVE = REGISTER.register(
            "b8_crushing_wave", () -> EntityType.Builder
                    .<B8CrushingWaveEntity>of(B8CrushingWaveEntity::new, MobCategory.MISC)
                    .sized(0.2F, 0.2F)
                    .clientTrackingRange(96)
                    .updateInterval(1)
                    .build("b8_crushing_wave"));
    public static final RegistryObject<EntityType<ApigloBossEntity>> APIGLO = REGISTER.register("apiglo", () ->
            EntityType.Builder.<ApigloBossEntity>of(ApigloBossEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(96).updateInterval(1).build("apiglo"));
    public static final RegistryObject<EntityType<TharKrooBossEntity>> THAR_KROO = REGISTER.register("thar_kroo", () ->
            EntityType.Builder.<TharKrooBossEntity>of(TharKrooBossEntity::new, MobCategory.MONSTER)
                    .sized(4.1F, 4.1F).clientTrackingRange(96).updateInterval(1).build("thar_kroo"));
    public static final RegistryObject<EntityType<ConquerorShadowBossEntity>> CONQUEROR_SHADOW = REGISTER.register(
            "conqueror_shadow", () -> EntityType.Builder
                    .<ConquerorShadowBossEntity>of(ConquerorShadowBossEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F).clientTrackingRange(96).updateInterval(1).build("conqueror_shadow"));
    public static final RegistryObject<EntityType<MarawTharBossEntity>> MARAWTHAR = REGISTER.register(
            "marawthar", () -> EntityType.Builder
                    .<MarawTharBossEntity>of(MarawTharBossEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.4F).clientTrackingRange(96).updateInterval(1).build("marawthar"));
    public static final RegistryObject<EntityType<KoyomiBossEntity>> KOYOMI = REGISTER.register(
            "koyo", () -> EntityType.Builder
                    .<KoyomiBossEntity>of(KoyomiBossEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(96).updateInterval(1).build("koyo"));
    public static final RegistryObject<EntityType<GariBossEntity>> GARI = REGISTER.register(
            "gari", () -> EntityType.Builder
                    .<GariBossEntity>of(GariBossEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F).clientTrackingRange(96).updateInterval(1).build("gari"));
    public static final RegistryObject<EntityType<EotharEchoEntity>> EOTHAR_ECHO = REGISTER.register(
            "eothar_echo", () -> EntityType.Builder
                    .<EotharEchoEntity>of(EotharEchoEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(96).updateInterval(10)
                    .build("eothar_echo"));
    public static final RegistryObject<EntityType<KorosEchoEntity>> KOROS_ECHO = REGISTER.register(
            "koros_echo", () -> EntityType.Builder
                    .<KorosEchoEntity>of(KorosEchoEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F).fireImmune().clientTrackingRange(96).updateInterval(10)
                    .build("koros_echo"));

    private ModEntities() {
    }
}
