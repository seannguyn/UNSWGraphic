package unsw.graphics;

/**
 * Represents a quaternion.
 * Class is immutable.
 *
 * @author Zahid, z5121750
 */
public class Quaternion {

    public static final Vector3 X_AXIS = new Vector3(1,0,0);
    public static final Vector3 Y_AXIS = new Vector3(0,1,0);
    public static final Vector3 Z_AXIS = new Vector3(0,0,1);

    private float scalar;
    private Vector3 axis;

    public static void main(String args[]) {
        Quaternion test = Quaternion.fromAxisRotate(Y_AXIS, 90);
        System.out.println(test.getFrame().getMatrix());

        System.out.println(test.getFrameHomogeneous().getMatrix());

        System.out.println(CoordFrame3D.identity().rotateY(90).getMatrix());
    }

    public Quaternion(float w, float x, float y, float z) {
        this.scalar = w;
        this.axis = new Vector3(x, y, z);
    }

    public Quaternion(float scalar, Vector3 axis) {
        this.scalar = scalar;
        this.axis = axis;
    }

    public static Quaternion fromAxisRotate(Vector3 axis, float theta) {
        double radians = Math.toRadians(theta)/2.0;
        Vector3 qaxis = axis.normalize().scale((float) Math.sin(radians));
        float qscalar = (float) Math.cos(radians);
        return new Quaternion(qscalar, qaxis);
    }

    public static Quaternion fromAxisRotateRad(Vector3 axis, double angle) {
        double theta = angle/2.0;
        Vector3 qaxis = axis.normalize().scale((float) Math.sin(theta));
        float qscalar = (float) Math.cos(theta);
        return new Quaternion(qscalar, qaxis);
    }

    public static Quaternion fromEulerAngles(float dx, float dy, float dz) {
        Quaternion qx = fromAxisRotate(X_AXIS, dx);
        Quaternion qy = fromAxisRotate(Y_AXIS, dy);
        Quaternion qz = fromAxisRotate(Z_AXIS, dz);
        return qx.multiply(qy).multiply(qz);
    }

    public float length() {
        return (float) Math.sqrt(scalar * scalar + axis.dotp(axis));
    }

    public Quaternion normalize() {
        float length = length();
        return new Quaternion(scalar / length, axis.scale(1.0f / length));
    }

    public Quaternion conjugate() {
        return new Quaternion(scalar, axis.negate());
    }

    public Quaternion plus(Quaternion q) {
        return new Quaternion(scalar + q.scalar, axis.plus(q.axis));
    }

    public Quaternion minus(Quaternion q) {
        return new Quaternion(scalar - q.scalar, axis.minus(q.axis));
    }

    public Quaternion multiply(Quaternion q) {
        float newScalar = this.scalar * q.scalar - (this.axis.dotp(q.axis));
        Vector3 newAxis = q.axis.scale(this.scalar)
                .plus(this.axis.scale(q.scalar))
                .plus(this.axis.cross(q.axis));
        return new Quaternion(newScalar, newAxis);
    }

    /**
     * Calculates the rotation transform matrix from the quaternion.
     * @return
     */
    public CoordFrame3D getFrame() {
        float w = scalar;
        float x = axis.getX();
        float y = axis.getY();
        float z = axis.getZ();

        // Formula from wikipedia, the inhomogeneous expression
        // https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles
        Vector3 i = new Vector3(1.0f - 2.0f*(y*y + z*z), 2.0f*(x*y + w*z), 2.0f*(x*z - w*y));
        Vector3 j = new Vector3(2.0f*(x*y - w*z), 1.0f - 2.0f*(x*x+z*z), 2.0f*(w*x + y*z));
        Vector3 k = new Vector3(2.0f*(w*y + x*z), 2.0f*(y*z - w*x), 1.0f - 2.0f*(x*x + y*y));

        return new CoordFrame3D(i, j, k);
    }

    /**
     * Calculates the rotation transform matrix from the quaternion.
     * @return
     */
    public CoordFrame3D getFrameHomogeneous() {
        float w = scalar;
        float x = axis.getX();
        float y = axis.getY();
        float z = axis.getZ();

        // Formula from the same wikipedia page as above, the homogeneous expression
        // Not sure what this is for, but don't want to retype it
        Vector3 i = new Vector3(w*w + x*x - y*y - z*z, 2.0f*(x*y + w*z), 2.0f*(x*z - w*y));
        Vector3 j = new Vector3(2.0f*(x*y - w*z), w*w - x*x + y*y - z*z, 2.0f*(y*z - w*x));
        Vector3 k = new Vector3(2.0f*(w*y + x*z), 2.0f*(y*z - w*x), w*w - x*x - y*y + z*z);
        return new CoordFrame3D(i, j, k);
    }

    @Override
    public String toString() {
        return "w: " + this.scalar + " " + this.axis;
    }
}
