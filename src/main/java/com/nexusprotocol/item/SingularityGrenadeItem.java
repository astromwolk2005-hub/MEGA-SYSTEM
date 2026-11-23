package com.nexusprotocol.item;

import com.nexusprotocol.system.SingularityManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class SingularityGrenadeItem extends Item {
    public SingularityGrenadeItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            Vec3d spawnPos = user.getEyePos().add(user.getRotationVector().multiply(10.0));
            SingularityManager.spawn(sw, spawnPos);
            if (!user.isCreative()) stack.decrement(1);
            user.getItemCooldownManager().set(this, 60);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public boolean hasGlint(ItemStack stack) { return true; }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("ПКМ: Создаёт чёрную дыру впереди (затягивает и взрывается)").formatted(Formatting.LIGHT_PURPLE));
    }
}
