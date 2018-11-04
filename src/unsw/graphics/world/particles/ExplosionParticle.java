package unsw.graphics.world.particles;

import java.awt.*;
import java.util.Random;

public class ExplosionParticle extends Particle {

    private static final Color[] COLORS = { Color.RED, Color.ORANGE, Color.YELLOW };
    private static final Random RANDOM = new Random();

    /**
     * Initialize velocity, color and life of particles
     */
    public ExplosionParticle() {
        float maxSpeed = 0.05f;
        float speed = (RANDOM.nextFloat() - 0.5f) * maxSpeed;
        float angle = (float) Math.toRadians(RANDOM.nextInt(360));

        // Initial speed for all the particles
        dx = speed * (float) Math.cos(angle);
        dy = (RANDOM.nextFloat() - 0.5f) * maxSpeed;
        dz = speed * (float) Math.sin(angle);

        int index = (int) ((speed + maxSpeed) / (maxSpeed * 2) * COLORS.length) % COLORS.length;

        r = COLORS[index].getRed()/255f;
        g = COLORS[index].getGreen()/255f;
        b = COLORS[index].getBlue()/255f;
        life = 1.0f;
    }
}
