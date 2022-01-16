package net.doubledoordev.torchtools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod("torchtools")
public class TorchTools
{
    public static final String MODID = "torchtools";
    private static final Logger LOGGER = LogManager.getLogger();
    //TODO: Make this configurable, It can be done as the slot shifting is done entirely client side.
    private static final int[] slots = {8, 2, 3, 4, 5, 6, 7, 8, -1};

    public TorchTools()
    {
        //ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, TorchToolsConfig.spec);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void torchingTools(PlayerInteractEvent.RightClickBlock event)
    {
        if (event.isCanceled() && event.getSide() == LogicalSide.SERVER) return;

        Item heldUsedItem = event.getItemStack().getItem();
        Tag<Item> passClickItemTag = ItemTags.getAllTags().getTagOrEmpty(new ResourceLocation(MODID, "place_pass_items"));
        Player player = event.getPlayer();
        InteractionHand handUsed = event.getHand();

        // Use Tags with class check.
        //TODO: Needs a config line for valid items as tags always come from servers. Thus this breaks the tag part unless the owner adds tags...(?)
        //TODO: Fix the stupid pass through triggering blocks like doors to open and close at the same time due to stacked interactions...
        //TODO: Set this to use a config to check for tool?
        //TODO: Block blacklist to cancel interactions on..? Can't be tags.
        if (passClickItemTag.contains(heldUsedItem) || heldUsedItem instanceof TieredItem)
        {
            // Save old slot id
            int originSlot = player.getInventory().selected;
            // Make sure we are within the 0-8 hotbar slots.
            if (originSlot < 0 || originSlot > 8) return;
            // Get the new slot id
            int newSlot = slots[originSlot];
            // Jump out if we hit the last slot.
            if (newSlot < 0) return;
            // Get new item
            ItemStack newSlotStack = player.getInventory().getItem(newSlot);
            // No empty please
            if (newSlotStack == ItemStack.EMPTY) return;
            // Set current slot to new slot to fool Minecraft
            player.getInventory().selected = newSlot;
            // Right click with the new slot & item from the client. This handles the call to the server for us. Entirely faking the action of switching and using.
            if (player.isLocalPlayer())
            {
                //TODO: REMOVE Attempts at controlling duplicate clicks.
                event.setCanceled(true);
                Minecraft.getInstance().gameMode.useItemOn(Minecraft.getInstance().player, Minecraft.getInstance().level, handUsed, event.getHitVec());
                // Swing the hand otherwise we stiff arm everything.
                player.swing(handUsed);
                //TODO: REMOVE Attempts at controlling duplicate clicks.
                event.setCanceled(true);
            }
            // Set back to origin.
            player.getInventory().selected = originSlot;
            //TODO: REMOVE Attempts at controlling duplicate clicks.
            event.setCanceled(true);
        }
    }
}
