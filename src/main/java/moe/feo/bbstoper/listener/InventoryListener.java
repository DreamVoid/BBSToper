package moe.feo.bbstoper.listener;

import me.dreamvoid.bbstoper.Utils;
import moe.feo.bbstoper.gui.GUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import moe.feo.bbstoper.CLI;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.config.Config;

import java.util.Arrays;
import java.util.UUID;

public class InventoryListener implements Listener {

	public InventoryListener(Plugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) return; // 如果不是玩家操作的，返回
		Player player = (Player) event.getWhoClicked();
		InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
		if (!(holder instanceof GUI.ToperHolder)) return; // 确认操作的是此插件的GUI

		event.setCancelled(true);

		switch (event.getRawSlot()){
			case 12: { // 点击绑定
				if (Config.GUI_USECHATGETID.getBoolean()) {
					player.closeInventory();
					UUID uid = player.getUniqueId();
					synchronized (IDListener.lock) { // 线程锁防止异步错位修改
						IDListener rgListener = IDListener.map.get(uid);
						// 如果这个玩家没有一个监听器
						if (rgListener == null) {
							new IDListener(player.getUniqueId()).register();// 为此玩家创建一个监听器
							String keywords = Arrays.toString(Config.GUI_CANCELKEYWORDS.getStringList().toArray());
							player.sendMessage(Message.PREFIX.getString() + Message.ENTER.getString().replaceAll("%KEYWORD%", keywords));
						}
					}
				}
				if (!Config.GUI_USECHATGETID.getBoolean()) {
					player.closeInventory();
					player.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
				}
				break;
			}
			case 13:{
				player.closeInventory();
				String[] args = { "reward" };
				CLI.getInstance().onCommand(player, null, null, args);
				break;
			}
			case 22:{ // 获取链接
				player.closeInventory();
				for (String msg : Message.CLICKPOSTICON.getStringList()) {
					String url = Utils.getMCBBSUrl(true);
					player.sendMessage(msg.replaceAll("%PAGE%", url));
				}
				break;
			}
		}
	}
}
