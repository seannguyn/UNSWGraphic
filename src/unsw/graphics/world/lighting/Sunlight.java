package unsw.graphics.world.lighting;

import com.jogamp.opengl.GL3;
import unsw.graphics.Quaternion;
import unsw.graphics.Shader;
import unsw.graphics.Vector3;

import java.awt.*;

public class Sunlight extends DirectionalLight {

    private static final Color DEFAULT_INTENSITY = Color.WHITE;//new Color(0.8f, 0.8f, 0.8f);

    // length of day and night
    private static final int DAY = 300;
    private static final int NIGHT = 100;

    private Color ambientIntensity = new Color(0.4f, 0.4f, 0.4f);

    private boolean cycleOn = false;
    private Vector3 rotationAxis;
    private int time = 0;

    /**
     * Constructor for a the sunlight
     *
     * @param direction direction to the source
     */
    public Sunlight(Vector3 direction) {
        this(direction, DEFAULT_INTENSITY);
    }

    /**
     * Constructor for a the sunlight
     *
     * @param direction
     * @param intensity
     */
    public Sunlight(Vector3 direction, Color intensity) {
        super(direction, intensity);

        // horizontal cross the direction, so the sun rotates away
        rotationAxis = new Vector3(-direction.getZ(), 0, direction.getX());

        // if sun is directly above terrain, sin of angle will be 1, which is the noon time
        double length = Math.sqrt(direction.dotp(direction));
        double sinval = direction.getY() / length; // sin value
        double ratio  = Math.asin(sinval) / Math.PI;
        time = (int) (ratio * DAY);
    }

    /**
     * Check time
     * @return
     */
    public boolean isDaytime() {
        return time < DAY;
    }

    /**
     * Check time
     * @return
     */
    public boolean isNightTime() {
        return time >= DAY;
    }

    /**
     * Turns incrementing time progression and the sun moving on/off
     * @return
     */
    public void toggleDayNightCycle() {
        cycleOn = !cycleOn;
        System.out.println("Day/Night cycle " + (cycleOn?"enabled":"disabled"));
    }

    /**
     * Turns incrementing time progression and the sun moving on/off
     * @return
     */
    public void toggleDayNight() {
        if (isDaytime()) {
            setDirection(0, -1, 0);
            time = DAY + NIGHT / 2;
        } else {
            setDirection(0, 1, 0);
            time = DAY / 2;
        }
    }

    /**
     * Check if sun is moving
     * @return
     */
    public boolean isDayNightCycleOn() {
        return cycleOn;
    }

    /**
     * Updates the sunlight according to the time
     */
    public void update() {
        if (!cycleOn) return;
        time = ++time % (DAY + NIGHT);
        double angle = Math.PI / (isDaytime() ? DAY : NIGHT);
        Quaternion q = Quaternion.fromAxisRotateRad(rotationAxis, angle);
        rotate(q);
    }

    @Override
    public void setUniforms(GL3 gl) {
        Shader.setColor(gl, "sunlight.intensity", getIntensity());
        Shader.setVector3(gl, "sunlight.direction", getDirection());
        Shader.setFloat(gl, "sunlight.time", time);

        Shader.setFloat(gl, "sunlight.daytime", DAY);
        Shader.setFloat(gl, "sunlight.nighttime", NIGHT);

        Shader.setColor(gl, "ambientIntensity", ambientIntensity);
    }
}
