package io.github.finalparadox.block;

import io.github.finalparadox.arena.ArenaSandboxTravelService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Small table-shaped block whose arena identity is fixed at registration. */
public final class ArenaSandboxBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.or(
            box(2, 8, 2, 14, 11, 14),
            box(3, 0, 3, 5, 8, 5),
            box(11, 0, 3, 13, 8, 5),
            box(3, 0, 11, 5, 8, 13),
            box(11, 0, 11, 13, 8, 13));

    private final String arenaId;

    public ArenaSandboxBlock(String arenaId, Properties properties) {
        super(properties);
        this.arenaId = arenaId;
    }

    public String arenaId() {
        return arenaId;
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
        return ArenaSandboxTravelService.request(serverPlayer, arenaId)
                ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context
    ) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
