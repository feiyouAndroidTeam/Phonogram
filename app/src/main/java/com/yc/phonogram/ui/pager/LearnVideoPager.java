package com.yc.phonogram.ui.pager;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.danikula.videocache.HttpProxyCacheServer;
import com.jakewharton.rxbinding.view.RxView;
import com.kk.utils.ToastUtil;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.xinqu.videoplayer.XinQuVideoPlayer;
import com.xinqu.videoplayer.XinQuVideoPlayerStandard;
import com.yc.phonogram.App;
import com.yc.phonogram.R;
import com.yc.phonogram.adapter.LPContentListAdapter;
import com.yc.phonogram.base.BasePager;
import com.yc.phonogram.domain.ExampleInfo;
import com.yc.phonogram.domain.PhonogramInfo;
import com.yc.phonogram.listener.PerfectClickListener;

import java.io.IOException;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/19.
 * 首页学音标
 */

public class LearnVideoPager  extends BasePager{

    private static final String TAG = LearnVideoPager.class.getSimpleName();
    private final PhonogramInfo mData;
    private LPContentListAdapter mLpContentListAdapter;
    private ImageView mIvLpLogo;
    private TextView mTvLpTipsContent;
    private XinQuVideoPlayerStandard mVidepPlayer;
    private KSYMediaPlayer mKsyMediaPlayer;
    private Animation mInputAnimation;
    private View mAttachView=null;//当前正在播放的ItemView
    private AnimationDrawable mAnimationDrawable;

    public LearnVideoPager(Activity context, PhonogramInfo phonogramInfo) {
        super(context);
        this.mData=phonogramInfo;
        setContentView(R.layout.pager_learn_child_content);
    }

