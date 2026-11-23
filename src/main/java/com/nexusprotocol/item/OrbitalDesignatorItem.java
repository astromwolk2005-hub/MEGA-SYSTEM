package com.nexusprotocol.item;

import com.nexusprotocol.system.OrbitalStrikeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

import java.util.List;

public class OrbitalDesignatorItem extends Item {
    public OrbitalDesignatorItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld sw) {
            HitResult hit = user.raycast(64.0, 0.0f, false);
            OrbitalStrikeManager.callStrike(sw, hit.getPos());
            user.sendMessage(Text.literal("🛰 Координаты переданы! Орбитальный удар через 3 сек...").formatted(Formatting.RED, Formatting.BOLD), true);
            user.getItemCooldownManager().set(this, 160);
        }
        return TypedActionResult.success(stack, world.isClient());
    }

    @Override
    public boolean hasGlint(ItemStack stack) { return true; }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("ПКМ: Вызвать ионный удар с орбиты (до 64 блоков)").formatted(Formatting.RED));
    }
}
