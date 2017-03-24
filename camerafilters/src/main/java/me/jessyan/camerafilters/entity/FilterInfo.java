package me.jessyan.camerafilters.entity;

/**
 * Created by jess on 8/17/16 17:46
 * Contact with jess.yan.effort@gmail.com
 */
public class FilterInfo {
    public boolean isExt;//是否使用扩展的滤镜(自己定义的滤镜)
    public int index;//内置滤镜的角标为0-13,扩展滤镜是自己在onExtFilterListener中定义的
    public FilterInfo(boolean isExt, int index) {
        this.isExt = isExt;
        this.index = index;
    }
}
