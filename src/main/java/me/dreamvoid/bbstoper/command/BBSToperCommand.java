package me.dreamvoid.bbstoper.command;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.Poster;
import moe.feo.bbstoper.Reward;
import moe.feo.bbstoper.config.Config;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.database.DatabaseManager;
import moe.feo.bbstoper.gui.GUI;
import moe.feo.bbstoper.listener.IDListener;
import moe.feo.bbstoper.utils.Crawler;
import moe.feo.bbstoper.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class BBSToperCommand implements TabExecutor {
    private final Map<String, String> cache = new HashMap<>();// 这个map是为了暂存玩家的绑定信息的
    private final Map<UUID, Long> queryrecord = new HashMap<>();// 这个map是用于储存玩家上次查询顶贴记录的时间

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                new GUI(player);
                return true;
            } else {
                sender.sendMessage("This server is running " + BBSToper.INSTANCE.getName() + " version " + BBSToper.INSTANCE.getDescription().getVersion() + " by " + BBSToper.INSTANCE.getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
                return false;
            }
        }
        
        switch (args[0].toLowerCase()){
            case "help": {
                sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TITLE.getString());
                if (sender.hasPermission("bbstoper.command.reward"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_REWARD.getString());
                if (sender.hasPermission("bbstoper.command.testreward"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TESTREWARD.getString());
                if (sender.hasPermission("bbstoper.command.binding"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
                if (sender.hasPermission("bbstoper.command.list"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_LIST.getString());
                if (sender.hasPermission("bbstoper.command.top"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TOP.getString());
                if (sender.hasPermission("bbstoper.command.check"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_CHECK.getString());
                if (sender.hasPermission("bbstoper.command.delete"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_DELETE.getString());
                if (sender.hasPermission("bbstoper.command.reload"))
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_RELOAD.getString());
                break;
            }
            case "binding": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.PLAYERCMD.getString());
                    sender.sendMessage(Message.HELP_HELP.getString());
                    break;
                }
                if (!(sender.hasPermission("bbstoper.command.binding"))) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    IDListener.unregister(sender);
                    break;
                }
                if (args.length == 2) {
                    Player player = Bukkit.getPlayer(sender.getName());
                    String uuid = player.getUniqueId().toString();
                    Poster poster = DatabaseManager.database.getPoster(uuid);
                    boolean isrecording = true;
                    if (poster != null) {
                        long cd = System.currentTimeMillis() - poster.getBinddate();// 已经过了的cd
                        long settedcd = Config.MCBBS_CHANGEIDCOOLDOWN.getInt() * (long) 86400000;// 设置的cd
                        if (cd < settedcd) {// 如果还在cd那么直接break;
                            long leftcd = settedcd - cd;// 剩下的cd
                            long leftcdtodays = leftcd / 86400000;
                            sender.sendMessage(Message.PREFIX.getString() + Message.ONCOOLDOWN.getString()
                                    .replaceAll("%COOLDOWN%", String.valueOf(leftcdtodays)));
                            IDListener.unregister(sender);
                            break;
                        }
                    } else {
                        poster = new Poster();
                        isrecording = false;
                    }
                    String ownersuuid = DatabaseManager.database.bbsNameCheck(args[1]);
                    if (ownersuuid == null) {// 没有人绑定过这个论坛id
                        if (cache.get(uuid) != null && cache.get(uuid).equals(args[1])) {
                            poster.setUuid(uuid);
                            poster.setName(sender.getName());
                            poster.setBbsname(args[1]);
                            poster.setBinddate(System.currentTimeMillis());
                            if (isrecording) DatabaseManager.database.updatePoster(poster);
                            else DatabaseManager.database.addPoster(poster);
                            cache.put(uuid, null);// 绑定成功, 清理这个键
                            sender.sendMessage(Message.PREFIX.getString() + Message.BINDINGSUCCESS.getString());
                            IDListener.unregister(sender);
                        } else if (cache.get(uuid) == null) {
                            cache.put(uuid, args[1]);
                            sender.sendMessage(Message.PREFIX.getString() + Message.REPEAT.getString());
                        } else {
                            sender.sendMessage(Message.PREFIX.getString() + Message.NOTSAME.getString());
                            cache.put(uuid, null);
                            IDListener.unregister(sender);
                        }
                        break;
                    } else if (ownersuuid.equals(uuid)) {// 自己绑定了这个论坛id
                        sender.sendMessage(Message.PREFIX.getString() + Message.OWNSAMEBIND.getString());
                        IDListener.unregister(sender);
                        break;
                    } else {
                        sender.sendMessage(Message.PREFIX.getString() + Message.SAMEBIND.getString());
                        IDListener.unregister(sender);
                        break;
                    }
                } else {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
                    IDListener.unregister(sender);
                    break;
                }
            }
            case "reward": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.PLAYERCMD.getString());
                    sender.sendMessage(Message.HELP_HELP.getString());
                    break;
                }
                if (!sender.hasPermission("bbstoper.command.reward")) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                Player player = Bukkit.getPlayer(sender.getName());
                String uuid = player.getUniqueId().toString();
                Poster poster = DatabaseManager.database.getPoster(uuid);
                if (poster == null) {// 没有绑定
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOTBOUND.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_BINDING.getString());
                    break;
                }
                if (!sender.hasPermission("bbstoper.bypass.querycooldown")) {
                    double cooldown = getQueryCoolDown(((Player) sender).getUniqueId());
                    if (cooldown > 0) {
                        sender.sendMessage(Message.PREFIX.getString() + Message.QUERY_COOLDOWN.getString()
                                .replaceAll("%COOLDOWN%", String.valueOf((int) cooldown)));
                        break;
                    } else queryrecord.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
                }
                Crawler crawler = new Crawler();
                if (!crawler.visible) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.PAGENOTVISIBLE.getString());
                    break;
                }
                String bbsname = poster.getBbsname();
                List<String> cache = new ArrayList<>();// 这个缓存是用来判断玩家的顶贴粒度是否小于一分钟
                boolean issucceed = false;
                boolean isovertime = false;
                boolean iswaitamin = false;
                boolean havepost = false;
                // 对ID进行遍历
                for (int i = 0; i < crawler.ID.size(); i++)
                    if (crawler.ID.get(i).equalsIgnoreCase(bbsname)) {// 如果ID等于poster的论坛名字
                        List<String> topstates = poster.getTopStates();
                        // 判断玩家的顶贴粒度是否小于一分钟了
                        // 缓存里面有这次时间
                        // 然后再去遍历数据库里面存的时间
                        for (String cachedtime : cache) {
                            if (cachedtime.equals(crawler.Time.get(i))) {
                                for (String topstate : topstates) {
                                    if (topstate.equals(crawler.Time.get(i))) {// 如果数据库里面的时间也等于这次的时间
                                        // 那就说明玩家肯定有两次同样时间的顶贴，说明玩家顶贴间隔小于一分钟
                                        iswaitamin = true;// 我们这里只会提醒玩家一次
                                        break;
                                    }
                                }
                            }
                        }
                        if (!topstates.contains(crawler.Time.get(i))) {// 如果数据库里没有这次顶贴的记录
                            havepost = true;
                            String datenow = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                            if (!datenow.equals(poster.getRewardbefore())) {// 如果上一次顶贴不是今天，置零
                                poster.setRewardbefore(datenow);
                                poster.setRewardtime(0);
                            }
                            if (poster.getRewardtime() < Config.REWARD_TIMES.getInt()) {// 奖励次数小于设定值
                                new Reward((Player) sender, crawler, i).award();
                                DatabaseManager.database.addTopState(poster.getBbsname(), crawler.Time.get(i));
                                poster.setRewardtime(poster.getRewardtime() + 1);// rewardtime次数加一
                                issucceed = true;
                            } else isovertime = true;
                        }
                    }
                DatabaseManager.database.updatePoster(poster);// 更新poster
                if (issucceed) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.REWARDGIVED.getString());
                    for (Player p :Bukkit.getOnlinePlayers()) {// 给有奖励权限且能看见此玩家(防止Vanish)的玩家广播
                        if (!p.canSee((Player)sender)) continue;
                        if (!p.hasPermission("bbstoper.command.reward")) continue;
                        p.sendMessage(Message.BROADCAST.getString().replaceAll("%PLAYER%", player.getName()));
                    }
                }
                if (isovertime) {
                    int rewardtimes = Config.REWARD_TIMES.getInt();
                    sender.sendMessage(Message.PREFIX.getString() + Message.OVERTIME.getString()
                            .replaceAll("%REWARDTIMES%", Integer.toString(rewardtimes)));
                }
                if (iswaitamin) sender.sendMessage(Message.PREFIX.getString() + Message.WAITAMIN.getString());
                if (!havepost) sender.sendMessage(Message.PREFIX.getString() + Message.NOPOST.getString());

                break;
            }
            case "testreward": {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(Message.PLAYERCMD.getString());
                    sender.sendMessage(Message.HELP_HELP.getString());
                    break;
                }
                if (!sender.hasPermission("bbstoper.command.testreward")) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                String type;
                if (args.length == 1) type = "NORMAL";
                else if (args.length == 2) {
                    type = args[1].toUpperCase();
                    if (!(type.equals("NORMAL") || type.equals("INCENTIVE") || type.equals("OFFDAY"))) {
                        sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                        sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TESTREWARD.getString());
                        break;
                    }
                } else {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TESTREWARD.getString());
                    break;
                }
                Player player = Bukkit.getPlayer(sender.getName());
                new Reward(player, null, 0).testAward(type);
                sender.sendMessage(Message.PREFIX.getString() + Message.REWARDGIVED.getString());
                break;
            }
            case "list": {
                if (!sender.hasPermission("bbstoper.command.list")) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                if (sender instanceof Player && !sender.hasPermission("bbstoper.bypassquerycooldown")) {
                    double cooldown = getQueryCoolDown(((Player) sender).getUniqueId());
                    if (cooldown > 0) {
                        sender.sendMessage(Message.PREFIX.getString() + Message.QUERY_COOLDOWN.getString()
                                .replaceAll("%COOLDOWN%", String.valueOf((int) cooldown)));
                        break;
                    } else queryrecord.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
                }
                int page = 1;
                if (args.length == 2) {
                    // 判断参数是否为数字
                    for (char c : args[1].toCharArray())
                        if (!Character.isDigit(c)) {
                            sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TOP.getString());
                            break;
                        }
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Message.INVALIDNUM.getString());
                        break;
                    }

                } else if (args.length > 2) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_LIST.getString());
                    break;
                }
                Crawler crawler = new Crawler();
                if (!crawler.visible) {
                    if (sender instanceof Player)
                        sender.sendMessage(Message.PREFIX.getString() + Message.PAGENOTVISIBLE.getString());
                    break;
                }
                int totalpage = (int) Math.ceil((double) crawler.ID.size() / Config.MCBBS_PAGESIZE.getInt());
                if (page > totalpage) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.OVERPAGE.getString());
                    break;
                }
                List<String> msglist = new ArrayList<>();
                msglist.add(Message.PREFIX.getString() + Message.POSTERNUM.getString() + ":" + crawler.ID.size());
                for (int i = (page - 1) * Config.MCBBS_PAGESIZE.getInt(); i < page
                        * Config.MCBBS_PAGESIZE.getInt(); i++) {
                    if (i >= crawler.ID.size())
                        break;// 当i不再小于顶贴人数，该停了
                    msglist.add(Message.POSTERID.getString() + ":" + crawler.ID.get(i) + " "
                            + Message.POSTERTIME.getString() + ":" + crawler.Time.get(i));
                }
                if (msglist.size() == 1)
                    msglist.add(Message.NOPOSTER.getString());
                String pageinfo = Message.PAGEINFO.getString();
                pageinfo = pageinfo.replaceAll("%PAGE%", Integer.toString(page));
                pageinfo = pageinfo.replaceAll("%TOTALPAGE%", Integer.toString(totalpage));
                msglist.add(Message.PREFIX.getString() + pageinfo);
                for (String s : msglist) sender.sendMessage(s);
                break;
            }
            case "top": {
                if (!sender.hasPermission("bbstoper.command.top")) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                if (sender instanceof Player && !sender.hasPermission("bbstoper.bypassquerycooldown")) {
                    double cooldown = getQueryCoolDown(((Player) sender).getUniqueId());
                    if (cooldown > 0) {
                        sender.sendMessage(Message.PREFIX.getString() + Message.QUERY_COOLDOWN.getString()
                                .replaceAll("%COOLDOWN%", String.valueOf((int) cooldown)));
                        break;
                    } else queryrecord.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
                }
                int page = 1;
                if (args.length == 2) {
                    // 判断参数是否为数字
                    for (char c : args[1].toCharArray())
                        if (!Character.isDigit(c)) {
                            sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                            sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TOP.getString());
                            break;
                        }
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Message.INVALIDNUM.getString());
                        break;
                    }
                } else if (args.length > 2) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_TOP.getString());
                    break;
                }
                List<Poster> posterlist = DatabaseManager.database.getTopPosters();
                posterlist.addAll(DatabaseManager.database.getNoCountPosters());
                int totalpage = (int) Math.ceil((double) posterlist.size() / Config.MCBBS_PAGESIZE.getInt());
                if (page > totalpage) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.OVERPAGE.getString());
                    break;
                }
                List<String> msglist = new ArrayList<>();
                msglist.add(Message.PREFIX.getString() + Message.POSTERTOTAL.getString() + ":" + posterlist.size());
                for (int i = (page - 1) * Config.MCBBS_PAGESIZE.getInt(); i < page
                        * Config.MCBBS_PAGESIZE.getInt(); i++) {
                    if (i >= posterlist.size())
                        break;// 当i不再小于顶贴人数，该停了
                    Poster poster = posterlist.get(i);
                    msglist.add(Message.POSTERPLAYER.getString() + ":" + poster.getName() + " "
                            + Message.POSTERID.getString() + ":" + poster.getBbsname() + " "
                            + Message.POSTERNUM.getString() + ":" + poster.getCount());
                }
                if (msglist.size() == 1)
                    msglist.add(Message.NOPLAYER.getString());
                String pageinfo = Message.PAGEINFOTOP.getString();
                pageinfo = pageinfo.replaceAll("%PAGE%", Integer.toString(page));
                pageinfo = pageinfo.replaceAll("%TOTALPAGE%", Integer.toString(totalpage));
                msglist.add(Message.PREFIX.getString() + pageinfo);
                for (String s : msglist) sender.sendMessage(s);
                break;
            }
            case "reload": {
                if (!(sender.hasPermission("bbstoper.command.reload"))) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                BBSToper.INSTANCE.saveDefaultConfig();
                Config.load();
                Message.load();
                DatabaseManager.initializeDatabase();
                Util.startAutoReward();
                sender.sendMessage(Message.PREFIX.getString() + Message.RELOAD.getString());
                break;
            }
            case "check": {
                if (!(sender.hasPermission("bbstoper.command.check"))) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                if (args.length != 3) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_CHECK.getString());
                    break;
                }
                switch (args[1].toLowerCase()) {
                    case "bbsid": {
                        String owneruuid = DatabaseManager.database.bbsNameCheck(args[2]);
                        if (owneruuid == null) {
                            sender.sendMessage(Message.PREFIX.getString() + Message.IDNOTFOUND.getString());
                            break;
                        }
                        OfflinePlayer owner = Bukkit.getOfflinePlayer(UUID.fromString(owneruuid));
                        String ownername = owner.getName();
                        sender.sendMessage(Message.PREFIX.getString() + Message.IDOWNER.getString()
                                .replaceAll("%PLAYER%", ownername).replaceAll("%UUID%", owneruuid));
                        break;
                    }
                    case "player": {
                        @SuppressWarnings("deprecation")
                        UUID owneruuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                        Poster poster = DatabaseManager.database.getPoster(owneruuid.toString());
                        if (poster == null) {
                            sender.sendMessage(Message.PREFIX.getString() + Message.OWNERNOTFOUND.getString());
                            break;
                        }
                        String mcbbsname = poster.getBbsname();
                        sender.sendMessage(
                                Message.PREFIX.getString() + Message.OWNERID.getString().replaceAll("%ID%", mcbbsname));
                        break;
                    }
                }
            }
            case "delete": {
                if (!(sender.hasPermission("bbstoper.command.delete"))) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.NOPERMISSION.getString());
                    break;
                }
                if (args.length != 2) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                    sender.sendMessage(Message.PREFIX.getString() + Message.HELP_DELETE.getString());
                    break;
                }
                @SuppressWarnings("deprecation")
                UUID uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
                Poster poster = DatabaseManager.database.getPoster(uuid.toString());
                if (poster == null) {
                    sender.sendMessage(Message.PREFIX.getString() + Message.OWNERNOTFOUND.getString());
                    break;
                }
                DatabaseManager.database.deletePoster(uuid.toString());
                sender.sendMessage(Message.PREFIX.getString() + Message.DELETESUCCESS.getString());
                break;
            }
            default: {
                sender.sendMessage(Message.PREFIX.getString() + Message.INVALID.getString());
                sender.sendMessage(Message.PREFIX.getString() + Message.HELP_HELP.getString());
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if ("help".startsWith(args[0])) list.add("help");
            if ("reward".startsWith(args[0]) && sender.hasPermission("bbstoper.command.reward")) list.add("reward");
            if ("testreward".startsWith(args[0]) && sender.hasPermission("bbstoper.command.testreward")) list.add("testreward");
            if ("binding".startsWith(args[0]) && sender.hasPermission("bbstoper.command.binding")) list.add("binding");
            if ("list".startsWith(args[0]) && sender.hasPermission("bbstoper.command.list")) list.add("list");
            if ("top".startsWith(args[0]) && sender.hasPermission("bbstoper.command.top")) list.add("top");
            if ("check".startsWith(args[0]) && sender.hasPermission("bbstoper.command.check")) list.add("check");
            if ("delete".startsWith(args[0]) && sender.hasPermission("bbstoper.command.delete")) list.add("delete");
            if ("reload".startsWith(args[0]) && sender.hasPermission("bbstoper.command.reload")) list.add("reload");
            return list;
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("check") && sender.hasPermission("bbstoper.command.check")) {
                List<String> list = new ArrayList<>();
                if ("bbsid".startsWith(args[1])) list.add("bbsid");
                if ("player".startsWith(args[1])) list.add("player");
                return list;
            }
        }
        return null;
    }

    public double getQueryCoolDown(UUID uuid) {
        int coolDown = Config.MCBBS_QUERYCOOLDOWN.getInt() * 1000;
        long now = System.currentTimeMillis();
        Long before = queryrecord.get(uuid);
        if (before == null) before = 0L;
        return (coolDown - (now - before)) / (double) 1000;
    }
}
