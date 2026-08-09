package io.github.finalparadox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.finalparadox.FinalParadox;
import io.github.finalparadox.entity.DroneEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Low-profile tactical quadcopter rendered around the below-body gun camera.
 * A belly-mounted machine gun pitches with the pilot look; geometry and UVs
 * are original to Final Paradox.
 */
public final class DroneModel extends EntityModel<DroneEntity> {
    public static final ModelLayerLocation DRONE_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(FinalParadox.MOD_ID, "recon_drone"),
            "main");

    private static final int MAX_BOMBS = 8;
    private static final float ROTOR_SPEED = 3.25F;

    private final ModelPart root;
    private final ModelPart frame;
    private final ModelPart camera;
    private final ModelPart leftGun;
    private final ModelPart rightGun;
    private final ModelPart leftBarrels;
    private final ModelPart rightBarrels;
    private final ModelPart[] rotors;
    private final ModelPart[] bombs;

    public DroneModel(ModelPart root) {
        this.root = root;
        frame = root.getChild("frame");
        camera = frame.getChild("camera");
        leftGun = frame.getChild("left_gun");
        rightGun = frame.getChild("right_gun");
        leftBarrels = leftGun.getChild("barrel_cluster");
        rightBarrels = rightGun.getChild("barrel_cluster");
        rotors = new ModelPart[]{
                frame.getChild("front_left").getChild("rotor"),
                frame.getChild("front_right").getChild("rotor"),
                frame.getChild("rear_left").getChild("rotor"),
                frame.getChild("rear_right").getChild("rotor")
        };
        ModelPart payload = frame.getChild("payload");
        bombs = new ModelPart[MAX_BOMBS];
        for (int i = 0; i < MAX_BOMBS; i++) {
            bombs[i] = payload.getChild("bomb_" + i);
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition frame = root.addOrReplaceChild(
                "frame", CubeListBuilder.create(), PartPose.offset(0.0F, -9.5F, 0.0F));

        addFuselage(frame);
        addArm(frame, "front_left", 45.0F);
        addArm(frame, "front_right", -45.0F);
        addArm(frame, "rear_left", 135.0F);
        addArm(frame, "rear_right", -135.0F);
        addCamera(frame);
        addGuns(frame);
        addPayload(frame);

        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addFuselage(PartDefinition frame) {
        frame.addOrReplaceChild(
                "fuselage",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -1.25F, -4.0F, 8.0F, 2.5F, 8.0F),
                PartPose.ZERO);
        frame.addOrReplaceChild(
                "top_plate",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-2.5F, 1.25F, -2.5F, 5.0F, 0.5F, 5.0F),
                PartPose.ZERO);
        frame.addOrReplaceChild(
                "nose",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-2.5F, -1.0F, -6.5F, 5.0F, 2.0F, 2.5F),
                PartPose.ZERO);
        frame.addOrReplaceChild(
                "rear_deck",
                CubeListBuilder.create()
                        .texOffs(16, 12)
                        .addBox(-2.0F, -0.5F, 4.0F, 4.0F, 1.0F, 2.0F),
                PartPose.ZERO);
        frame.addOrReplaceChild(
                "belly_plate",
                CubeListBuilder.create()
                        .texOffs(30, 12)
                        .addBox(-2.5F, -2.0F, -3.0F, 5.0F, 0.75F, 6.0F),
                PartPose.ZERO);

        // Solid lateral hangers bridging each belly edge to its Gatling mount,
        // so the guns no longer read as thin wires hanging off the hull.
        frame.addOrReplaceChild(
                "hanger_left",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-4.4F, -2.0F, -0.35F, 1.9F, 0.4F, 0.7F),
                PartPose.ZERO);
        frame.addOrReplaceChild(
                "hanger_right",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(2.5F, -2.0F, -0.35F, 1.9F, 0.4F, 0.7F),
                PartPose.ZERO);

        PartDefinition antenna = frame.addOrReplaceChild(
                "antenna", CubeListBuilder.create(), PartPose.offset(2.25F, 1.75F, 2.5F));
        antenna.addOrReplaceChild(
                "mast",
                CubeListBuilder.create()
                        .texOffs(40, 20)
                        .addBox(-0.35F, 0.0F, -0.35F, 0.7F, 2.25F, 0.7F),
                PartPose.ZERO);
        antenna.addOrReplaceChild(
                "cap",
                CubeListBuilder.create()
                        .texOffs(45, 20)
                        .addBox(-0.65F, 2.0F, -0.65F, 1.3F, 0.5F, 1.3F),
                PartPose.ZERO);
    }

    private static void addArm(PartDefinition frame, String name, float yawDegrees) {
        PartDefinition arm = frame.addOrReplaceChild(
                name, CubeListBuilder.create(), PartPose.rotation(0.0F, yawDegrees * Mth.DEG_TO_RAD, 0.0F));
        arm.addOrReplaceChild(
                "strut",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-0.8F, -0.5F, -7.2F, 1.6F, 1.0F, 6.0F),
                PartPose.ZERO);
        arm.addOrReplaceChild(
                "motor",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-1.0F, -0.75F, -1.0F, 2.0F, 1.5F, 2.0F),
                PartPose.offset(0.0F, 0.25F, -7.2F));

        PartDefinition rotor = arm.addOrReplaceChild(
                "rotor", CubeListBuilder.create(), PartPose.offset(0.0F, 1.2F, -7.2F));
        rotor.addOrReplaceChild(
                "hub",
                CubeListBuilder.create()
                        .texOffs(26, 20)
                        .addBox(-0.5F, -0.25F, -0.5F, 1.0F, 0.5F, 1.0F),
                PartPose.ZERO);
        rotor.addOrReplaceChild(
                "blade_left",
                CubeListBuilder.create()
                        .texOffs(32, 20)
                        .addBox(-4.0F, -0.25F, -0.35F, 3.5F, 0.5F, 0.7F),
                PartPose.rotation(0.0F, 8.0F * Mth.DEG_TO_RAD, 0.0F));
        rotor.addOrReplaceChild(
                "blade_right",
                CubeListBuilder.create()
                        .texOffs(32, 20)
                        .addBox(0.5F, -0.25F, -0.35F, 3.5F, 0.5F, 0.7F),
                PartPose.rotation(0.0F, -8.0F * Mth.DEG_TO_RAD, 0.0F));
    }

    private static void addCamera(PartDefinition frame) {
        PartDefinition camera = frame.addOrReplaceChild(
                "camera", CubeListBuilder.create(), PartPose.offset(0.0F, -2.8F, -4.7F));
        camera.addOrReplaceChild(
                "mount",
                CubeListBuilder.create()
                        .texOffs(52, 0)
                        .addBox(-2.0F, 0.0F, -0.5F, 4.0F, 0.6F, 1.0F),
                PartPose.offset(0.0F, 1.0F, 0.7F));
        camera.addOrReplaceChild(
                "housing",
                CubeListBuilder.create()
                        .texOffs(7, 30)
                        .addBox(-1.5F, -1.0F, -1.25F, 3.0F, 2.0F, 2.5F),
                PartPose.ZERO);
        camera.addOrReplaceChild(
                "lens",
                CubeListBuilder.create()
                        .texOffs(20, 30)
                        .addBox(-1.0F, -0.7F, -2.25F, 2.0F, 1.4F, 1.0F),
                PartPose.ZERO);
    }

    private static void addGuns(PartDefinition frame) {
        addGun(frame, "left_gun", -4.8F, -0.5F);
        addGun(frame, "right_gun", 4.8F, 0.5F);
    }

    private static void addGun(
            PartDefinition frame, String name, float xOffset, float inwardYawDegrees) {
        PartDefinition gun = frame.addOrReplaceChild(
                name, CubeListBuilder.create(),
                PartPose.offsetAndRotation(
                        xOffset, -7.0F, -1.0F,
                        0.0F, inwardYawDegrees * Mth.DEG_TO_RAD, 0.0F));
        gun.addOrReplaceChild(
                "mount",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-0.8F, -0.6F, -0.8F, 1.6F, 5.8F, 1.6F),
                PartPose.ZERO);
        gun.addOrReplaceChild(
                "receiver",
                CubeListBuilder.create()
                        .texOffs(0, 12)
                        .addBox(-1.15F, -0.8F, -2.0F, 2.3F, 1.6F, 3.5F),
                PartPose.ZERO);

        CubeListBuilder barrelCluster = CubeListBuilder.create();
        for (int tube = 0; tube < 6; tube++) {
            double angle = tube * Math.PI / 3.0D;
            float x = (float) Math.cos(angle) * 0.62F;
            float y = (float) Math.sin(angle) * 0.62F;
            barrelCluster.texOffs(0, 20).addBox(
                    x - 0.22F, y - 0.22F, -8.0F,
                    0.44F, 0.44F, 6.0F);
        }
        barrelCluster.texOffs(26, 20)
                .addBox(-0.28F, -0.28F, -8.15F, 0.56F, 0.56F, 6.3F);
        gun.addOrReplaceChild(
                "barrel_cluster", barrelCluster, PartPose.offset(0.0F, -1.3F, 0.0F));
        gun.addOrReplaceChild(
                "magazine",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-0.75F, -2.5F, -1.5F, 1.5F, 2.3F, 1.1F),
                PartPose.ZERO);
        gun.addOrReplaceChild(
                "grip",
                CubeListBuilder.create()
                        .texOffs(16, 20)
                        .addBox(-0.45F, -1.5F, 1.2F, 0.9F, 1.5F, 0.9F),
                PartPose.ZERO);
    }

    private static void addPayload(PartDefinition frame) {
        PartDefinition payload = frame.addOrReplaceChild(
                "payload", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));
        int[][] bombUvs = {
                {27, 30}, {40, 30}, {0, 37}, {13, 37},
                {26, 37}, {39, 37}, {0, 44}, {13, 44}
        };
        for (int row = 0; row < 4; row++) {
            float z = -3.0F + row * 2.0F;
            for (int side = 0; side < 2; side++) {
                int index = row * 2 + side;
                float x = side == 0 ? -2.1F : 2.1F;
                payload.addOrReplaceChild(
                        "bomb_" + index,
                        CubeListBuilder.create()
                                .texOffs(bombUvs[index][0], bombUvs[index][1])
                                .addBox(-0.625F, -0.625F, -1.0F, 1.25F, 1.25F, 2.0F),
                        PartPose.offset(x, 0.0F, z));
            }
        }
    }

    @Override
    public void setupAnim(DroneEntity entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        root.y = Mth.sin(ageInTicks * 0.35F) * 0.08F;
        frame.zRot = Mth.clamp(Mth.wrapDegrees(entity.getYRot() - entity.yRotO)
                * 0.008F, -0.14F, 0.14F);
        frame.xRot = Mth.clamp((float) -entity.getDeltaMovement().y
                * 0.3F, -0.12F, 0.12F);
        float modelPitch = DroneModelMath.modelPitchRadians(headPitch);
        camera.xRot = modelPitch;
        leftGun.xRot = modelPitch;
        rightGun.xRot = modelPitch;

        float barrelSpin = switch (entity.getGatlingState()) {
            case DroneEntity.GATLING_WARMING -> ageInTicks * 0.45F;
            case DroneEntity.GATLING_FIRING -> ageInTicks * 1.8F;
            default -> 0.0F;
        };
        leftBarrels.zRot = barrelSpin;
        rightBarrels.zRot = -barrelSpin;

        rotors[0].yRot = ageInTicks * ROTOR_SPEED;
        rotors[1].yRot = -ageInTicks * ROTOR_SPEED;
        rotors[2].yRot = -ageInTicks * ROTOR_SPEED;
        rotors[3].yRot = ageInTicks * ROTOR_SPEED;

        int bombCount = Mth.clamp(entity.getBombs(), 0, MAX_BOMBS);
        for (int i = 0; i < MAX_BOMBS; i++) {
            bombs[i].visible = i < bombCount;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(pose, consumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
