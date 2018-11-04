package unsw.graphics.world;

import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.world.camera.Camera;
import unsw.graphics.world.particles.Emitter;
import unsw.graphics.world.particles.ExplosionParticle;
import unsw.graphics.world.particles.Particle;

import java.awt.*;
import java.io.IOException;

public class Bomb {

    private static final int MAX_PARTICLES = 500;
    private static final float DECAY = 0.02f;
    private static final float GRAVITY = -0.0008f;

    private TriangleMesh mesh;
    private Emitter explosion;
    private Terrain terrain;

    private float transX = 0;
    private float transY = 0;
    private float transZ = 0;

    private CoordFrame3D localFrame;

    private float ground = 1;

    private boolean dropped = false;
    private boolean exploded = false;

    /**
     * Creates a grenade
     *
     * @param terrain needed for collision check
     */
    public Bomb(Terrain terrain) {
        this.terrain = terrain;
    }

    public boolean hasExploded() {
        return exploded;
    }

    /**
     * Drop the grenade to make it explode
     *
     * @param pos position to drop bomb
     */
    public void drop(Point3D pos) {
        transX = pos.getX();
        transY = pos.getY();
        transZ = pos.getZ();

        dropped = true;
        // For collision check
        ground = terrain.getAltitude(transX, transZ);
        // So particles are drawn when ground hit
        explosion.emit();
    }

    /**
     * Reuse bomb object
     */
    public void reload() {
        dropped = false;
        exploded = false;
        // reset the explosion particles
        explosion.stop();
        explosion.reset();
    }

    /**
     * Move data into buffers
     *
     * @param gl
     */
    public void init(GL3 gl) {
        // create triangle mesh
        try {
            mesh = new TriangleMesh("res/models/sphere.ply", true, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mesh.init(gl);


        // initialize particles and emitter
        Particle[] particles = new Particle[MAX_PARTICLES];
        for (int i = 0; i < MAX_PARTICLES; i++) particles[i] = new ExplosionParticle();
        explosion = new Emitter(particles, DECAY, GRAVITY);
        explosion.init(gl);
    }

    /**
     * Draw the bombs
     * @param gl
     * @param frame
     */
    public void draw(GL3 gl, CoordFrame3D frame) {
        // Only display the grenade once it's been 'dropped'
        if (!dropped) return;

        localFrame = frame.translate(transX, transY, transZ);

        // Show explosion particles when grenade hits the ground
        if (transY <= ground) {
            exploded = true;
            return;
        }

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.2f, 0.2f, 0.2f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setColor(gl, "specularCoeff", new Color(0.3f, 0.3f, 0.3f));
        Shader.setFloat(gl, "phongExp", 16f);

        // Draw textures only for the chopper's body
        Shader.setPenColor(gl, Color.DARK_GRAY);
        Shader.setBoolean(gl, "useTexture", false);

        localFrame = localFrame.scale(0.1f, 0.1f, 0.1f);
        mesh.draw(gl, localFrame);
        transY += 80 * GRAVITY;
    }

    /**
     * Draw the explosion
     * @param gl
     * @param camera
     */
    public void drawParticles(GL3 gl, Camera camera) {
        if (!dropped || !exploded) return;
        explosion.draw(gl, camera, localFrame);
    }

    public void destroy(GL3 gl) {
        explosion.destroy(gl);
        mesh.destroy(gl);
    }
}
