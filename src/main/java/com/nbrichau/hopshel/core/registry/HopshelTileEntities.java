package com.nbrichau.hopshel.core.registry;

import com.nbrichau.hopshel.common.tileentity.BurrowTileEntity;
import com.nbrichau.hopshel.core.HopshelMod;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelTileEntities {

	public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, HopshelMod.MODID);

	public static final RegistryObject<TileEntityType<?>> HOPSHEL_BURROW = TILES.register("hopshel_burrow", () -> TileEntityType.Builder.of(BurrowTileEntity::new, HopshelBlocks.HOPSHEL_BURROW.get()).build(null));

}
