package com.example.lib_audio.mdiaplayer.core;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * @author AlbertZyc
 * @CreatTime 2020/3/28
 * @Description
 **/
public class CustomMediaPlayer extends MediaPlayer implements MediaPlayer.OnCompletionListener {
    public enum Status {
        IDEL, INITIALIZED, STARTED, PAUSED, STOPPED, COMPLETED;
    }
    private OnCompletionListener mOnCompletionListener;

    private Status mState = Status.IDEL;

    public CustomMediaPlayer() {
        super();
        mState = Status.IDEL;
        super.setOnCompletionListener(this);
    }

    @Override
    public void reset() {
        super.reset();
        mState = Status.IDEL;
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, IllegalStateException, SecurityException {
        super.setDataSource(path);
        mState = Status.INITIALIZED;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        mState = Status.STARTED;
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        mState = Status.PAUSED;
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        mState = Status.STOPPED;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mState = Status.COMPLETED;
    }

    public Status getState() {
        return mState;
    }

    public boolean isComplete() {
        return mState == Status.COMPLETED;
    }



    public void setCompleteListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }
}
