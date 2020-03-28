package com.example.lib_audio.mdiaplayer.events;

import com.example.lib_audio.mdiaplayer.model.AudioBean;

public class AudioLoadEvent {
  public AudioBean mAudioBean;

  public AudioLoadEvent(AudioBean audioBean) {
    this.mAudioBean = audioBean;
  }
}
