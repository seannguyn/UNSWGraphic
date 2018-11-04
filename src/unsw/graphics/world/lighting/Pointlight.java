package unsw.graphics.world.lighting;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;

public class Pointlight {

    private static final float MOVEMENT_SCALE = 0.04f;
    private static final float ROTATION_SCALE = 2; // in degrees

    private float altitude = 0;

    private float transX = 0;
    private float transY = 0;
    private float transZ = 0;

    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    /**
     * Constructor for a spotlight
     */
    public Pointlight() {
    }

    /**
     * Gets the avatar's local frame from give global frame.
     * @param frame
     * @return
     */
    public CoordFrame3D getLocalFrame(CoordFrame3D frame) {
        return frame.translate(transX, transY, transZ)
                .rotate(rotateX, rotateY, rotateZ);
    }

    public float getPosX() {
        return transX;
    }

    public float getPosY() {
        return transY;
    }

    public float getPosZ() {
        return transZ;
    }

    /**
     * Sets the avatar rotation along the Y-axis
     * @param angle degrees
     */
    public void setRotateY(float angle) {
        rotateY = angle;
    }

    /**
     * Gets the avatar rotation along the Y-axis
     * @return angle in degrees
     */
    public float getRotateY() {
        return rotateY;
    }

    public void init(GL3 gl) {

    }

    public void draw(GL3 gl, CoordFrame3D frame) {

    }

    public void destroy(GL3 gl) {
    }
}
