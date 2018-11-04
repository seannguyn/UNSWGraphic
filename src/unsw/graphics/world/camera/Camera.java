package unsw.graphics.world.camera;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point2D;

import java.awt.*;

/**
 * Represents a camera to placed in the world
 *
 * @author Zahid, z51217150
 * @author Sean, z5055824
 */
public class Camera implements MouseListener {

    private static final float FAR = 200;
    private static final float NEAR = 0.1f;
    private float fov = 60;

    private Robot robot;
    private GLWindow window;
    private boolean hasFocus = false;

    private Point2D mousePos;

    private int centerX;
    private int centerY;

    private float transX = 0;
    private float transY = 0;
    private float transZ = 0;

    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    private static final float scale = 1;

    /**
     * Camera constructor
     *
     * @param window the application window
     */
    public Camera(GLWindow window) {
        this.window = window;
        this.centerX = window.getWidth() / 2;
        this.centerY = window.getHeight() / 2;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        this.mousePos = new Point2D(0, 0);
    }

    public float getRotateX() {
        return rotateX;
    }

    public float getRotateY() {
        return rotateY;
    }

    /**
     * Checks whether the application window has focus.
     * @return true if window clicked, false otherwise
     */
    public boolean isInFocus() {
        return hasFocus;
    }

    /**
     * Move mouse to the center of the window.
     */
    public void resetMousePosition() {
        robot.mouseMove(window.getX() + centerX, window.getY() + centerY);
    }

    /**
     * Gets the mouse position relative to the center of the application
     * window.
     * @return mouse position
     */
    public Point2D getMousePos() {
        return mousePos;
    }

    /**
     * Sets the camera's field of view.
     *
     * @param angle
     */
    public void setFOV(float angle) {
        fov = angle;
    }

    /**
     * Sets the camera transform position
     *
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(float x, float y, float z) {
        transX = x;
        transY = y;
        transZ = z;
    }

    /**
     * Sets the camera transform rotations
     *
     * @param x
     * @param y
     * @param z
     */
    public void setRotations(float x, float y, float z) {
        rotateX = x;
        rotateY = y;
        rotateZ = z;
    }

    /**
     * Sets the projection matrix of the application.
     *
     * @param gl
     * @param width  width of the window
     * @param height height of the window
     */
    public void reshape(GL3 gl, int width, int height) {
        Shader.setProjMatrix(gl, Matrix4.perspective(fov, width / (float) height, NEAR, FAR));
    }

    /**
     * Sets the view matrix of the application.
     *
     * @param gl
     */
    public void setViewMatrix(GL3 gl) {
        CoordFrame3D viewFrame = CoordFrame3D.identity()
                .scale(1/scale, 1/scale, 1/scale)
                .rotate(-rotateX, -rotateY, -rotateZ)
                .translate(-transX, -transY, -transZ);
        Shader.setViewMatrix(gl, viewFrame.getMatrix());
    }

    /**
     * Sets the view matrix of the application.
     *
     * @param gl
     */
    public void setProjMatrix(GL3 gl) {
        reshape(gl, window.getWidth(), window.getHeight());
    }

    /**
     * Sets the view matrix for the skybox shader, discarding the camera's
     * rotation transformations, along with the projection matrix.
     *
     * @param gl
     */
    public void setSkyboxMatrices(GL3 gl) {
        CoordFrame3D viewFrame = CoordFrame3D.identity()
                .scale(1/scale, 1/scale, 1/scale)
                .rotate(-rotateX, -rotateY, -rotateZ);
        Shader.setViewMatrix(gl, viewFrame.getMatrix());
        reshape(gl, window.getWidth(), window.getHeight());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        hasFocus = !hasFocus;
        window.setPointerVisible(!hasFocus);
        if (hasFocus) resetMousePosition();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        float dx = e.getX() - centerX;
        float dy = e.getY() - centerY;
        mousePos = new Point2D(dx, dy);
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) { }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
    }
}
