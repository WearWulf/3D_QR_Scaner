package com.savinik.engine_model.engine.services;


import com.savinik.engine_model.engine.model.Object3DData;

public interface LoadListener {

    void onStart();

    void onProgress(String progress);

    void onLoadError(Exception ex);

    void onLoad(Object3DData data);

    void onLoadComplete();
}
