package unsw.graphics.world.particles;

import java.util.Random;

/**
 * Particle class
 */
public abstract class Particle {
    public float life;       // how alive it is
    public float r, g, b;    // color
    public float dx, dy, dz; // speed in the direction

    public float posX, posY;

    /**
     * Initialize velocity, color and life of particles
     */
    public Particle() {
        r = g = b = life = 1.0f;
        dx = dy = dz = posX = posY = 0;
    }
}