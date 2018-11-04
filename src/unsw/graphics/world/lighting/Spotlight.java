package unsw.graphics.world.lighting;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.world.Utility;

import java.awt.*;

/**
 * Spotlight class
 */
public class Spotlight extends Light{

    // Attenuation coefficients for spotlight attenuation
    // decrease the linear and quadratic coeffs for stronger lights
    private static final float ATTENUATION_CONSTANT  = 1.0f;
    private static final float ATTENUATION_LINEAR    = 0.005f;
    private static final float ATTENUATION_QUADRATIC = 0.0007f;

    // Spotlight at maximum intensity and points down on default
    private Color   intensity = Color.WHITE;
    private Vector3 direction = new Vector3(0,-1,0);
    private Point3D position  = new Point3D(0,0,0);

    private CoordFrame3D localFrame = CoordFrame3D.identity();

    private float innerCutOffAngle = 20;
    private float outerCutOffAngle = 38;

    /**
     * Constructor for a spotlight.
     * @param position
     * @param direction
     * @param intensity
     */
    public Spotlight(Point3D position, Vector3 direction, Color intensity) {
        this.position  = position;
        this.direction = direction;
        this.intensity = intensity;
    }

    /**
     * Constructor for a spotlight.
     * @param position
     */
    public Spotlight(Point3D position) {
        this.position = position;
    }

    /**
     * Sets the spotlight's position
     * @param x
     * @param y
     * @param z
     */
    public void setPosition(float x, float y, float z) {
        position = new Point3D(x, y, z);
    }

    /**
     * Sets the spotlight's position
     * @param position the new position
     */
    public void setPosition(Point3D position) {
        this.position = position;
    }

    /**
     * Change the direction the spotlight is facing
     * @param dx
     * @param dy
     * @param dz
     */
    public void setDirection(float dx, float dy, float dz) {
        direction = new Vector3(dx, dy, dz);
    }

    /**
     * Change the direction the spotlight is facing
     * @param direction the new direction
     */
    public void setDirection(Vector3 direction) {
        this.direction = direction;
    }

    /**
     * Changes how wide the spotlight will shine
     * @param angle in degrees
     */
    public void setInnerCutOffAngle(float angle) {
        innerCutOffAngle = Utility.clamp(angle, 0, 180);
    }

    /**
     * Rotate the light direction along the x axis
     * @param angle change in degrees
     */
    public void rotataX(float angle) {
        direction = CoordFrame3D.identity()
                .rotateX(angle).transform(direction);
    }

    /**
     * Rotate the light direction along the y axis
     * @param angle change in degrees
     */
    public void rotataY(float angle) {
        direction = CoordFrame3D.identity()
                .rotateY(angle).transform(direction);
    }

    /**
     * Rotate the light direction along the z axis
     * @param angle change in degrees
     */
    public void rotataZ(float angle) {
        direction = CoordFrame3D.identity()
                .rotateZ(angle).transform(direction);
    }

    public void transform(CoordFrame3D frame) {
        localFrame = frame;
    }

    public void setUniforms(GL3 gl) {
        // Transform the position and direction with the local frame
        Point3D position  = localFrame.transform(this.position);
        Vector3 direction = localFrame.transform(this.direction);
        Color   intensity = isLightOn() ? this.intensity : Color.BLACK;

        // Set the spotlight properties
        Shader.setColor(gl, "spot.intensity", intensity);
        Shader.setPoint3D(gl, "spot.position", position);
        Shader.setVector3(gl, "spot.direction", direction);
        Shader.setFloat(gl, "spot.phi", (float) Math.cos(Math.toRadians(innerCutOffAngle)));
        Shader.setFloat(gl, "spot.gamma", (float) Math.cos(Math.toRadians(outerCutOffAngle)));

        // Set this spotlight's attenuation properties
        Shader.setFloat(gl, "spot.constant", ATTENUATION_CONSTANT);
        Shader.setFloat(gl, "spot.linear", ATTENUATION_LINEAR);
        Shader.setFloat(gl, "spot.quadratic", ATTENUATION_QUADRATIC);
    }
}
