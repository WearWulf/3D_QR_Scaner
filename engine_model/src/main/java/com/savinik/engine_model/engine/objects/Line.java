package com.savinik.engine_model.engine.objects;

import android.opengl.GLES20;

import com.savinik.engine_model.engine.model.Object3DData;
import com.savinik.engine_model.util.io.IOUtils;

public final class Line {

    public static Object3DData build(float[] line) {
        return new Object3DData(IOUtils.createFloatBuffer(line.length).put(line))
                .setDrawMode(GLES20.GL_LINES).setId("Line");
    }
}
