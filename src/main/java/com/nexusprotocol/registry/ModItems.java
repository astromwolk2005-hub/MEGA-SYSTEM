package com.nexusprotocol.registry;

import com.nexusprotocol.NexusMod;
import com.nexusprotocol.item.GravityGunItem;
import com.nexusprotocol.item.OrbitalDesignatorItem;
import com.nexusprotocol.item.SingularityGrenadeItem;
import com.nexusprotocol.item.StationTeleporterItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;

public class ModItems {
    public static final Item ORBITAL_DESIGNATOR = register("orbital_designator",
            new OrbitalDesignatorItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));

    public static final Item GRAVITY_GUN = register("gravity_gun",
            new GravityGunItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));

    public static final Item SINGULARITY_GRENADE = register("singularity_grenade",
            new SingularityGrenadeItem(new Item.Settings().maxCount(16).rarity(Rarity.RARE).fireproof()));

    public static final Item STATION_TELEPORTER = register("station_teleporter",
            new StationTeleporterItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof()));

    public static final ItemGroup NEXUS_GROUP = Registry.register(
            Registries.ITEM_GROUP,
            NexusMod.id("nexus_tab"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ORBITAL_DESIGNATOR))
                    .displayName(Text.translatable("itemGroup.nexusprotocol"))
                    .entries((context, entries) -> {
                        entries.add(ORBITAL_DESIGNATOR);
                        entries.add(GRAVITY_GUN);
                        entries.add(SINGULARITY_GRENADE);
                        entries.add(STATION_TELEPORTER);
                    })
                    .build()
    );

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, NexusMod.id(name), item);
    }

    public static void register() {}
}
