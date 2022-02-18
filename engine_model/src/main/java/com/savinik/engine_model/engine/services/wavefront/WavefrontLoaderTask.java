package com.savinik.engine_model.engine.services.wavefront;

import android.app.Activity;
import android.opengl.GLES20;

import com.savinik.engine_model.engine.model.Object3DData;
import com.savinik.engine_model.engine.services.LoadListener;
import com.savinik.engine_model.engine.services.LoaderTask;

import java.net.URI;
import java.util.List;

/**
 * Wavefront loader implementation
 *
 * @author andresoviedo
 */

public class WavefrontLoaderTask extends LoaderTask {

    public WavefrontLoaderTask(final Activity parent, final URI uri, final LoadListener callback) {
        super(parent, uri, callback);
    }

    @Override
    protected List<Object3DData> build() {

        final WavefrontLoader wfl = new WavefrontLoader(GLES20.GL_TRIANGLE_FAN, this);

        super.publishProgress("Loading model...");

        final List<Object3DData> load = wfl.load(uri);

        return load;
    }

    @Override
    public void onProgress(String progress) {
        super.publishProgress(progress);
    }
}
