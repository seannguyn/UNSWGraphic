package unsw.graphics.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.newt.event.InputEvent;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.world.camera.*;
import unsw.graphics.world.avatar.*;
import unsw.graphics.world.lighting.*;
import unsw.graphics.world.particles.RainCloud;


/**
 * COMP3421 Assignment 2
 * Creates a terrain a chopper can explore.
 *
 * @author Zahid, z5121750
 * @author Sean, z5055824
 */
public class World extends Application3D implements KeyListener {

    /*  The avatar is setup to appear near the middle of the
     *  Terrain on application start.
     *
     *  The world can be viewed through either the first or third
     *  person camera. Click on the window to enable mouse
     *  controls, the mouse cursor should disappear. Press C to
     *  switch between camera modes when the mouse is disabled.
     *  In third person mode, drag the mouse while the  mouse
     *  controls are enabled to adjust the camera position.
     *
     *  Basic requirements:
     *      - Terrain mesh generation           2
     *      - Terrain altitude interpolation    1
     *      - Trees	                            2
     *      - Camera	                        1
     *      - Sunlight	                        1
     *      - Avatar / 3rd person	            2
     *      - Road                              3
     *      - Torchlight	                    2
     *
     *  Extensions:
     *      - Avatar with animation             2..4
     *      - Moving sunlight                   2
     *      - Torchlight attenuation            2
     *      - Particle effects                  4
     *      - Skybox with cube mapping          ?
     *
     *
     *  Avatar Controls:
     *      UP    - move forward
     *      DOWN  - move backwards
     *      LEFT  - turn left
     *      RIGHT - turn right
     *      SPACE - move up
     *      SHIFT - move down
     *      WASD  - moves the chopper as you'd expect
     *      E     - toggle spotlight
     *      Q     - toggle avatar fly mode
     *      R     - drop bombs
     *
     *  World Controls:
     *      C     - switch camera modes
     *      T     - toggle day night cycle
     *      Y     - toggle between day and night time
     *      G     - toggle rain
     */

    private static final String VERTEX_SHADER   = "shaders/asst2_vertex.glsl";
    private static final String FRAGMENT_SHADER = "shaders/asst2_fragment.glsl";

    private static final int MAX_BOMBS = 10;

    private Shader defaultShader;

    private Terrain terrain;
    private Skybox  skybox;
    private Avatar  avatar;

    private Sunlight  sunlight;
    private Spotlight spotlight;

    private Camera camera;
    private FirstPersonCamera fpc;
    private ThirdPersonCamera tpc;
    private boolean firstPerson = true;

    private Bomb[] bombs;
    private RainCloud rain;

    /**
     * Create the world
     * @param terrain the world's terrain
     * @throws IOException
     */
    public World(Terrain terrain) throws IOException {
        super("Assignment 2", 1200, 800);
        this.terrain = terrain;
        this.avatar = new Chopper(terrain);
        this.skybox  = new Skybox(terrain);
    }

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws IOException {
        String level = "res/worlds/demo.json";
        Terrain terrain = LevelIO.load(new File(level));

        //Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

    @Override
    public void init(GL3 gl) {
        super.init(gl);

        // Initialize shader here
        defaultShader = new Shader(gl, VERTEX_SHADER, FRAGMENT_SHADER);

        // Set up lighting for the world here
        sunlight  = terrain.getSunlight();
        spotlight = ((Chopper) avatar).getSpotlight();
        spotlight.toggle();

        // Initialise all world objects here
        terrain.init(gl);
        avatar.init(gl);
        skybox.init(gl);

        // Set up cameras here
        fpc = new FirstPersonCamera(getWindow(), avatar);
        tpc = new ThirdPersonCamera(getWindow(), avatar);
        camera = fpc;

        // Provide event listeners here
        getWindow().addKeyListener(this);
        getWindow().addKeyListener(avatar);
        getWindow().addMouseListener(camera);

        bombs = new Bomb[MAX_BOMBS];
        for (int i = 0; i < MAX_BOMBS; i++) {
            bombs[i] = new Bomb(terrain);
            bombs[i].init(gl);
        }
        ((Chopper) avatar).arm(bombs); // attach bombs to da chopper

        rain = new RainCloud(terrain);
        rain.init(gl);
    }

    @Override
    public void display(GL3 gl) {
        /*
         * All lighting has to be updated here
         * Don't change order of drawing, some objects
         * have their own shaders, cbb to save shader states
         * Drawing everything using the default shader first
         * then group those with the same shaders
         */

        CoordFrame3D frame = CoordFrame3D.identity();
        defaultShader.use(gl);

        // Clear the screen and set the transformation matrices
        clearScreen(gl);
        camera.setViewMatrix(gl);
        camera.setProjMatrix(gl);

        // Set up the lighting
        sunlight.update();
        sunlight.setUniforms(gl);
        spotlight.setUniforms(gl);

        // Only draw the avatar when in third person mode
        if (firstPerson) avatar.update(gl, frame);
        else avatar.draw(gl, frame);

        terrain.draw(gl, frame);

        for (int i = 0; i < MAX_BOMBS; i++) bombs[i].draw(gl, frame);

        // Uses different shader for the particles
        for (int i = 0; i < MAX_BOMBS; i++) bombs[i].drawParticles(gl, camera);

        // Uses skybox shader
        skybox.draw(gl, camera, sunlight);

        rain.draw(gl, camera, frame);
    }

    @Override
    public void reshape(GL3 gl, int width, int height) {
        camera.reshape(gl, width, height);
    }

    @Override
    public void destroy(GL3 gl) {
        super.destroy(gl);
        avatar.destroy(gl);
        terrain.destroy(gl);
        skybox.destroy(gl);
        rain.destroy(gl);

        for (int i = 0; i < MAX_BOMBS; i++) bombs[i].destroy(gl);
    }

    /**
     * Switch between first and third person veis.
     */
    public void switchCamera() {
        getWindow().removeMouseListener(camera);
        firstPerson = !firstPerson;
        camera = firstPerson ? fpc : tpc;
        getWindow().addMouseListener(camera);

        // Change the default direction spotlight is facing
        if (firstPerson)
            spotlight.setDirection(0,0,-1);
        else
            spotlight.setDirection(0, -2, -1.7f);
    }

    // taken form super.display()
    private void clearScreen(GL3 gl) {
        // Set the clear color.
        gl.glClearColor(getBackground().getRed()/255f, getBackground().getGreen()/255f,
                getBackground().getBlue()/255f, getBackground().getAlpha()/255f);

        // Clear the screen with the defined clear color
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Accepts only one pressed event, not repeating
        if ((InputEvent.AUTOREPEAT_MASK & e.getModifiers()) != 0) return;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_C:
                if (camera.isInFocus()) return;
                switchCamera();
                break;
            case KeyEvent.VK_T:
                sunlight.toggleDayNightCycle();
                break;
            case KeyEvent.VK_Y:
                sunlight.toggleDayNight();
                break;
            case KeyEvent.VK_G:
                rain.toggle();
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
