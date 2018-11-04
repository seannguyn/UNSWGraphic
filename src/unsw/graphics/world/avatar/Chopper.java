package unsw.graphics.world.avatar;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.*;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.world.Bomb;
import unsw.graphics.world.Terrain;
import unsw.graphics.world.Utility;
import unsw.graphics.world.lighting.Spotlight;

import java.awt.*;
import java.io.IOException;

/**
 * Chopper avatar.
 *
 * @author Zahid, z5121750
 */
public class Chopper extends Avatar {

    // rotor angle increment in degrees
    private static final float ROTOR_SPEED = 27;

    // position of chopper components relative to the body
    private static final Point3D WING_OFFSET = new Point3D(-0.03f, 1.23f, 0.17f);
    private static final Point3D TAIL_OFFSET = new Point3D(-0.15f, 0.58f, 3.25f);
    private static final Point3D LIGHT_OFFSET = new Point3D(0.000f, 0.00f, -0.1f);

    private Spotlight spotlight;

    private Bomb[] bombs;
    private int bombCount = 0;
    private int nextBomb = 0;

    private TriangleMesh body;
    private TriangleMesh wing;
    private TriangleMesh tail;

    private Texture bodyTex;
    private Texture wingTex;
    private Texture tailTex;

    private float rotorAngle = 0;

    public Chopper(Terrain terrain) throws IOException {
        super(terrain, true);
        body = new TriangleMesh("res/models/chopper/chopper_body.ply", true, true);
        wing = new TriangleMesh("res/models/chopper/chopper_wing.ply", true, true);
        tail = new TriangleMesh("res/models/chopper/chopper_tail.ply", true, false);
        setScale(0.2f);

        // Initialize the chopper's spotlight
        Vector3 direction = new Vector3(0, 0, -1);
        spotlight = new Spotlight(LIGHT_OFFSET, direction, Color.WHITE);
    }

    /**
     * Get spotlight object
     *
     * @return
     */
    public Spotlight getSpotlight() {
        return spotlight;
    }

    /**
     * Give bombs for the chopper to drop
     *
     * @param bombs
     */
    public void arm(Bomb[] bombs) {
        this.bombs = bombs;
        this.bombCount = bombs.length;
    }

    private void bombsAway() {
        if (bombCount == 0) return;

        if (nextBomb >= bombCount) {
            for (int i = 0; i < bombCount; i++) bombs[i].reload();
            nextBomb = 0;
        }

        this.bombs[nextBomb++].drop(getPosition());
        System.out.println( (bombCount - nextBomb) + " bombs left" );
    }

    @Override
    public void init(GL3 gl) {
        super.init(gl);

        // Initialize the chopper's meshes
        body.init(gl);
        wing.init(gl);
        tail.init(gl);

        // Load the chopper's textures
        bodyTex = new Texture(gl, "res/textures/chopper/body.bmp", "bmp", false);
        //wingTex = new Texture(gl, "res/textures/chopper/wing.bmp", "bmp", false);
        //tailTex = new Texture(gl, "res/textures/chopper/tail.bmp", "bmp", false);
    }

    @Override
    public void update(GL3 gl, CoordFrame3D frame) {
        super.update(gl, frame);

        // Change the spotlight position and direction to match the chopper
        CoordFrame3D local = getLocalFrame(frame);
        spotlight.transform(local);
    }

    @Override
    public void draw(GL3 gl, CoordFrame3D frame) {
        // super will call update
        super.draw(gl, frame);

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.2f, 0.2f, 0.2f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setColor(gl, "specularCoeff", new Color(0.3f, 0.3f, 0.3f));
        Shader.setFloat(gl, "phongExp", 16f);

        // Draw textures only for the chopper's body
        Shader.setPenColor(gl, Color.WHITE);
        Shader.setBoolean(gl, "useTexture", true);
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);

        gl.glBindTexture(GL.GL_TEXTURE_2D, bodyTex.getId());
        CoordFrame3D bodyFrame = getLocalFrame(frame);
        body.draw(gl, bodyFrame);

        Shader.setBoolean(gl, "useTexture", false);
        Shader.setPenColor(gl, Color.GRAY);

        CoordFrame3D wingFrame = bodyFrame.translate(WING_OFFSET).rotateY(rotorAngle);
        wing.draw(gl, wingFrame);
        CoordFrame3D tailFrame = bodyFrame.translate(TAIL_OFFSET).rotateX(rotorAngle);
        tail.draw(gl, tailFrame);

        rotorAngle = Utility.normaliseAngle(rotorAngle + ROTOR_SPEED);
    }

    @Override
    public void destroy(GL3 gl) {
        super.destroy(gl);

        bodyTex.destroy(gl);
        wingTex.destroy(gl);
        tailTex.destroy(gl);

        body.destroy(gl);
        wing.destroy(gl);
        tail.destroy(gl);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        super.keyPressed(e);

        if ((InputEvent.AUTOREPEAT_MASK & e.getModifiers()) != 0) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_E:
                spotlight.toggle();
                break;
            case KeyEvent.VK_Q:
                toggleFlyMode();
                break;
            case KeyEvent.VK_R:
                bombsAway();
                break;
            default:
                break;
        }
    }
}
