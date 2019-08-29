package com.easefun.polyv.cloudclassdemo.watch.playback_cache.playback_list;

import com.easefun.polyv.cloudclass.model.playback.PolyvPlaybackListVO;
import com.easefun.polyv.commonui.modle.db.PolyvPlaybackCacheDBEntity;

import java.util.List;

import io.reactivex.functions.Consumer;


/**
 * date: 2019/8/14 0014
 *
 * @author hwj
 * description 回放列表数据仓库
 */
public interface IPolyvPlaybackListRepo {
    void getPlaybackList(String channelId, int pageNum, int pageSize, Consumer<PolyvPlaybackListVO> success, Consumer<Throwable> error);

    void isVideoLiveIdExitInDB(String videoPoolId, Consumer<List<PolyvPlaybackCacheDBEntity>> isExit);

    void destroy();
}
