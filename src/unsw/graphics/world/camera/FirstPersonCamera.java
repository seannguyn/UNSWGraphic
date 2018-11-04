package unsw.graphics.world.camera;

import com.jogamp.newt.event.*;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.world.avatar.Avatar;

/**
 * A 3D camera that can be moved around in the world.
 *
 * @author Zahid z5121570
 */
public class FirstPersonCamera extends Camera {

    // Negative for direction to follow mouse, positive to invert
    private static final float ROTATION_SCALE = -0.2f;
    private static final float ZOOM_SCALE = 4f;

    private Avatar avatar;

    private float zoom = 0;

    public FirstPersonCamera(GLWindow window, Avatar avatar) {
        super(window);
        this.avatar = avatar;
    }

    private void updatePosition() {
        float z = avatar.getPosZ();
        float y = avatar.getPosY();
        float x = avatar.getPosX();
        setPosition(x, y, z);

        float yaw = avatar.getRotateY();
        setRotations(getRotateX(), yaw, 0);
    }

    @Override
    public void setViewMatrix(GL3 gl) {
        updatePosition();
        super.setViewMatrix(gl);
    }

    @Override
    public void reshape(GL3 gl, int width, int height) {
        setFOV(60 - zoom);
        super.reshape(gl, width, height);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!isInFocus()) return;
        super.mouseMoved(e);

        Point2D pos = getMousePos();
        float pitch = getRotateX() + (pos.getY() * ROTATION_SCALE);
        float yaw   = getRotateY() + (pos.getX() * ROTATION_SCALE);

        setRotations(pitch, yaw, 0);
        avatar.setRotations(pitch, yaw, 0);
        resetMousePosition();
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        float scroll = e.getRotation()[1];
        if (scroll > 0) zoom += ZOOM_SCALE;
        if (scroll < 0) zoom -= ZOOM_SCALE;
    }
}
