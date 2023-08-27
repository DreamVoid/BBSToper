package moe.feo.bbstoper.gui;

import java.util.ArrayList;
import java.util.List;

import moe.feo.bbstoper.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.utils.Crawler;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.Poster;
import moe.feo.bbstoper.utils.Util;

public class GUI {
	private Inventory inventory;
	
	public static String getTitle() {// 获取插件的gui标题必须用此方法，因为用户可能会修改gui标题
		return Message.GUI_TITLE.getString().replaceAll("%PREFIX%", Message.PREFIX.getString());
	}

	public GUI(Player player) {
		createGui(player);
		Bukkit.getScheduler().runTask(BBSToper.INSTANCE, () -> player.openInventory(inventory));
	}
	
	public class ToperHolder implements InventoryHolder {// 定义一个Holder用于识别此插件的GUI
		@Override
		public Inventory getInventory() {
			return getGui();
		}
	}

	@SuppressWarnings("deprecation")
	public void createGui(Player player) {
		inventory = Bukkit.createInventory(new ToperHolder(), InventoryType.CHEST, getTitle());
		for (int i = 0; i < inventory.getSize(); i++) {// 设置边框
			if (i > 9 && i < 17)
				continue;
			inventory.setItem(i, getRandomPane());
		}

		ItemStack skull;
		try {
			skull = new ItemStack(Material.SKULL_ITEM, 1, (short) SkullType.PLAYER.ordinal());
		} catch (NoSuchFieldError e) {// 某些高版本服务端不兼容旧版写法
			skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
		}
		SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();// 玩家头颅
		if (Config.GUI_DISPLAYHEADSKIN.getBoolean()) {// 如果开启了头颅显示，才会设置头颅的所有者
			try {
				skullMeta.setOwningPlayer(player);
			} catch (NoSuchMethodError e) {// 这里为了照顾低版本
				skullMeta.setOwner(player.getName());
			}
		}
		skullMeta.setDisplayName(Message.GUI_SKULL.getString().replaceAll("%PLAYER%", player.getName()));
		List<String> skullLores = new ArrayList<>();
		Poster poster = DatabaseManager.database.getPoster(player.getUniqueId().toString());
		if (poster != null) {
			skullLores.add(Message.GUI_BBSID.getString().replaceAll("%BBSID%", poster.getBbsname()));
			skullLores.add(Message.GUI_POSTTIMES.getString().replaceAll("%TIMES%", String.valueOf(poster.getTopStates().size())));
			skullLores.add(Message.GUI_CLICKREBOUND.getString());
		} else {
			skullLores.add(Message.GUI_NOTBOUND.getString());
			skullLores.add(Message.GUI_CLICKBOUND.getString());
		}
		skullMeta.setLore(skullLores);
		skull.setItemMeta(skullMeta);
		inventory.setItem(12, skull);

		ItemStack sunflower;
		try {
			sunflower = new ItemStack(Material.DOUBLE_PLANT);
		} catch (NoSuchFieldError e) {// 某些高版本服务端不兼容旧版写法
			sunflower = new ItemStack(Material.getMaterial("SUNFLOWER"));
		}
		ItemMeta sunflowerMeta = sunflower.getItemMeta();
		sunflowerMeta.setDisplayName(Message.GUI_REWARDS.getString());
		List<String> sunflowerLores = new ArrayList<>(Message.GUI_REWARDSINFO.getStringList());// 自定义奖励信息
		if (sunflowerLores.isEmpty()) {// 如果没有自定义奖励信息
			sunflowerLores.addAll(Config.REWARD_COMMANDS.getStringList());// 直接显示命令
			if (Config.REWARD_INCENTIVEREWARD_ENABLE.getBoolean()) {
				sunflowerLores.add(Message.GUI_INCENTIVEREWARDS.getString());// 激励奖励
				sunflowerLores.addAll(Config.REWARD_INCENTIVEREWARD_COMMANDS.getStringList());// 激励奖励命令
			}
			if (Config.REWARD_OFFDAYREWARD_ENABLE.getBoolean()) {
				sunflowerLores.add(Message.GUI_OFFDAYREWARDS.getString());// 休息日奖励
				sunflowerLores.addAll(Config.REWARD_OFFDAYREWARD_COMMANDS.getStringList()); // 休息日奖励命令
			}
		}
		sunflowerLores.add(Message.GUI_CLICKGET.getString());
		sunflowerMeta.setLore(sunflowerLores);
		sunflower.setItemMeta(sunflowerMeta);
		inventory.setItem(13, sunflower);

		ItemStack star = new ItemStack(Material.NETHER_STAR);
		ItemMeta starMeta = star.getItemMeta();
		starMeta.setDisplayName(Message.GUI_TOPS.getString());
		List<String> starLore = new ArrayList<>();
		List<Poster> posters = DatabaseManager.database.getTopPosters();
		for (int i = 0; i < posters.size(); i++) {
			if (i >= Config.GUI_TOPPLAYERS.getInt())
				break;
			starLore.add(Message.POSTERPLAYER.getString() + ":" + posters.get(i).getName() + " "
					+ Message.POSTERID.getString() + ":" + posters.get(i).getBbsname() + " "
					+ Message.POSTERNUM.getString() + ":" + posters.get(i).getCount());
		}
		starMeta.setLore(starLore);
		star.setItemMeta(starMeta);
		inventory.setItem(14, star);

		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta compassMeta = compass.getItemMeta();
		compassMeta.setDisplayName(Message.GUI_PAGESTATE.getString());
		List<String> compassLore = new ArrayList<>();
		compassLore.add(Message.GUI_PAGEID.getString().replaceAll("%PAGEID%", Config.MCBBS_URL.getString()));
		Crawler crawler = new Crawler();
		// 如果帖子可视，就获取帖子最近一次顶贴
		// 如果从没有人顶帖，就以“----”代替上次顶帖时间(原来不加判断直接get会报索引范围错误)
		compassLore.add(crawler.visible ?
				Message.GUI_LASTPOST.getString().replaceAll("%TIME%", crawler.Time.size() > 0 ? crawler.Time.get(0) : "----") :
				Message.GUI_PAGENOTVISIBLE.getString());

		String extra = Util.getExtraReward(crawler);
		if (extra != null) {
			String extraRewards = Message.GUI_EXTRAREWARDS.getString().replaceAll("%EXTRA%", extra);
			compassLore.add(extraRewards);
		}
		compassLore.add(Message.GUI_CLICKOPEN.getString());
		compassMeta.setLore(compassLore);
		compass.setItemMeta(compassMeta);
		inventory.setItem(22, compass);
	}

