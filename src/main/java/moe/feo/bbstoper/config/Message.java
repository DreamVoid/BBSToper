package moe.feo.bbstoper.config;

import com.google.common.base.Charsets;
import moe.feo.bbstoper.BBSToper;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public enum Message {
	PREFIX("chat.prefix"),
	RELOAD("chat.reload"),
	SQL_EXCEPTION("chat.sql-exception"),
	QUERY_COOLDOWN("chat.query-cooldown"),

	POSTERID("chat.posterid"),
	POSTERNUM("chat.posternum"),
	OVERPAGE("chat.overpage"),

	POSTERTIME("chat.postertime"),
	PAGEINFO("chat.pageinfo"),
	NOPOSTER("chat.noposter"),

	POSTERPLAYER("chat.posterplayer"),
	POSTERTOTAL("chat.postertotal"),
	PAGEINFOTOP("chat.pageinfotop"),
	NOPLAYER("chat.noplayer"),

	NOTBOUND("chat.reward.notbound"),
	NOPOST("chat.reward.nopost"),
	OVERTIME("chat.reward.overtime"),
	WAITAMIN("chat.reward.waitamin"),
	INTERVALTOOSHORT("chat.reward.intervaltooshort"),
	REWARD("chat.reward.reward"),
	EXTRAREWARD("chat.reward.extrareward"),
	REWARDGIVED("chat.reward.rewardgived"),
	BROADCAST("chat.reward.broadcast"),

	ENTER("chat.bind.enter"),
	CANCELED("chat.bind.canceled"),
	REPEAT("chat.bind.repeat"),
	NOTSAME("chat.bind.notsame"),
	ONCOOLDOWN("chat.bind.oncooldown"),
	SAMEBIND("chat.bind.samebind"),
	OWNSAMEBIND("chat.bind.ownsamebind"),
	BINDINGSUCCESS("chat.bind.bindingsuccess"),

	IDOWNER("chat.check.idowner"),
	IDNOTFOUND("chat.check.idnotfound"),
	OWNERID("chat.check.ownerid"),
	OWNERNOTFOUND("chat.check.ownernotfound"),

	DELETESUCCESS("chat.delete.deletesuccess"),

	NOPERMISSION("chat.other.nopermission"),
	INVALID("chat.other.invalid"),
	INVALIDNUM("chat.other.invalidnum"),
	PLAYERCMD("chat.other.playercmd"),
	PAGENOTVISIBLE("chat.other.pagenotvisible"),
	NONE("chat.other.none"),
	FAILEDGETWEB("chat.other.failedgetweb"),
	FAILEDRESOLVEWEB("chat.other.failedresolveweb"),
	FAILEDUNINSTALLMO("chat.other.faileduninstallmo"),

	GUI_TITLE("gui.title"),
	GUI_FRAME("gui.frame"),
	GUI_SKULL("gui.skull"),
	GUI_NOTBOUND("gui.notbound"),
	GUI_CLICKBOUND("gui.clickbound"),
	GUI_CLICKREBOUND("gui.clickrebound"),
	GUI_BBSID("gui.bbsid"),
	GUI_POSTTIMES("gui.posttimes"),
	GUI_REWARDS("gui.rewards"),
	GUI_INCENTIVEREWARDS("gui.incentiverewards"),
	GUI_OFFDAYREWARDS("gui.offdayrewards"),
	GUI_CLICKGET("gui.clickget"),
	GUI_TOPS("gui.tops"),
	GUI_PAGESTATE("gui.pagestate"),
	GUI_PAGEID("gui.pageid"),
	GUI_LASTPOST("gui.lastpost"),
	GUI_EXTRAREWARDS("gui.extrarewards"),
	GUI_PAGENOTVISIBLE("gui.pagenotvisible"),
	GUI_CLICKOPEN("gui.clickopen"),
	GUI_REWARDSINFO("gui.rewardsinfo"),

	CLICKPOSTICON("clickposticon"),

	INFO("info"),
	EXTRAINFO("extrainfo"),

	HELP_TITLE("help.title"),
	HELP_HELP("help.help"),
	HELP_BINDING("help.binding"),
	HELP_REWARD("help.reward"),
	HELP_TESTREWARD("help.testreward"),
	HELP_LIST("help.list"),
	HELP_TOP("help.top"),
	HELP_CHECK("help.check"),
	HELP_DELETE("help.delete"),
	HELP_RELOAD("help.reload");

	public final String path;

	private static FileConfiguration messageConfig;

	Message(String path) {
		this.path = path;
	}

	public static void load() {// 加载与重载
		messageConfig = YamlConfiguration.loadConfiguration(new File(BBSToper.INSTANCE.getDataFolder(), "lang.yml"));// 加载配置
		InputStream defConfigStream = BBSToper.INSTANCE.getResource("lang.yml");
		if (defConfigStream != null) {
			BBSToper.INSTANCE.getConfig().setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream, Charsets.UTF_8)));
		}
	}

	public String getString() {
		return ChatColor.translateAlternateColorCodes('&', messageConfig.getString(path));
	}

	public List<String> getStringList() {
		return Collections.unmodifiableList(// 禁止修改
				messageConfig.getStringList(path).stream().map(msg -> ChatColor.translateAlternateColorCodes('&', msg))
						.collect(Collectors.toList()));
	}

}
