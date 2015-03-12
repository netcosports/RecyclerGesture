package com.netcosports.recyclergesture.library;

/**
 * Encapsulate common behaviour of each gesture.
 */
public abstract class RecyclerGesture {

    private boolean enable;

    /**
     * Encapsulate common behaviour of each gesture.
     */
    public RecyclerGesture() {
        enable = true;
    }

    /**
     * Enable/disable gesture.
     * <p/>
     * Enabled by default.
     *
     * @param enable true to enable the gesture.
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    /**
     * Used to know if the gesture is enable or not.
     *
     * @return true if the gesture if enable.
     */
    public boolean isEnabled() {
        return this.enable;
    }
}
