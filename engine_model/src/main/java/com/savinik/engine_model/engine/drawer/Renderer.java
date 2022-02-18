package com.savinik.engine_model.engine.drawer;

import com.savinik.engine_model.engine.model.Object3DData;



public interface Renderer {

	void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int textureId, float[] lightPosInWorldSpace, float[] cameraPos);

	void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int textureId, float[] lightPosInWorldSpace, float[] colorMask, float[] cameraPos);

	void draw(Object3DData obj, float[] pMatrix, float[] vMatrix, int drawType, int drawSize, int textureId, float[]
			lightPosInWorldSpace, float[] colorMask, float[] cameraPos);
}