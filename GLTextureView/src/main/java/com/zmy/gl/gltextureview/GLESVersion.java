package com.zmy.gl.gltextureview;

public enum GLESVersion {
    VERSION1X(1),
    VERSION2X(2),
    VERSION3X(3);
    private int version;

    GLESVersion(int realVersion) {
        version = realVersion;
    }

    public int getVersion() {
        return version;
    }
}
