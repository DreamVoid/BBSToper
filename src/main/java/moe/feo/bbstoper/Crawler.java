package moe.feo.bbstoper;

import me.dreamvoid.bbstoper.Utils;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Crawler {
	public final List<String> ID = new ArrayList<>();
	public final List<String> Time = new ArrayList<>();
	public boolean visible = true;

	public Crawler() {
		resolveWebData();
		kickExpiredData();
	}

	public void resolveWebData() {
		String url = Utils.getMCBBSUrl();
		try {
			Document doc = Config.PROXY_ENABLE.getBoolean() ? Jsoup.connect(url).proxy(Config.PROXY_IP.getString(), Config.PROXY_PORT.getInt()).get() : Jsoup.connect(url).get();
			Elements listClass = doc.getElementsByClass("list"); // 获取一个class名为list的元素的合集

			Element list = listClass.get(0); // mcbbs顶贴列表页面只会有一个list，直接使用即可
			Element listBody = list.getElementsByTag("tbody").get(0); // tbody表示表的身体而不是表头
			for (Element rows : listBody.getElementsByTag("tr")) { // tr是表的一行
				Elements cells = rows.getElementsByTag("td"); // td表示一行的单元格，cells为单元格的合集
				String action = cells.get(2).text();
				if (!(action.equals("提升(提升卡)")||action.equals("提升(服务器/交易代理提升卡)"))) {// 这里过滤掉不是提升卡的操作
					continue;
				}
				Element idCell = cells.get(0);// 第一个单元格中包含有id
				String id = idCell.getElementsByTag("a").get(0).text();
				Element timeCell = cells.get(1);// 第二个单元格就是time了
				Element timeSpan = timeCell.getElementsByTag("span").first();// time有两种，一种在span标签里面

				// attr用于获取元素的属性值，这个值就是我们要的time
				// 6天过后的时间将直接被包含在单元格中
				String time = timeSpan != null ? timeSpan.attr("title") : timeCell.text();

				ID.add(id);
				Time.add(time);
			}
		} catch (IOException e) {// 这里经常会因为网络连接不顺畅而报错
			if (Config.DEBUG.getBoolean()) e.printStackTrace();
			BBSToper.INSTANCE.getLogger().warning(Message.FAILEDGETWEB.getString());
		} catch (IndexOutOfBoundsException e) {
			this.visible = false;
			String warn = Message.FAILEDRESOLVEWEB.getString();
			if (!warn.isEmpty()) {
				BBSToper.INSTANCE.getLogger().warning(Message.FAILEDRESOLVEWEB.getString());
			}
		}
	}

	public void kickExpiredData() {// 剔除过期的数据
		// 注意mcbbs的日期格式，月份和天数都是非零开始，小时分钟是从零开始
		SimpleDateFormat sdfm = new SimpleDateFormat("yyyy-M-d HH:mm");
		Date now = new Date();
		long validtime = Config.REWARD_PERIOD.getInt() * 24 * 60 * 60 * 1000L;// 有效期
		Date expirydate = new Date(now.getTime() - validtime);// 过期时间，如果小于这个时间则表示过期
		for (int i = 0; i < Time.size(); i++) {
			try {
				Date date = sdfm.parse(Time.get(i));
				if (date.before(expirydate)) {// 过期了
					Time.remove(i);
					ID.remove(i);
					i--;// 这里要吧序数往前退一个
				}
			} catch (ParseException e) {
				if (Config.DEBUG.getBoolean()) e.printStackTrace();
				return;
			}
		}
	}

	public void activeReward() {// 主动给玩家发奖励
		for (int i = 0; i < ID.size(); i++) {
			String bbsName = ID.get(i);
			String time = Time.get(i);
			if (!DatabaseManager.database.checkTopState(bbsName, time)) {// 如果这个记录不存在于数据库中
				String uuid = DatabaseManager.database.bbsNameCheck(bbsName);
				Poster poster = DatabaseManager.database.getPoster(uuid);
				if (uuid != null) {// 这个玩家已经绑定,这时候就可以开始对玩家进行检测了
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
					if (!offlinePlayer.isOnline() || Bukkit.getPlayer(UUID.fromString(uuid)).hasPermission("bbstoper.reward")) {
						continue; // 不在线或者没权限就跳过
					}

					Player player = Bukkit.getPlayer(UUID.fromString(uuid));

					String dateNow = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
					if (!dateNow.equals(poster.getRewardbefore())) {// 上次领奖的日期不是今天，直接将奖励次数清零
						poster.setRewardbefore(dateNow);// 奖励日期设置为今天
						poster.setRewardtime(0);
					}
					if (poster.getRewardtime() >= Config.REWARD_TIMES.getInt()) {
						continue;// 如果领奖次数已经大于设定值了，那么跳出循环
					}

					// 这时候就可以给玩家发奖励了
					new Reward(player, this, i).award();
					DatabaseManager.database.addTopState(bbsName, time);
					poster.setRewardtime(poster.getRewardtime() + 1);
					DatabaseManager.database.updatePoster(poster);// 把poster储存起来

					// 给有奖励权限且能看见此玩家(防止Vanish)的玩家广播
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (!p.canSee(player) || !p.hasPermission("bbstoper.reward")) continue;
						p.sendMessage(Message.BROADCAST.getString().replaceAll("%PLAYER%", offlinePlayer.getName()));
					}
				}
			}
		}
	}
}
