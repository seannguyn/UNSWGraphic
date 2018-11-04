package unsw.graphics.world.camera;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.world.Utility;
import unsw.graphics.world.avatar.Avatar;

/**
 * A third person camera that can be attached to an avatar.
 *
 * @author Zahid z5121570
 */
public class ThirdPersonCamera extends Camera {

    // negative zoom so scroll up moves camera closer and vice versa
    private static final float ZOOM_SCALE = -0.05f;

    // negative so that the camera follows the mouse, not inverted
    private static final float ROTATION_SCALE = -0.2f;

    private Avatar avatar;

    private float mouseDx = 0;
    private float mouseDy = 0;

    // Maximum angle the avatar can face up or down
    private static final float MAX_LOOK_DOWN = -30;
    private static final float MAX_LOOK_UP   = 5;

    private float distance = 3;     // Distance from avatar
    private float pitchAngle = 35;  // Angle from horizontal, avatar up to the camera
    private float pivotAngle = 0;   // Angle from behind the avatar, positive anticlockwise

    /**
     * Constructor for a camera that follows an avatar in the world
     *
     * @param window application window
     * @param avatar the avatar this camera is attached to
     */
    public ThirdPersonCamera(GLWindow window, Avatar avatar) {
        super(window);
        this.avatar = avatar;
    }

    private void updatePosition() {
        // Calculate the changes to the avatar's rotations and reset mouse
        float avatarAngleY = avatar.getRotateY() + mouseDx;
        float avatarAngleX = avatar.getRotateX() + mouseDy;
        mouseDx = mouseDy = 0;

        avatarAngleX = Utility.clamp(avatarAngleX, MAX_LOOK_DOWN, MAX_LOOK_UP);

        // Update the avatar's rotations, do it once here and not in mouseMoved
        // to avoid async updates to avatar
        avatar.setRotations(avatarAngleX, avatarAngleY, avatar.getRotateZ());

        // Calculate the total rotation of camera about the avatar's x and y-axis
        float totalAngleY = avatarAngleY + pivotAngle;

        // minus since rotating forwards, z to y axis, is the negative direction
        // and increase the total pitch
        float totalAngleX = pitchAngle - avatarAngleX;

        // Calculate the camera's horizontal and vertical offset from the avatar
        float vertical   = distance * (float) Math.sin(Math.toRadians(totalAngleX));
        float horizontal = distance * (float) Math.cos(Math.toRadians(totalAngleX));

        // Calculate the camera's position based on its offset from the avatar
        float x = avatar.getPosX() + (float) Math.sin(Math.toRadians(totalAngleY)) * horizontal;
        float y = avatar.getPosY() + vertical;
        float z = avatar.getPosZ() + (float) Math.cos(Math.toRadians(totalAngleY)) * horizontal;
        setPosition(x, y, z);

        // total pitch is negative since we want the camera to rotate down to the avatar
        // total angle about the Y-axis is set so the camera always faces the avatar
        setRotations(-totalAngleX, totalAngleY , 0);
    }

    @Override
    public void setViewMatrix(GL3 gl) {
        updatePosition();
        super.setViewMatrix(gl);
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        float scroll = e.getRotation()[1];
        if (scroll > 0) distance += ZOOM_SCALE;
        if (scroll < 0) distance -= ZOOM_SCALE;
        distance = Utility.clamp(distance, 1.5f, 5);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!isInFocus()) return;
        super.mouseMoved(e);
        Point2D pos = getMousePos();

        // Move the camera around the avatar
        pitchAngle += pos.getY() * ROTATION_SCALE;
        pivotAngle += pos.getX() * ROTATION_SCALE;

        resetMousePosition();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!isInFocus()) return;
        super.mouseMoved(e);
        Point2D pos = getMousePos();

        // Sum up all the mouse's horizontal and vertical rotations
        mouseDx += pos.getX() * ROTATION_SCALE;
        mouseDy += pos.getY() * ROTATION_SCALE;

        resetMousePosition();
    }

}
