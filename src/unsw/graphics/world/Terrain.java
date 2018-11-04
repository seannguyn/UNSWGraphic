package unsw.graphics.world;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.world.lighting.Sunlight;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author Zahid, z5121750
 * @author Sean, z5055824
 */
public class Terrain extends TriangleMesh{

    private int width;
    private int depth;
    private float[][] altitudes;

    private Sunlight sunlight;
    private List<Tree> trees;
    private List<Road> roads;

    private Texture texture;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight, List<Point3D> vertices,
                   List<Integer> indices, List<Point2D> texCoords) {
        super(vertices, indices, true, texCoords);

        this.width = width;
        this.depth = depth;
        this.sunlight = new Sunlight(sunlight);

        this.trees = new ArrayList<>();
        this.roads = new ArrayList<>();

        this.altitudes = new float[depth][width];
        for (Point3D v : vertices)
            this.altitudes[(int) v.getZ()][(int) v.getX()]= v.getY();
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        this.sunlight.setDirection(dx, dy, dz);
    }

    /**
     * Gets the sunlight object.
     * @return
     */
    public Sunlight getSunlight() {
        return sunlight;
    }

    /**
     * Get the getAltitude at grid index x and z.
     *
     * @param x
     * @param z
     * @return
     */
    public float getAltitude(int x, int z) {
        return this.altitudes[z][x];
    }

    /**
     * Get the getAltitude at an arbitrary point.
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * @param x
     * @param z
     * @return
     */
    public float getAltitude(float x, float z) {
        if (x < 0 || z < 0) return 0;
        if (x >= width || z >= depth) return 0;
        /*
         * The point will lie somewhere in between four vertices
         *
         *    [Left][Top]     [Right][Top]
         *              +-----+
         *              |    /|
         *              |  /  |
         *              |/    |
         *              +-----+
         * [Left][Bottom]     [Right][Bottom]
         *
         * Check which triangle the point is on, then use bilinear interpolation
         *
         */

        int l = (int) x;  // Left index
        int r = l + 1;    // Right index
        int t = (int) z;  // Top index
        int b = t + 1;    // Bottom index

        // Edge cases for the whole terrain
        // Altitude of last, bottom rightmost vertex
        if (r == width && b == depth) return this.altitudes[t][l];
        // Altitude along right border
        if (r == width)
            return this.altitudes[b][l]*(z-t) + this.altitudes[t][l]*(b-z);
        // Altitude along bottom border
        if (b == depth)
            return this.altitudes[t][l]*(r-x) + this.altitudes[t][r]*(x-l);

        float dt = z - t; // The point's distance from top
        float db = b - z; // Distance from the bottom
        float dl = x - l; // Distance from left
        float dr = r - x; // Distance from right

        float dfd;    // Distance from the diagonal line
        float ml, mr; // Altitudes at left and right intercept

        if (dl*dt - dr*db < 0) {
            // If area of dl*dt is smaller that the opposite, the point
            // is inside the top left triangle, use linear interpolation
            // to get altitude at intercept at line tl-bl, vertical line
            ml = this.altitudes[b][l]*dt + this.altitudes[t][l]*db;

            // Get altitude at intercept of tr-bl, diagonal line
            // Calculate using relative distance from top and bottom
            mr = this.altitudes[b][l]*dt + this.altitudes[t][r]*db;

            // From db = dl + horizontal distance from point to diagonal
            // For any point on the diagonal, db = dl, drawing it out helps
            dfd = db - dl;

            return (mr * dl + ml * dfd) / (dl + dfd);
        }

        // Inside bottom right triangle, get intercept at lines, tr-bl and tr-br
        // Does the same as above, but changing the appropriate values
        mr = this.altitudes[b][r]*dt + this.altitudes[t][r]*db;
        ml = this.altitudes[b][l]*dt + this.altitudes[t][r]*db;
        dfd = dt - dr;
        return (ml * dr + mr * dfd) / (dr + dfd);
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the getAltitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = getAltitude(x, z);
        try {
            Tree tree = new Tree(x, y, z);
            trees.add(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Add a road. 
     *
     */
    public void addRoad(float width, List<Point3D> spine) {
        Road road = new Road(width, spine, this);
        roads.add(road);        
    }

    @Override
    public void init(GL3 gl) {
        super.init(gl);

        texture = new Texture(gl, "res/textures/grass.bmp", "bmp", true);
        // Set wrap mode for texture in S and T directions
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_MIRRORED_REPEAT);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_MIRRORED_REPEAT);

        for (Tree t : this.trees) t.init(gl);
        for (Road r : this.roads) r.init(gl);
    }

    @Override
    public void draw(GL3 gl, CoordFrame3D frame) {
        Shader.setBoolean(gl, "useTexture", true);
        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.2f, 0.2f, 0.2f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.7f, 0.8f, 0.7f));
        Shader.setColor(gl, "specularCoeff", new Color(0.0f, 0.0f, 0.0f));
        Shader.setFloat(gl, "phongExp", 1f);

        Shader.setPenColor(gl, Color.WHITE);
        super.draw(gl, frame);

        for (Tree t : this.trees) t.draw(gl, frame);
        for (Road r : this.roads) r.draw(gl, frame);
    }

    @Override
    public void destroy(GL3 gl) {
        for (Tree t : this.trees) t.destroy(gl);
        for (Road r : this.roads) r.destroy(gl);
        texture.destroy(gl);
        super.destroy(gl);
    }
}
