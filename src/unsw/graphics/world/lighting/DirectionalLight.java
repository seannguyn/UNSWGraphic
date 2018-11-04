package unsw.graphics.world.lighting;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Quaternion;
import unsw.graphics.Shader;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point3D;

import java.awt.*;

public class DirectionalLight extends Light{

    private Color intensity;
    private Vector3 direction;

    /**
     * Constructor for a directional light source
     *
     * @param direction
     * @param intensity
     */
    public DirectionalLight(Vector3 direction, Color intensity) {
        this.direction = direction;
        this.intensity = intensity;
    }

    /**
     * Constructor for a directional light source
     * @param dx x direction
     * @param dy y direction
     * @param dz z direction
     * @param i the intensity of the light
     */
    public DirectionalLight(float dx, float dy, float dz, float i) {
        this.direction = new Vector3(dx, dy, dz);
        this.intensity = new Color(i, i, i);
    }

    /**
     * Sets the light's intensity.
     * @return
     */
    public void setIntensity(Color intensity) {
        this.intensity = intensity;
    }

    /**
     * Get the light's intensity.
     * @return
     */
    public Color getIntensity() {
        return intensity;
    }

    /**
     * Get the light's direction.
     * @return
     */
    public Vector3 getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the light
     * @param dx x direction
     * @param dy y direction
     * @param dz z direction
     */
    public void setDirection(float dx, float dy, float dz) {
        this.direction = new Vector3(dx, dy, dz);
    }

    /**
     * Rotate the light direction along the x axis
     * @param angle change in degrees
     */
    public void rotateX(float angle) {
        direction = CoordFrame3D.identity()
                .rotateX(angle).transform(direction);
    }

    /**
     * Rotate the light direction along the y axis
     * @param angle change in degrees
     */
    public void rotateY(float angle) {
        direction = CoordFrame3D.identity()
                .rotateY(angle).transform(direction);
    }

    /**
     * Rotate the light direction along the z axis
     * @param angle change in degrees
     */
    public void rotateZ(float angle) {
        direction = CoordFrame3D.identity()
                .rotateZ(angle).transform(direction);
    }

    /**
     * Rotate the light direction using Quaternions.
     * @param q
     */
    public void rotate(Quaternion q) {
        direction = q.getFrame().transform(direction);
    }

    @Override
    public void setUniforms(GL3 gl) {
        Shader.setColor(gl, "dirlight.intensity", intensity);
        Shader.setVector3(gl, "dirlight.direction", direction);
    }
}
