package unsw.graphics.world;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A road on the terrain.
 *
 * @author Zahid, z5121750
 */
public class Road {

    private static final int SEGMENTS = 16;

    private static final String TEXTURE_FILE = "res/textures/soil.bmp";
    private static final String TEXTURE_EXT = "bmp";

    private TriangleMesh mesh;
    private Texture texture;

    private Terrain terrain;

    private List<Point3D> points;
    private float width;

    /**
     * Create a new road with the specified spine
     *
     * @param width   width of the road
     * @param spine   the Bezier control points that define the curve
     * @param terrain the Terrain the road lies on
     */
    public Road(float width, List<Point3D> spine, Terrain terrain) {
        this.width = width;
        this.points = spine;
        this.terrain = terrain;
    }

    public void init(GL3 gl) {
        mesh = createMesh();
        mesh.init(gl);
        texture = new Texture(gl, TEXTURE_FILE, TEXTURE_EXT, true);
    }

    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());

        // Set wrap mode for texture in S and T directions
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_MIRRORED_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_MIRRORED_REPEAT);

        Shader.setBoolean(gl, "useTexture", true);

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.4f, 0.4f, 0.4f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
        Shader.setColor(gl, "specularCoeff", new Color(0.0f, 0.0f, 0.0f));
        Shader.setFloat(gl, "phongExp", 1f);

        // enable polygon offset for filled polygons
        gl.glEnable(GL3.GL_POLYGON_OFFSET_FILL);
        // push this polygon to the front a little
        gl.glPolygonOffset(-1, -1);

        Shader.setPenColor(gl, Color.WHITE);
        mesh.draw(gl, frame);

        // If you do not turn this off again it will not work!
        gl.glDisable(GL3.GL_POLYGON_OFFSET_FILL);
    }

    public void destroy(GL3 gl) {
        texture.destroy(gl);
        mesh.destroy(gl);
    }

    private TriangleMesh createMesh() {

        List<Point3D> vertices  = new ArrayList<>();
        List<Vector3> normals   = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();

        float t = 0;
        float x = width / 2;
        float interval = 1.0f / SEGMENTS;
        Vector3 previous = new Vector3(0, 0, 1);

        // Get the altitude of the start of the road
        float y = terrain.getAltitude(point(0).getX(), point(0).getZ());

        while (t <= points.size() / 3) {
            Point3D phi = point(t);
            Vector3 tangent = tangent(t);
            if (tangent.isZeroVector()) tangent = previous;

            // Construct the Frenet frame, ensure that tangent is always valid
            CoordFrame3D frame = CoordFrame3D.frenet(phi, tangent);

            // Extrude the points of the road's cross section using the Frenet frame
            Point3D left = frame.transform(new Point3D(-x, y, 0));
            Point3D right = frame.transform(new Point3D(x, y, 0));

            // Store the extruded points
            vertices.add(left);
            vertices.add(right);

            // Store the respective texture coordinates
            texCoords.add(new Point2D(left.getX(), left.getZ()));
            texCoords.add(new Point2D(right.getX(), right.getZ()));

            // Normal of the road should just be pointing up
            normals.add(new Vector3(0, 1, 0));
            normals.add(new Vector3(0, 1, 0));

            previous = tangent;
            t += interval;
        }

        return new TriangleMesh(vertices, normals, indices(), texCoords);
    }

    private List<Integer> indices() {
        List<Integer> indices = new ArrayList<>();

        int quads = points.size() / 3 * (SEGMENTS - 1);
        for (int i = 0; i <= quads; i++) {
            int index = i * 2;

            indices.add(index);
            indices.add(index + 1);
            indices.add(index + 2);

            indices.add(index + 3);
            indices.add(index + 2);
            indices.add(index + 1);
        }

        return indices;
    }

    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     *
     * @param t
     * @return
     */
    private Point3D point(float t) {
        int i = (int) t;

        // Check if t is the ending point of the spine as
        // i can't be bigger that the spine size
        if (i >= points.size() / 3) {
            t = 1;
            i--;
        } else {
            t = t - i;
        }
        i *= 3;

        Point3D p0 = points.get(i++);
        Point3D p1 = points.get(i++);
        Point3D p2 = points.get(i++);
        Point3D p3 = points.get(i);

        float x = (1 - t) * (1 - t) * (1 - t) * p0.getX()
                + 3 * (1 - t) * (1 - t) * t * p1.getX()
                + 3 * (1 - t) * t * t * p2.getX()
                + t * t * t * p3.getX();

        float z = (1 - t) * (1 - t) * (1 - t) * p0.getZ()
                + 3 * (1 - t) * (1 - t) * t * p1.getZ()
                + 3 * (1 - t) * t * t * p2.getZ()
                + t * t * t * p3.getZ();

        return new Point3D(x, 0, z);
    }

    /**
     * Gets tangent of a point on the spine.
     *
     * @param t
     * @return
     */
    private Vector3 tangent(float t) {
        int i = (int) t;
        if (i != points.size() / 3) {
            t = t - i;
        } else {
            t = 1;
            i--;
        }
        i *= 3;

        Point3D p0 = points.get(i++);
        Point3D p1 = points.get(i++);
        Point3D p2 = points.get(i++);
        Point3D p3 = points.get(i);

        Vector3 v0 = p1.minus(p0);
        Vector3 v1 = p2.minus(p1);
        Vector3 v2 = p3.minus(p2);

        float x = (1 - t) * (1 - t) * v0.getX()
                + 2 * (1 - t) * t * v1.getX()
                + t * t * v2.getX();

        float z = (1 - t) * (1 - t) * v0.getZ()
                + 2 * (1 - t) * t * v1.getZ()
                + t * t * v2.getZ();

        return new Vector3(3 * x, 0, 3 * z);
    }
}
