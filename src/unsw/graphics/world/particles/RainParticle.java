package unsw.graphics.world.particles;

import java.awt.*;
import java.util.Random;

public class RainParticle extends Particle {

    private static final Color[] COLORS = {Color.BLUE, Color.cyan};//, Color.WHITE};
    private static final Random RANDOM = new Random();

    /**
     * Initialize velocity, color and life of particles
     */
    public RainParticle() {
        float maxSpeed = 0.02f;
        float speed = (RANDOM.nextFloat() - 0.5f) * maxSpeed;
        float angle = (float) Math.toRadians(RANDOM.nextInt(360));

        // Initial speed for all the particles
        dx = speed * (float) Math.cos(angle);
        dy = Math.min((RANDOM.nextFloat() - 0.7f), 0.0f);
        dz = speed * (float) Math.sin(angle);

        int index = RANDOM.nextInt(COLORS.length);

        r = (COLORS[index].getRed() / 255f) / 5f;
        g = (COLORS[index].getGreen() / 255f) / 5f;
        b = (COLORS[index].getBlue() / 255f) / 5f;
        life = 0.8f;
    }
}
