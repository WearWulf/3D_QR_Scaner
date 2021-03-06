package com.savinik.engine_model.util.android;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class AndroidURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("android".equals(protocol)) {
            return new com.savinik.engine_model.util.android.assets.Handler();
        } else if ("content".equals(protocol)){
            return new com.savinik.engine_model.util.android.content.Handler();
        }
        return null;
    }
}
