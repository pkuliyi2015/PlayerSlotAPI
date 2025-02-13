package pku.yim.magiclibs.magicslotapi.hook;

import com.germ.germplugin.GermPlugin;
import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.GermSlotAPI;
import com.germ.germplugin.api.event.gui.GermGuiSlotPreClickEvent;
import com.germ.germplugin.api.event.gui.GermGuiSlotSavedEvent;
import pku.yim.magiclibs.magicslotapi.MagicSlotAPI;
import pku.yim.magiclibs.magicslotapi.event.SlotUpdateEvent;
import pku.yim.magiclibs.magicslotapi.event.UpdateTrigger;
import pku.yim.magiclibs.magicslotapi.slot.PlayerSlot;
import pku.yim.magiclibs.magicslotapi.slot.impl.GermPluginSlot;
import pku.yim.magiclibs.magicslotapi.util.Events;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GermPluginHook {

    public GermPlugin instance;

    public GermPluginHook() {
        this.instance = GermPlugin.getPlugin();
    }

    public static void register() {
        Map<String, PlayerSlot> slotMap = MagicSlotAPI.getAPI().getSlotMap();
        try {
            Events.subscribe(GermGuiSlotSavedEvent.class, event -> {
                String identifier = event.getIdentity();
                if (slotMap.containsKey(identifier)) {
                    PlayerSlot slot = slotMap.get(identifier);
                    SlotUpdateEvent update = new SlotUpdateEvent(UpdateTrigger.GERM_PLUGIN, event.getPlayer(), slot, event.getOldItemStack(), event.getNewItemStack());
                    update.setUpdateImmediately();
                    Bukkit.getPluginManager().callEvent(update);
                }
            });
            GermPluginSlot.disableCacheUpdate();
        } catch (Throwable e) {
            Events.subscribe(GermGuiSlotPreClickEvent.class, event -> {
                // 旧版萌芽获取不到新旧物品, 因此需要延时检测
                String identifier = event.getSlotIdentity();
                if (slotMap.containsKey(identifier)) {
                    PlayerSlot slot = slotMap.get(identifier);
                    ItemStack oldItem = event.getSlot();
                    ItemStack newItem = event.getCursor();
                    if (oldItem.equals(newItem)) {
                        return;
                    }
                    SlotUpdateEvent update = new SlotUpdateEvent(UpdateTrigger.GERM_PLUGIN, event.getPlayer(), slot, oldItem, newItem);
                    Bukkit.getPluginManager().callEvent(update);
                    if (update.isCancelled()) {
                        event.setCancelled(true);
                    }
                }
            });
        }
    }

    public ItemStack getItemFromSlot(Player player, String identity) {
        return GermSlotAPI.getItemStackFromIdentity(player, identity);
    }

    public void setItemToSlot(Player player, String identifier, ItemStack toBePuttedItem) {
        GermSlotAPI.saveItemStackToIdentity(player, identifier, toBePuttedItem);
        GermPacketAPI.sendSlotItemStack(player, identifier, toBePuttedItem);
    }

}
