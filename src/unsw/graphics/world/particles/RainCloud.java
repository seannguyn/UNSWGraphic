package unsw.graphics.world.particles;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.world.Terrain;
import unsw.graphics.world.camera.Camera;

public class RainCloud {

    private static final int MAX_PARTICLES = 1000;
    private static final float DECAY = 0.000001f;
    private static final float GRAVITY = -0.00002f;
    private static final int MAX_TIME = 180;
    private static final int HEIGHT = 10;

    private Emitter emitter;

    private float transX;
    private float transY;
    private float transZ;

    private int time = 0;
    private boolean raining = false;

    public RainCloud(Terrain terrain) {
        transX = terrain.getWidth()/2;
        transZ = terrain.getDepth()/2;
        transY = HEIGHT;
    }

    public void toggle() {
        raining = !raining;
        System.out.println("Rain " + (raining?"enabled":"disabled"));

        if (raining) emitter.emit();
        else emitter.stop();
    }

    public void init(GL3 gl) {
        Particle[] particles = new Particle[MAX_PARTICLES];
        for (int i = 0; i < MAX_PARTICLES; i++) {
            particles[i] = new RainParticle();
        }
        emitter = new Emitter(particles, DECAY, GRAVITY);
        emitter.init(gl);
    }

    public void draw(GL3 gl, Camera camera, CoordFrame3D frame) {
        CoordFrame3D localFrame = frame.translate(transX, transY, transZ);
        emitter.draw(gl, camera, localFrame);

        for (int i = 0; i < HEIGHT; i++) {
            localFrame = localFrame.translate(0, i, 0);
            emitter.draw(gl, camera, localFrame);
        }

        time = (++time % MAX_TIME);
        if (time == 0) emitter.reset();
    }

    public void destroy(GL3 gl) {
        emitter.destroy(gl);
    }


}
