package unsw.graphics.world.particles;

import java.util.Random;

public class TestParticle extends Particle {

    private static final float[][] colors = { // rainbow of 12 colors
            { 1.0f, 0.5f, 0.5f }, { 1.0f, 0.75f, 0.5f },
            { 1.0f, 1.0f, 0.5f }, { 0.75f, 1.0f, 0.5f },
            { 0.5f, 1.0f, 0.5f }, { 0.5f, 1.0f, 0.75f },
            { 0.5f, 1.0f, 1.0f }, { 0.5f, 0.75f, 1.0f },
            { 0.5f, 0.5f, 1.0f }, { 0.75f, 0.5f, 1.0f },
            { 1.0f, 0.5f, 1.0f }, { 1.0f, 0.5f, 0.75f } };

    private Random rand = new Random();

    /**
     * Initialize velocity, color and life of particles
     */
    public TestParticle() {
        // Generate a random speed and direction in polar coordinate, then
        // resolve them into x and y.
        float maxSpeed = 0.01f;
        float speed = 0.02f + (rand.nextFloat() - 0.5f) * maxSpeed;
        float angle = (float) Math.toRadians(rand.nextInt(360));

        float speedYGlobal = 0.1f;

        // Initial speed for all the particles
        dx = speed * (float) Math.cos(angle);
        dy = speed * (float) Math.sin(angle) + speedYGlobal;
        dz = (rand.nextFloat() - 0.5f) * maxSpeed;

        int colorIndex = (int) (((speed - 0.02f) + maxSpeed)
                / (maxSpeed * 2) * colors.length) % colors.length;

        // Pick a random color
        r = colors[colorIndex][0];
        g = colors[colorIndex][1];
        b = colors[colorIndex][2];

        // Initially it's fully alive
        life = 1.0f;
    }
}
