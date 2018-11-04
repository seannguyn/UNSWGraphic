package unsw.graphics.world;

/**
 * Useful maths methods
 *
 * @author Zahid, z5121750
 */
public class Utility {

    /**
     * Normalise an angle to the range [-180, 180)
     *
     * @param angle
     * @return
     */
    public static float normaliseAngle(float angle) {
        return ((angle + 180f) % 360f + 360f) % 360f - 180f;
    }

    /**
     * Clamp a value to the given range
     *
     * @param value
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

}