package unsw.graphics.world.particles;

import com.jogamp.opengl.GL3;
import unsw.graphics.*;
import unsw.graphics.world.camera.Camera;

import java.awt.*;

/**
 * Displays fireworks using a particles system. Taken from NeHe Lesson #19a:
 * Fireworks
 *
 * @author Zahid, z5121750
 */
public class Emitter {

    private static final String TEXTURE_FILENAME = "res/textures/star.png";
    private static final String TEXTURE_EXT = "png";

    private static final String VERTEX_SHADER   = "shaders/asst2_particle_vert.glsl";
    private static final String FRAGMENT_SHADER = "shaders/asst2_particle_frag.glsl";

    private Shader shader;
    private Point3DBuffer velocities;
    private ColorBuffer colors;
    private int velocitiesName;
    private int colorsName;

    private Particle[] particles;
    private Texture texture;

    private int particleCount;
    private int time = 0;

    private boolean emitting = false;
    private float decay = 0.001f;
    private float gravity = -0.000001f;

    /**
     * Constructor for a particle emitter
     * @param particles to emit
     */
    public Emitter(Particle[] particles) {
        this.particles = particles;
        this.particleCount = particles.length;
    }

    /**
     * Constructor for a particle emitter
     * @param decay
     * @param gravity
     */
    public Emitter(Particle[] particles, float decay, float gravity) {
        this.particles = particles;
        this.particleCount = particles.length;
        this.decay = decay;
        this.gravity = gravity;
    }

    /**
     * Initialize the particles and copies the data to the corresponding buffers
     * @param gl
     */
    public void init(GL3 gl) {
        // Setup the particles shader
        shader = new Shader(gl, VERTEX_SHADER, FRAGMENT_SHADER);

        // Allocate the buffers
        velocities = new Point3DBuffer(particleCount);
        colors = new ColorBuffer(particleCount);

        int[] names = new int[3];
        gl.glGenBuffers(3, names, 0);
        velocitiesName = names[0];
        colorsName = names[1];

        // Load the texture image
        texture = new Texture(gl, TEXTURE_FILENAME, TEXTURE_EXT, false);

        // Update the buffers
        for (int i = 0; i < particleCount; i++) {
            velocities.put(i, particles[i].dx, particles[i].dy, particles[i].dz);
            colors.put(i, particles[i].r, particles[i].g, particles[i].b, particles[i].life);
        }

        // Create and bind buffers for velocities and colors
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, velocitiesName);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, particleCount * 3 * Float.BYTES,
                velocities.getBuffer(), GL3.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, colorsName);
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, particleCount * 4 * Float.BYTES,
                colors.getBuffer(), GL3.GL_DYNAMIC_DRAW);

        // Set the point size
        gl.glPointSize(30);
    }

    /**
     * Draws the particles on the screen
     * @param gl
     * @param camera
     * @param frame
     */
    public void draw(GL3 gl, Camera camera, CoordFrame3D frame) {
        // No need to draw if not emitting
        if (!emitting) return;

        shader.use(gl);

        // Creates an additive blend to hide the texture's alpha background
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE);

        // Disable depth writing so the textures don't overlap
        gl.glDepthMask(false);

        // Set the uniform value in the shader
        camera.setProjMatrix(gl);
        camera.setViewMatrix(gl);
        Shader.setModelMatrix(gl, frame.getMatrix());

        Shader.setPenColor(gl, Color.WHITE);
        Shader.setInt(gl, "time", time);
        Shader.setFloat(gl, "decay", decay);
        Shader.setFloat(gl, "gravity", gravity);

        // Bind the particle textures and set texture uniform
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL3.GL_TEXTURE0);
        gl.glBindTexture(GL3.GL_TEXTURE_2D, texture.getId());

        // Bind velocities and colors, set color uniform, set velocity as position uniform
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, velocitiesName);
        gl.glVertexAttribPointer(Shader.POSITION, 3, GL3.GL_FLOAT, false, 0, 0);
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, colorsName);
        gl.glVertexAttribPointer(Shader.COLOR, 4, GL3.GL_FLOAT, false, 0, 0);

        gl.glDrawArrays(GL3.GL_POINTS, 0, particles.length);
        time++;

        // Reset the changed settings
        gl.glDepthMask(true);
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Destroy the allocated buffers
     * @param gl
     */
    public void destroy(GL3 gl) {
        gl.glDeleteBuffers(2, new int[] { velocitiesName, colorsName }, 0);
        texture.destroy(gl);
    }

    /**
     * Show the particles
     */
    public void emit() {
        emitting = true;
    }

    /**
     * Don't draw the particles
     */
    public void stop() {
        emitting = false;
    }

    /**
     * Restart particles' life
     */
    public void reset() {
        time = 0;
    }
}
