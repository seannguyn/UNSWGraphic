package unsw.graphics.world;

import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * Tree model from ply file.
 *
 * @author Zahid, z5121750
 * @author Sean, z5055824
 */
public class Tree {

    private static TriangleMesh mesh;
    private static int instances = 0;
    private static Random rand = new Random();

    private Point3D position;
    private float rotateY;
    
    public Tree(float x, float y, float z) {
        position = new Point3D(x, y, z);
        rotateY = rand.nextInt(360);
    }
    
    public Point3D getPosition() {
        return position;
    }

    public void init(GL3 gl) {
        if (Tree.mesh != null) {
            Tree.instances++;
            return;
        }

        try {
            Tree.mesh = new TriangleMesh("res/models/tree.ply");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tree.mesh.init(gl);
        Tree.instances++;
    }

    public void draw(GL3 gl, CoordFrame3D frame) {

        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.1f, 0.1f, 0.1f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.8f, 0.8f, 0.8f));
        Shader.setColor(gl, "specularCoeff", new Color(0.1f, 0.1f, 0.1f));
        Shader.setFloat(gl, "phongExp", 16f);

        Shader.setBoolean(gl, "useTexture", false);

        CoordFrame3D modelframe = frame.translate(position)
                .rotateY(rotateY)
                .scale(0.1f, 0.1f, 0.1f)
                .translate(0, 4.5f, 0.55f);

        Shader.setPenColor(gl, new Color(70, 40, 11));
        Tree.mesh.draw(gl, modelframe);
    }

    public void destroy(GL3 gl) {
        Tree.instances--;
        if (Tree.instances == 0) Tree.mesh.destroy(gl);
    }
}
