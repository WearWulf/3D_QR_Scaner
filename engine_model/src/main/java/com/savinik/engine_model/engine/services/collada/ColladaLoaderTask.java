package com.savinik.engine_model.engine.services.collada;

import android.app.Activity;

import com.savinik.engine_model.engine.model.Object3DData;
import com.savinik.engine_model.engine.services.LoadListener;
import com.savinik.engine_model.engine.services.LoaderTask;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class ColladaLoaderTask extends LoaderTask {

    public ColladaLoaderTask(Activity parent, URI uri, LoadListener callback) {
        super(parent, uri, callback);
    }

    @Override
    protected List<Object3DData> build() throws IOException {
        return new ColladaLoader().load(uri, this);
    }
}