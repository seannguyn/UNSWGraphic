package unsw.graphics.world.particles;


public class TestEmitter extends Emitter {

    private static final int MAX_PARTICLES = 500;
    private static final float DECAY = 0.002f;
    private static final float GRAVITY = -0.0008f;

    private static Particle[] PARTICLES = createParticles();

    public TestEmitter() {
        super(PARTICLES, DECAY, GRAVITY);
    }

    private static Particle[] createParticles() {
        Particle[] particles = new Particle[MAX_PARTICLES];
        for (int i = 0; i < MAX_PARTICLES; i++) {
            particles[i] = new TestParticle();
        }
        return particles;
    }
}
