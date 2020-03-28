package com.example.lib_audio.mdiaplayer.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;

import com.example.lib_audio.app.AudioHelper;
import com.example.lib_audio.mdiaplayer.events.AudioCompleteEvent;
import com.example.lib_audio.mdiaplayer.events.AudioErrorEvent;
import com.example.lib_audio.mdiaplayer.events.AudioLoadEvent;
import com.example.lib_audio.mdiaplayer.events.AudioPauseEvent;
import com.example.lib_audio.mdiaplayer.events.AudioReleaseEvent;
import com.example.lib_audio.mdiaplayer.events.AudioStartEvent;
import com.example.lib_audio.mdiaplayer.model.AudioBean;

import org.greenrobot.eventbus.EventBus;

/**
 * @author AlbertZyc
 * @CreatTime 2020/3/28
 * @Description 播放音频，对外发送各种类型事件
 **/
public class AudioPlayer implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        AudioFocusManager.AudioFocusListener {

    private static final String TAG = "AudioPlayer";
    private static final int TIME_MSG = 0x01;
    private static final int TIME_INVAL = 100;
    //真正负责播放的核心MediaPlayer子类
    private CustomMediaPlayer mMediaPlayer;
    private WifiManager.WifiLock mWifiLock;
    //音频焦点监听器
    private AudioFocusManager mAudioFocusManager;
    private boolean isPausedByFocusLossTransient;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_MSG:
                    break;
            }
        }
    };

    public AudioPlayer() {
        init();
    }

    private void init() {
        mMediaPlayer = new CustomMediaPlayer();
        mMediaPlayer.setWakeMode(null, PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnErrorListener(this);
        //初始化wifilock
        mWifiLock = ((WifiManager) AudioHelper.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
        mAudioFocusManager = new AudioFocusManager(AudioHelper.getContext(), this);
    }

    /**
     * 对外提供的加载方法
     */
    public void load(AudioBean audioBean) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(audioBean.mUrl);
            mMediaPlayer.prepareAsync();
            //对外发送load事件
            EventBus.getDefault().post(new AudioLoadEvent(audioBean));
        } catch (Exception e) {
            //对外发送error事件
            EventBus.getDefault().post(new AudioErrorEvent());
        }
    }

    /**
     * 对外提供暂停
     */
    public void pause() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED) {
            mMediaPlayer.pause();
            //释放音频焦点wifilock
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
            //释放音频焦点
            if (mAudioFocusManager != null) {
                mAudioFocusManager.abandonAudioFocus();
            }
            //发送暂停事件
            EventBus.getDefault().post(new AudioPauseEvent());
        }
    }

    /**
     * 对外提供恢复
     */
    public void resume() {
        if (getStatus() == CustomMediaPlayer.Status.PAUSED) {
            start();
        }
    }

    /**
     * 清空播放器占用的资源
     */
    public void release() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.release();
        mMediaPlayer = null;
        //释放音频焦点wifilock
        if (mWifiLock.isHeld()) {
            mWifiLock.release();
        }
        //释放音频焦点
        if (mAudioFocusManager != null) {
            mAudioFocusManager.abandonAudioFocus();
        }
        mWifiLock = null;
        mAudioFocusManager = null;
        //发送release销毁事件
        EventBus.getDefault().post(new AudioReleaseEvent());
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        //缓存进度回调
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //播放完毕回调
        EventBus.getDefault().post(new AudioCompleteEvent());
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        //播放出错回调
        EventBus.getDefault().post(new AudioErrorEvent());
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //准备完毕
        start();
    }

    @Override
    public void audioFocusGrant() {
        //再次获得音频焦点
        setVolume(1.0f, 1.0f);
        if (isPausedByFocusLossTransient) {
            resume();
        }
        isPausedByFocusLossTransient = false;
    }

    @Override
    public void audioFocusLoss() {
        //永久失去焦点
        pause();
    }

    @Override
    public void audioFocusLossTransient() {
        //短暂性失去焦点
        pause();
        isPausedByFocusLossTransient = true;
    }

    @Override
    public void audioFocusLossDuck() {
        //瞬间失去焦点：比如短信声音
        setVolume(0.5f, 0.5f);
    }

    /**
     * 获取当前音乐总时长,更新进度用
     */
    public int getDuration() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED
                || getStatus() == CustomMediaPlayer.Status.PAUSED) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (getStatus() == CustomMediaPlayer.Status.STARTED
                || getStatus() == CustomMediaPlayer.Status.PAUSED) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //设置音量
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) mMediaPlayer.setVolume(leftVolume, rightVolume);
    }
    
    //内部开始播放
    private void start() {
        if (!mAudioFocusManager.requestAudioFocus()) {
            Log.e(TAG, "获取音频焦点失败了！");
        }
        mMediaPlayer.start();
        // 启用wifi锁
        mWifiLock.acquire();
        //对外发送start事件
        //更新进度
        mHandler.sendEmptyMessage(TIME_MSG);
        EventBus.getDefault().post(new AudioStartEvent());
    }

    private CustomMediaPlayer.Status getStatus() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getState();
        }
        return CustomMediaPlayer.Status.STOPPED;
    }

}
