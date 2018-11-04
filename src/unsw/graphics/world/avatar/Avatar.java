package unsw.graphics.world.avatar;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.world.Terrain;
import unsw.graphics.world.Utility;

import java.util.Arrays;

/**
 * An avatar in the world.
 *
 * @author Zahid, z5121750
 * @author Sean, z5055824
 */
public class Avatar implements KeyListener {

    private static final float MOVEMENT_SCALE = 0.06f;
    private static final float ROTATION_SCALE = 2; // in degrees
    private static final float ALTITUDE_OFFSET = 0.5f;

    private Terrain terrain;

    private boolean flying = false;
    private float altitude = 0;

    private float transX = 0;
    private float transY = 0;
    private float transZ = 0;

    private float rotateX = 0;
    private float rotateY = 0;
    private float rotateZ = 0;

    private float scale = 1;

    // 0-3: W, A, S, D
    // 4-5: SHIFT, SPACE
    // 6-9: UP, DOWN, LEFT, RIGHT
    private boolean[] keyMap;

    /**
     * Constructor for an avatar in the world.
     * @param terrain
     */
    public Avatar(Terrain terrain) {
        this.terrain = terrain;
        this.keyMap = new boolean[10];

        // start in the middle of the world
        this.transX = terrain.getWidth()/2;
        this.transZ = terrain.getDepth()/2;
        this.transY = 2;
    }

    /**
     * Constructor for an avatar in the world.
     * @param terrain
     * @param flying
     */
    public Avatar(Terrain terrain, boolean flying) {
        this(terrain);
        this.flying = flying;
    }

    /**
     * Gets the avatar's local frame from give global frame.
     * @param frame
     * @return
     */
    public CoordFrame3D getLocalFrame(CoordFrame3D frame) {
        return frame.translate(transX, transY, transZ)
                .rotateY(rotateY)
                .rotateZ(rotateZ)
                .rotateX(rotateX)
                .scale(scale, scale, scale);
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

    public Point3D getPosition() {
        return new Point3D(transX, transY, transZ);
    }

    /**
     * Sets this avatar's scale
     * @param scale
     */
    public void setScale(float scale) {
        this.scale = scale == 0 ? 1 : scale;
    }

    /**
     * Gets this avatar's scale
     * @return
     */
    public float getScale() {
        return scale;
    }

    /**
     * Sets the avatar's transform rotations
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
     * Sets the avatar rotation along the Y-axis
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

    /**
     * Gets the avatar rotation along the X-axis
     * @return angle in degrees
     */
    public float getRotateX() {
        return rotateX;
    }

    /**
     * Gets the avatar rotation along the Z-axis
     * @return angle in degrees
     */
    public float getRotateZ() {
        return rotateZ;
    }

    /**
     * Gets the avatar key presses.
     * @return
     */
    public boolean[] getKeyMap() {
        return Arrays.copyOf(keyMap, 8);
    }

    public void init(GL3 gl) {

    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        update(gl, frame);
    }

    public void destroy(GL3 gl) {
    }

    /**
     * Enable flight for the avatar
     */
    public void toggleFlyMode() {
        flying = !flying; // Toggle flying
        System.out.println("Flying " + (flying?"enabled":"disabled"));
    }

    /**
     * Moves this avatar according to the key pressed.
     */
    public void update(GL3 gl, CoordFrame3D frame) {
        // First update where the avatar is facing
        if (keyMap[8]) rotateY += ROTATION_SCALE;
        if (keyMap[9]) rotateY -= ROTATION_SCALE;

        // Ground the avatar first if not flying
        float terrainAltitude = terrain.getAltitude(transX, transZ) + ALTITUDE_OFFSET;
        if (!flying && altitude > 0) {
            altitude -= 2*MOVEMENT_SCALE;
            transY = altitude + terrainAltitude;
            return;
        }

        // Then move the avatar horizontally
        if (keyMap[0] || keyMap[6]) { // W or UP
            transZ -= (float) Math.cos(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
            transX -= (float) Math.sin(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
        }
        if (keyMap[1]) {
            transZ += (float) Math.sin(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
            transX -= (float) Math.cos(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
        }
        if (keyMap[2] || keyMap[7]) { // S or DOWN
            transZ += (float) Math.cos(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
            transX += (float) Math.sin(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
        }
        if (keyMap[3]) {
            transZ -= (float) Math.sin(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
            transX += (float) Math.cos(Math.toRadians(rotateY)) * MOVEMENT_SCALE;
        }

        // Keep avatar inside the terrain
        transX = Utility.clamp(transX, 0, terrain.getWidth()-1);
        transZ = Utility.clamp(transZ, 0, terrain.getDepth()-1);

        float prevY = transY;

        // Make sure the avatar's Y matches the terrain
        transY = terrain.getAltitude(transX, transZ) + ALTITUDE_OFFSET;
        if (!flying) return;

        // Calculate the new altitude to ensure the avatar's Y value stays the
        // same since the terrain height will affect it otherwise
        altitude = prevY - transY;

        // Move the avatar vertically if in flying mode
        if (keyMap[4]) altitude -= MOVEMENT_SCALE;
        if (keyMap[5]) altitude += MOVEMENT_SCALE;
        altitude = Math.max(altitude, 0);
        transY += altitude;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((InputEvent.AUTOREPEAT_MASK & e.getModifiers()) != 0) return;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                keyMap[0] = true; break;
            case KeyEvent.VK_A:
                keyMap[1] = true; break;
            case KeyEvent.VK_S:
                keyMap[2] = true; break;
            case KeyEvent.VK_D:
                keyMap[3] = true; break;
            case KeyEvent.VK_SHIFT:
                keyMap[4] = true; break;
            case KeyEvent.VK_SPACE:
                keyMap[5] = true; break;
            case KeyEvent.VK_UP:
                keyMap[6] = true; break;
            case KeyEvent.VK_DOWN:
                keyMap[7] = true; break;
            case KeyEvent.VK_LEFT:
                keyMap[8] = true; break;
            case KeyEvent.VK_RIGHT:
                keyMap[9] = true; break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if ((InputEvent.AUTOREPEAT_MASK & e.getModifiers()) != 0) return;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_W:
                keyMap[0] = false; break;
            case KeyEvent.VK_A:
                keyMap[1] = false; break;
            case KeyEvent.VK_S:
                keyMap[2] = false; break;
            case KeyEvent.VK_D:
                keyMap[3] = false; break;
            case KeyEvent.VK_SHIFT:
                keyMap[4] = false; break;
            case KeyEvent.VK_SPACE:
                keyMap[5] = false; break;
            case KeyEvent.VK_UP:
                keyMap[6] = false; break;
            case KeyEvent.VK_DOWN:
                keyMap[7] = false; break;
            case KeyEvent.VK_LEFT:
                keyMap[8] = false; break;
            case KeyEvent.VK_RIGHT:
                keyMap[9] = false; break;
        }
    }
}
