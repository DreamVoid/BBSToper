package moe.feo.bbstoper.utils;

import moe.feo.bbstoper.BBSToper;
import moe.feo.bbstoper.Reward;
import moe.feo.bbstoper.config.Message;
import moe.feo.bbstoper.config.Config;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class Util {
	private static BukkitTask autoRewardTask;
	private static final ArrayList<Integer> runningTaskIdList = new ArrayList<>();

	public static void startAutoReward() {// 自动奖励的方法
		if (autoRewardTask != null) {// 任务对象不为空
			boolean taskCancelled;// 是否已经取消
			try {
				taskCancelled = autoRewardTask.isCancelled();
			} catch (NoSuchMethodError e) {// 1.7.10还没有这个方法
				taskCancelled = false;// 默认就当这个任务没有取消
			}
			if (!taskCancelled) {// 如果任务还被取消
				autoRewardTask.cancel();// 将之前的任务取消
			}
		}
		int period = Config.REWARD_AUTO.getInt() * 20;
		if (period > 0) {
			autoRewardTask = new BukkitRunnable() {// 自动奖励，异步执行
				@Override
				public void run() {
					addRunningTaskID(this.getTaskId());
					task();
					removeRunningTaskID(this.getTaskId());
				}

				public void task() {
					Crawler crawler = new Crawler();
					if (!crawler.visible)
						return;
					crawler.activeReward();
				}
			}.runTaskTimerAsynchronously(BBSToper.INSTANCE, 0, period);
		}
	}

	public static void waitForAllTask() {// 此方法会阻塞直到所有此插件创建的线程结束
		int count = 0;
		try {
			while (!runningTaskIdList.isEmpty()) {// 当list非空，阻塞线程100毫秒后再判断一次
				if (count > 30000) {// 超过30秒没有关闭就算超时
					throw new TimeoutException();
				}
				Thread.sleep(100);
				count = count + 100;
			}
		} catch (InterruptedException | TimeoutException e) {
			e.printStackTrace();
		}
	}

	public static void addRunningTaskID(int i) {
		if (!runningTaskIdList.contains(i))
			runningTaskIdList.add(i);
	}

	public static void removeRunningTaskID(int i) {
		if (runningTaskIdList.contains(i))
			runningTaskIdList.remove((Integer) i);
	}
	
	public static String getExtraReward(Crawler crawler) {// 获取会获得的额外奖励(可为空)
		boolean incentive = false;// 是否符合激励奖励条件
		boolean offday = false;// 是否符合休息日奖励条件
		Calendar current = Calendar.getInstance();// 当前时间
		Calendar lastpost = Calendar.getInstance();// 上一次顶贴的时间
		if (crawler.Time.size() > 0) {// 如果有顶贴记录的话
			SimpleDateFormat bbsformat = new SimpleDateFormat("yyyy-M-d HH:mm");// mcbbs的日期格式
			try {
				Date lastpostdate = bbsformat.parse(crawler.Time.get(0));
				lastpost.setTime(lastpostdate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		if (Reward.canIncentiveReward(current, lastpost)) {
			incentive = true;
		}
		if (Reward.canOffDayReward(current)) {
			offday = true;
		}
		String extra = null;
		if (incentive) {
			// 如果休息日奖励也达成了, 并且激励奖励和休息日奖励都不是额外奖励, 不会发放激励奖励(只会发放休息日奖励)
			if (!(offday && !Config.REWARD_INCENTIVEREWARD_EXTRA.getBoolean()
					&& !Config.REWARD_OFFDAYREWARD_EXTRA.getBoolean())) {
				extra = Message.GUI_INCENTIVEREWARDS.getString();
			}
		}
		if (offday) {
			if (extra == null) {
				extra = Message.GUI_OFFDAYREWARDS.getString();
			} else {
				extra = extra + "+" + Message.GUI_OFFDAYREWARDS.getString();
			}
		}
		return extra;
	}

}
