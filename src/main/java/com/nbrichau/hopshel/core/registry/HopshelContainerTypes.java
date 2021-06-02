package com.nbrichau.hopshel.core.registry;

import com.nbrichau.hopshel.common.inventory.container.HopshelContainer;
import com.nbrichau.hopshel.core.HopshelMod;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class HopshelContainerTypes {

	public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, HopshelMod.MODID);

	public static final RegistryObject<ContainerType<HopshelContainer>> HOPSHEL_CONTAINER = CONTAINERS.register("hopshel_container",
			() -> IForgeContainerType.create((windowId, inv, data) -> new HopshelContainer(windowId, inv.player.getCommandSenderWorld(), inv, inv.player, data.readInt())));

}
