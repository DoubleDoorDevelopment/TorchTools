/*
 * Copyright (c) 2014, Dries007.net
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.torchtools;

import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.doubledoordev.d3core.D3Core;
import net.doubledoordev.d3core.util.ID3Mod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.logging.log4j.Logger;

import java.util.List;

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
public class TorchTools implements ID3Mod
{
    public static final String MODID = "TorchTools";

    @Mod.Instance(MODID)
    public static TorchTools instance;

    private Logger      logger;

    public TorchTools()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    /**
     * This method is the mod. Everything else is extra
     */
    @SubscribeEvent
    public void playerInteractEventHandler(PlayerInteractEvent event)
    {
        // Server side and on block only.
        if (event.isCanceled() || event.world.isRemote || event.action != RIGHT_CLICK_BLOCK) return;
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
        if (slotStack == null || newSlot >= 9) return; //Prevents overlooping into non-hotbar slots!
        // Set current slot to new slot to fool Minecraft
        event.entityPlayer.inventory.currentItem = newSlot;
        // Debug info
        if (D3Core.debug()) logger.info("Player: " + event.entityPlayer.getDisplayName() + "\tOldSlot: " + oldSlot + "\tOldStack: " + slotStack);
        // Fake right click                                                                                                                                                   Oh look fake values :p
        boolean b = ((EntityPlayerMP) event.entityPlayer).theItemInWorldManager.activateBlockOrUseItem(event.entityPlayer, event.world, slotStack, event.x, event.y, event.z, event.face, 0.5f, 0.5f, 0.5f);
        // Remove empty stacks
        if (slotStack.stackSize <= 0) slotStack = null;
        // Debug info
        if (D3Core.debug()) logger.info("Player: " + event.entityPlayer.getDisplayName() + "\tNewSlot: " + newSlot + "\tNewStack: " + slotStack + "\tResult: " + b);
        // Set old slot back properly
        event.entityPlayer.inventory.currentItem = oldSlot;
        // Update client
        event.entityPlayer.inventory.setInventorySlotContents(newSlot, slotStack);
        ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(0, newSlot + 36, slotStack));
        // Prevent derpy doors
        event.setCanceled(true);
    }

    @Override
    public void syncConfig()
    {
        
    }

    @Override
    public void addConfigElements(List<IConfigElement> configElements)
    {

    }
}
