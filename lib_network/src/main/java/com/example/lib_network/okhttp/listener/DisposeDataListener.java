package com.example.lib_network.okhttp.listener;

public interface DisposeDataListener {
    /**
     * 请求成功回调事件处理
     */
    void onSuccess(Object responseObject);
    /**
     * 请求失败回调事件处理
     */
    void onFailure(Object responseObject);
}
