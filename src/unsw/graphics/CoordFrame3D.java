/**
 * 
 */
package unsw.graphics;

import java.awt.Color;

import com.jogamp.opengl.GL3;

import unsw.graphics.geometry.Line3D;
import unsw.graphics.geometry.Point3D;

/**
 * A coordinate frame in 3D.
 * 
 * Coordinate frames allow for geometry to be transformed before being drawn to the canvas.
 * 
 * @author Robert Clifton-Everest
 * @author Zahid, z5121750
 */
public class CoordFrame3D {
    private Matrix4 matrix;
    
    /**
     * Use the given 4X4 matrix as a coordinate frame.
     * 
     * @param matrix
     */
    public CoordFrame3D(Matrix4 matrix) {
        this.matrix = matrix;
    }

    /**
     * Creates a coordinate frame from the give i, j and k vectors with
     * origin at (0,0,0).
     *
     * @param i
     * @param j
     * @param k
     */
    public CoordFrame3D(Vector3 i, Vector3 j, Vector3 k) {
        Point3D origin = new Point3D(0,0,0);
        this.matrix = new Matrix4(i, j, k, origin);
    }
    
    /**
     * The identity coordinate frame has an i-axis of (1,0,0), a j-axis of (0,1,0), a k-axis of 
     * (0,0,1) and an origin at (0,0,0)
     * @return
     */
    public static CoordFrame3D identity() {
        return new CoordFrame3D(Matrix4.identity());
    }

    /**
     * Calculates the Frenet Frame from the new origin and the tangent z axis.
     * @return
     */
    public static CoordFrame3D frenet(Point3D phi, Vector3 tangent) {
        Vector3 i = new Vector3(-tangent.getZ(), 0, tangent.getX()).normalize();
        Vector3 j = i.cross(tangent).normalize();
        Vector3 k = tangent.normalize();
        return new CoordFrame3D(new Matrix4(i, j, k, phi));
    }

    /**
     * A new coordinate frame that is the translation of this frame along the given vector.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public CoordFrame3D translate(float x, float y, float z) {
        return new CoordFrame3D(matrix.multiply(Matrix4.translation(x, y, z)));
    }
    
    /**
     * Treating the argument as a displacement from this coordinate frame, returns a new coordinate 
     * frame that is a translation of this frame along that displacement.
     * @param point
     * @return
     */
    public CoordFrame3D translate(Point3D point) {
        return new CoordFrame3D(matrix.multiply(Matrix4.translation(point)));
    }
    
    /**
     * A new coordinate frame that is a rotation of this frame by the given degrees around the 
     * x-axis.
     * @param degrees
     * @return
     */
    public CoordFrame3D rotateX(float degrees) {
        return new CoordFrame3D(matrix.multiply(Matrix4.rotationX(degrees)));
    }
    
    /**
     * A new coordinate frame that is a rotation of this frame by the given degrees around the 
     * y-axis.
     * @param degrees
     * @return
     */
    public CoordFrame3D rotateY(float degrees) {
        return new CoordFrame3D(matrix.multiply(Matrix4.rotationY(degrees)));
    }

    /**
     * A new coordinate frame that is a rotation of this frame by the given degrees around the 
     * z-axis.
     * @param degrees
     * @return
     */
    public CoordFrame3D rotateZ(float degrees) {
        return new CoordFrame3D(matrix.multiply(Matrix4.rotationZ(degrees)));
    }

    /**
     * A new coordinate frame that is a rotation of this frame by the given degrees around the
     * respective axis in the given order.
     * @return
     */
    public CoordFrame3D rotate(float thetaX, float thetaY, float thetaZ) {
        return new CoordFrame3D(matrix.multiply(Matrix4.rotationX(thetaX))
                .multiply(Matrix4.rotationY(thetaY)
                .multiply(Matrix4.rotationZ(thetaZ))));
    }

    /**
     * A new coordinate frame that is this frame scaled by the given amount in the x, y, and z 
     * directions.
     * @param x
     * @param y
     * @param z
     * @return
     */
    public CoordFrame3D scale(float x, float y, float z) {
        return new CoordFrame3D(matrix.multiply(Matrix4.scale(x,y,z)));
    }

    /**
     * A new coordinate frame that is this frame scaled by the given amount in the x, y, and z
     * directions.
     * @param s
     * @return
     */
    public CoordFrame3D scale(float s) {
        return new CoordFrame3D(matrix.multiply(Matrix4.scale(s,s,s)));
    }

    /**
     * Get the matrix representation of this coordinate frame.
     * @return
     */
    public Matrix4 getMatrix() {
        return matrix;
    }
    
    /**
     * Draw the coordinate frame on the canvas.
     * 
     * This method is useful for debugging.
     * 
     * @param gl
     */
    public void draw(GL3 gl) {
       Line3D iAxis = new Line3D(0f,0f,0f, 1f,0f,0f); 
       Line3D jAxis = new Line3D(0f,0f,0f, 0f,1f,0f);
       Line3D kAxis = new Line3D(0f,0f,0f, 0f,0f,1f);
       Shader.setPenColor(gl, Color.RED);
       iAxis.draw(gl,this);
       Shader.setPenColor(gl, Color.GREEN);
       jAxis.draw(gl,this);
       Shader.setPenColor(gl, Color.BLUE);
       kAxis.draw(gl,this);
       Shader.setPenColor(gl, Color.BLACK);
    }

    /**
     * Transform the given point under this coordinate frame.
     * @param p
     * @return
     */
    public Point3D transform(Point3D p) {
        return matrix.multiply(p.asHomogenous()).asPoint3D();
    }

    /**
     * Transform the given vector under this coordinate frame.
     * @param v
     * @return
     */
    public Vector3 transform(Vector3 v) {
        return matrix.multiply(v.extend()).trim();
    }

    /**
     * Rotates this coordinate frame by the given quaternion.
     * @param q
     * @return
     */
    public CoordFrame3D rotate(Quaternion q) {
        Matrix4 rotm = q.getFrame().matrix;
        return new CoordFrame3D(matrix.multiply(rotm));
    }
}