	/**
	 * 获取随机一种颜色的玻璃板
	 * @return 玻璃板物品
	 */
	public ItemStack getRandomPane() {
		short data = (short)(Math.random()* 16);// 这会随机取出0-15的数据值
		while (data == 8) {// 8号亮灰色染色玻璃板根本没有颜色
			data = (short)(Math.random()* 16);
		}
		ItemStack frame;
		try {
			frame = new ItemStack(Material.STAINED_GLASS_PANE, 1, data);
		} catch (NoSuchFieldError e) {// 某些高版本服务端不兼容旧版写法
			String[] glassPanes = {
					"WHITE_STAINED_GLASS_PANE",
					"ORANGE_STAINED_GLASS_PANE",
					"MAGENTA_STAINED_GLASS_PANE",
					"LIGHT_BLUE_STAINED_GLASS_PANE",
					"YELLOW_STAINED_GLASS_PANE",
					"LIME_STAINED_GLASS_PANE",
					"PINK_STAINED_GLASS_PANE",
					"GRAY_STAINED_GLASS_PANE",
					"LIGHT_GRAY_STAINED_GLASS_PANE",
					"CYAN_STAINED_GLASS_PANE",
					"PURPLE_STAINED_GLASS_PANE",
					"BLUE_STAINED_GLASS_PANE",
					"BROWN_STAINED_GLASS_PANE",
					"GREEN_STAINED_GLASS_PANE",
					"RED_STAINED_GLASS_PANE",
					"BLACK_STAINED_GLASS_PANE"};
			frame = new ItemStack(Material.getMaterial(glassPanes[data]), 1);
		}
		ItemMeta frameMeta = frame.getItemMeta();
		frameMeta.setDisplayName(Message.GUI_FRAME.getString());
		frame.setItemMeta(frameMeta);
		return frame;
	}

	public Inventory getGui() {
		return inventory;
	}
}
