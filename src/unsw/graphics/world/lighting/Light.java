package unsw.graphics.world.lighting;

import com.jogamp.opengl.GL3;

public abstract class Light {

    private boolean isLightOn = true;

    /**
     * Turn this light on/off
     */
    public void toggle() {
        isLightOn = !isLightOn;
    }

    /**
     * Check on/off
     * @return
     */
    public boolean isLightOn() {
        return isLightOn;
    }

    /**
     * Sets the uniform lighting variables in the shader.
     * Also use to update the lighting object.
     * @param gl
     */
    public abstract void setUniforms(GL3 gl);
}
