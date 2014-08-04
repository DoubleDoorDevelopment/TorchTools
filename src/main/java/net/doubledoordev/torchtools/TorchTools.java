/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 DoubleDoorDevelopment
 *
 * I can't demand this, but I ask for respect and gratitude for the time and effort
 * put into the project by all developers, testers, designers and documenters. ~~Dries007
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.doubledoordev.torchtools;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.util.DevPerks;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;

/**
 * Main mod file
 *
 * Thanks for the idea Tinkers Construct
 *
 * @author Dries007
 * @author DoubleDoorDevelopment
 */
@Mod(modid = TorchTools.MODID, name = TorchTools.MODID)
public class TorchTools
{
    public static final String MODID = "TorchTools";

    @Mod.Instance(MODID)
    public static TorchTools instance;

    private boolean      debug = false;
    private Logger      logger;

    public TorchTools()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        Configuration configuration = new Configuration(event.getSuggestedConfigurationFile());
        debug = configuration.getBoolean("debug", MODID, debug, "Enable debug, use when errors or weird behaviour happens.");
        if (configuration.getBoolean("sillyness", MODID, true, "Disable sillyness only if you want to piss of the devs XD")) MinecraftForge.EVENT_BUS.register(new DevPerks(debug));
        if (configuration.hasChanged()) configuration.save();
    }

    /**
     * This method is the mod. Everything else is extra
     */
    @SubscribeEvent
    public void playerInteractEventHandler(PlayerInteractEvent event)
    {
        // Server side and on block only.
        if (event.world.isRemote || event.action != RIGHT_CLICK_BLOCK) return;
        ItemStack heldItem = event.entityPlayer.inventory.getCurrentItem();
        // Only tools, not null
        if (heldItem == null || !(heldItem.getItem() instanceof ItemTool)) return;
        // Save old slot id
        int oldSlot = event.entityPlayer.inventory.currentItem;
        // Calculate new slot id
        int newSlot = oldSlot == 0 ? 8 : oldSlot + 1;
        // Get new item
        ItemStack slotStack = event.entityPlayer.inventory.getStackInSlot(newSlot);
        // No null please
        if (slotStack == null) return;
        // Set current slot to new slot to fool Minecraft
        event.entityPlayer.inventory.currentItem = newSlot;
        // Debug info
        if (debug) logger.info("Player: " + event.entityPlayer.getDisplayName() + "\tOldSlot: " + oldSlot + "\tOldStack: " + slotStack);
        // Fake right click                                                                                                                                                   Oh look fake values :p
        boolean b = ((EntityPlayerMP) event.entityPlayer).theItemInWorldManager.activateBlockOrUseItem(event.entityPlayer, event.world, slotStack, event.x, event.y, event.z, event.face, 0.5f, 0.5f, 0.5f);
        // Remove empty stacks
        if (slotStack.stackSize <= 0) slotStack = null;
        // Debug info
        if (debug) logger.info("Player: " + event.entityPlayer.getDisplayName() + "\tNewSlot: " + newSlot + "\tNewStack: " + slotStack + "\tResult: " + b);
        // Set old slot back properly
        event.entityPlayer.inventory.currentItem = oldSlot;
        // Update client
        event.entityPlayer.inventory.setInventorySlotContents(newSlot, slotStack);
        ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(0, newSlot + 36, slotStack));
    }
}
