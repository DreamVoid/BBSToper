package me.dreamvoid.bbstoper;

import moe.feo.bbstoper.config.Config;

public class Utils {
    public static boolean findClass(String className){
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e){
            return false;
        }
    }

    /**
     * 获取 MCBBS 宣传帖链接
     * @return 宣传帖完整链接
     */
    public static String getMCBBSUrl(){
        return getMCBBSUrl(false);
    }

    /**
     * 获取 MCBBS 宣传帖链接
     * @param optimize 是否优化链接为形如 thread-xxx-1-1.html 的样式
     * @return 宣传帖完整链接
     */
    public static String getMCBBSUrl(boolean optimize){
        if(optimize){
            return Config.MCBBS_LINK.getString() + "thread-" + Config.MCBBS_URL.getString() + "-1-1.html";
        } else return Config.MCBBS_LINK.getString() + "forum.php?mod=misc&mobile=no&action=viewthreadmod&tid=" + Config.MCBBS_URL.getString();
    }
}