    @Override
    protected void initViews() {
        if(null==mContext) return;
        //封面
        mIvLpLogo = (ImageView) getView(R.id.iv_lp_logo);
        mTvLpTipsContent = (TextView) getView(R.id.tv_lp_tips_content);
        mTvLpTipsContent.setMovementMethod(ScrollingMovementMethod.getInstance());

        //点击了音标的说LOGO
        ((ImageView) getView(R.id.iv_lp_tips_logo)).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onClickView(View v) {

                stopMusic();
                startMusic("http://sc.wk2.com/upload/music/9/2017-11-17/5a0e4b51c34e7.mp3");
            }
        });
        mVidepPlayer = (XinQuVideoPlayerStandard) getView(R.id.video_player);
        mVidepPlayer.widthRatio=16;
        mVidepPlayer.heightRatio=9;
        mLpContentListAdapter = new LPContentListAdapter(mContext,null);
        GridView gridView = (GridView) getView(R.id.grid_view);
        gridView.setAdapter(mLpContentListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(null!=mLpContentListAdapter){
                    List<ExampleInfo> exampleInfos=mLpContentListAdapter.getData();
                    if(null!=exampleInfos&&exampleInfos.size()>0){
                        ExampleInfo exampleInfo = exampleInfos.get(position);
                        if(null!=exampleInfo){
                            //http://sc.wk2.com/upload/music/9/2017-11-17/5a0e4b51c34e7.mp3
                            startMusic(exampleInfo.getVideo(),true,view);
                        }
                    }
                }
            }
        });
    }



    @Override
    protected void loadData() {
        if(null==mData) return;
        mVidepPlayer.setUp(mData.getVideo(), XinQuVideoPlayer.SCREEN_WINDOW_LIST,false,mData.getName());
        RequestOptions options = new RequestOptions();
        options.placeholder(R.mipmap.ic_player_error);
        options.error(R.mipmap.ic_player_error);
        options.diskCacheStrategy(DiskCacheStrategy.ALL);//缓存源资源和转换后的资源
        options.skipMemoryCache(true);//跳过内存缓存

        Glide.with(mContext).load(mData.getCover()).apply(options).thumbnail(0.1f).into(mVidepPlayer.thumbImageView);
        String proxyUrl =mData.getVideo();
        HttpProxyCacheServer proxy = App.getProxy();
        if(null!=proxy){
            proxyUrl= proxy.getProxyUrl(mData.getVideo());
        }
        mVidepPlayer.setUp(proxyUrl, XinQuVideoPlayer.SCREEN_WINDOW_LIST,false,null==mData.getName()?"":mData.getName());
        Glide.with(mContext).load(mData.getImg()).apply(options).thumbnail(0.1f).into(mIvLpLogo);//音标
        Glide.with(mContext).load(mData.getCover()).apply(options).thumbnail(0.1f).into(mVidepPlayer.thumbImageView);//视频播放器封面
        mTvLpTipsContent.setText(mData.getDesp());
        if(null!=mLpContentListAdapter&&null!=mData.getExampleInfos()){
            mLpContentListAdapter.setNewData(mData.getExampleInfos(),mData.getName());
        }
    }


    private void startMusic(String musicUrl){
        startMusic(musicUrl,false,null);
    }

    /**
     * 开始播放音乐
     * @param musicUrl
     * @param attachView
     * @param isListPlay 是否是列表播放
     */
    private void startMusic(String musicUrl, final boolean isListPlay, View attachView){
        stopMusic();
        if(null!=attachView){
            this.mAttachView=attachView;
        }
        if(TextUtils.isEmpty(musicUrl)) return;
        if(null==mKsyMediaPlayer){
            mKsyMediaPlayer = new KSYMediaPlayer.Builder(getActivity()).build();
            mKsyMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        mKsyMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer mp) {
                if(isListPlay){
                    cleanLoadingAnimation();//清除和隐藏加载进度条
                }
                //开始播放动画，如果当前有动画正在播放
                if(isListPlay&&null!=mAttachView){
                    if(null!=mInputAnimation){
                        mInputAnimation.reset();
                        mInputAnimation.cancel();
                        mInputAnimation=null;
                    }
                    mInputAnimation =getAnimation();
                    mAttachView.startAnimation(mInputAnimation);
                }
                //开始播放
                mp.start();
            }
        });
        mKsyMediaPlayer.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                cleanLoadingAnimation();//清除和隐藏加载进度条
                ToastUtil.toast2(getActivity(),"单词播放失败！");
                return true;
            }
        });
        try {
            String proxyUrl =musicUrl;
            HttpProxyCacheServer proxy = App.getProxy();
            if(null!=proxy){
                proxyUrl= proxy.getProxyUrl(musicUrl);
            }
            mKsyMediaPlayer.setDataSource(proxyUrl);
            mKsyMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(null!=attachView&&null!=mAttachView&&isListPlay){
            ImageView progressLoad = mAttachView.findViewById(R.id.progress_load);
            if(null!=progressLoad){
                progressLoad.setVisibility(View.VISIBLE);
                mAnimationDrawable = (AnimationDrawable) progressLoad.getDrawable();
                if(null!=mAnimationDrawable){
                    mAnimationDrawable.start();
                }
            }
        }
    }

    public void stopMusic(){
        //停止ItemView缩放动画播放
        if(null!=mInputAnimation){
            mInputAnimation.reset();
            mInputAnimation.cancel();
            mInputAnimation=null;
        }
        //停止音乐播放
        if(null!=mKsyMediaPlayer){
            if(mKsyMediaPlayer.isPlaying()){
                mKsyMediaPlayer.stop();
            }
            mKsyMediaPlayer.release();
            mKsyMediaPlayer.reset();
            mKsyMediaPlayer=null;
        }
        cleanLoadingAnimation();
    }

    /**
     * 清除缓冲进度
     */
    private void cleanLoadingAnimation() {
        if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }
        mAnimationDrawable=null;
        //刚才正在播放音标的ItemView
        if(null!=mAttachView){
            ImageView progress_load=mAttachView.findViewById(R.id.progress_load);
            if(null!=progress_load){
                progress_load.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 点赞的动画小
     * @return
     */
    public  ScaleAnimation getAnimation(){
        ScaleAnimation followScaleAnimation = new ScaleAnimation(1.0f, 1.6f, 1.0f, 1.6f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        followScaleAnimation.setRepeatCount(0);
        followScaleAnimation.setDuration(1000);
        return followScaleAnimation;
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG,"onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG,"onPause");
        stopMusic();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG,"onDestroyView");
    }
}
