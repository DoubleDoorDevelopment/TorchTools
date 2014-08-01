package net.dries007.torchtools;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK;

/**
 * Main mod file
 *
 * Thanks for the idea Tinkers Construct
 *
 * @author Dries007
 */
@Mod(modid = TorchTools.MODID, name = TorchTools.MODID)
public class TorchTools
{
    public static final String MODID = "TorchTools";

    @Mod.Instance(MODID)
    public static TorchTools instance;

    public TorchTools()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

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
        // Fake right click                                                                                                                                                   Oh look fake values :p
        ((EntityPlayerMP) event.entityPlayer).theItemInWorldManager.activateBlockOrUseItem(event.entityPlayer, event.world, slotStack, event.x, event.y, event.z, event.face, 0.5f, 0.5f, 0.5f);
        // Set old slot back properly
        event.entityPlayer.inventory.currentItem = oldSlot;
        // Update client
        ((EntityPlayerMP) event.entityPlayer).playerNetServerHandler.sendPacket(new S2FPacketSetSlot(event.entityPlayer.openContainer.windowId, newSlot + 36, slotStack));
    }
}
