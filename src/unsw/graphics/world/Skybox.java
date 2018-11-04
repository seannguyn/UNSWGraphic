package unsw.graphics.world;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.world.camera.Camera;
import unsw.graphics.world.lighting.Sunlight;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages a skybox.
 *
 * Ref: https://learnopengl.com/Advanced-OpenGL/Cubemaps
 *
 * @author Zahid, z5121750
 */
public class Skybox {

    private static final float SIZE = 100f;

    private Shader  shader;
    private Texture cubemap;
    private TriangleMesh mesh;

    /**
     * Constructor for skybox
     */
    public Skybox(Terrain terrain) {
        mesh = new TriangleMesh(vertices(), false);
    }

    /**
     * Initialize the skybox.
     * @param gl
     */
    public void init(GL3 gl) {
        mesh.init(gl);

        shader = new Shader(gl, "shaders/asst2_skybox_vert.glsl",
                "shaders/asst2_skybox_frag.glsl");

        cubemap = new Texture(gl,
                "res/textures/skybox/Daylight Box_Left.bmp",
                "res/textures/skybox/Daylight Box_Right.bmp",
                "res/textures/skybox/Daylight Box_Bottom.bmp",
                "res/textures/skybox/Daylight Box_Top.bmp",
                "res/textures/skybox/Daylight Box_Front.bmp",
                "res/textures/skybox/Daylight Box_Back.bmp",
                false, false);
    }

    /**
     * Draw the skybox.
     * @param gl
     * @param camera
     * @param sunlight
     */
    public void draw(GL3 gl, Camera camera, Sunlight sunlight) {
        shader.use(gl);
        camera.setSkyboxMatrices(gl);
        sunlight.setUniforms(gl);

        Shader.setPenColor(gl, Color.WHITE);

        Shader.setInt(gl, "tex", 0);
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_CUBE_MAP, cubemap.getId());

        gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MAG_FILTER, GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);
        gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL3.GL_TEXTURE_CUBE_MAP, GL3.GL_TEXTURE_WRAP_R, GL3.GL_CLAMP_TO_EDGE);

        mesh.draw(gl);
    }

    /**
     * Destroy mesh of the skybox.
     * @param gl
     */
    public void destroy(GL3 gl) {
        cubemap.destroy(gl);
        mesh.destroy(gl);
    }

    private List<Point3D> vertices() {
        List<Point3D> vertices = new ArrayList<>();

        for (int i = 0; i < VERTICES.length/3; i++) {
            float x = VERTICES[i*3] ;
            float y = VERTICES[i*3 + 1];
            float z = VERTICES[i*3 + 2];
            vertices.add(new Point3D(x, y, z));
        }

        return vertices;
    }

    private static final float[] VERTICES = {
            -SIZE,  SIZE, -SIZE,
            -SIZE, -SIZE, -SIZE,
             SIZE, -SIZE, -SIZE,
             SIZE, -SIZE, -SIZE,
             SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE, -SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

             SIZE, -SIZE, -SIZE,
             SIZE, -SIZE,  SIZE,
             SIZE,  SIZE,  SIZE,
             SIZE,  SIZE,  SIZE,
             SIZE,  SIZE, -SIZE,
             SIZE, -SIZE, -SIZE,

            -SIZE, -SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
             SIZE,  SIZE,  SIZE,
             SIZE,  SIZE,  SIZE,
             SIZE, -SIZE,  SIZE,
            -SIZE, -SIZE,  SIZE,

            -SIZE,  SIZE, -SIZE,
             SIZE,  SIZE, -SIZE,
             SIZE,  SIZE,  SIZE,
             SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE,  SIZE,
            -SIZE,  SIZE, -SIZE,

            -SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
             SIZE, -SIZE, -SIZE,
             SIZE, -SIZE, -SIZE,
            -SIZE, -SIZE,  SIZE,
             SIZE, -SIZE,  SIZE
    };
}
