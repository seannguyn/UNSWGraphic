package unsw.graphics.world;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;

/**
 * COMMENT: Comment LevelIO 
 *
 * @author malcolmr
 */
public class LevelIO {

    /**
     * Load a terrain object from a JSON file
     * 
     * @param mapFile
     * @return
     * @throws FileNotFoundException 
     */
    public static Terrain load(File mapFile) throws FileNotFoundException {

        Reader in = new FileReader(mapFile);
        JSONTokener jtk = new JSONTokener(in);
        JSONObject jsonTerrain = new JSONObject(jtk);

        JSONArray jsonSun = jsonTerrain.getJSONArray("sunlight");
        float dx = (float)jsonSun.getDouble(0);
        float dy = (float)jsonSun.getDouble(1);
        float dz = (float)jsonSun.getDouble(2);

        int width = jsonTerrain.getInt("width");
        int depth = jsonTerrain.getInt("depth");

        List<Point3D> vertices  = new ArrayList<>();
        List<Point2D> texCoords = new ArrayList<>();

        JSONArray jsonAltitude = jsonTerrain.getJSONArray("altitude");
        for (int i = 0; i < jsonAltitude.length(); i++) {
            float x = i % width;
            float z = i / width;
            float y = (float) jsonAltitude.getDouble(i);

            vertices.add(new Point3D(x, y, z));
            texCoords.add(new Point2D(x, z));
        }

        List<Integer> indices = getIndices(width, depth);
        Terrain terrain = new Terrain(width, depth, new Vector3(dx, dy, dz), vertices, indices, texCoords);

        if (jsonTerrain.has("trees")) {
            JSONArray jsonTrees = jsonTerrain.getJSONArray("trees");
            for (int i = 0; i < jsonTrees.length(); i++) {
                JSONObject jsonTree = jsonTrees.getJSONObject(i);
                float x = (float) jsonTree.getDouble("x");
                float z = (float) jsonTree.getDouble("z");
                terrain.addTree(x, z);
            }
        }
        
        if (jsonTerrain.has("roads")) {
            JSONArray jsonRoads = jsonTerrain.getJSONArray("roads");
            for (int i = 0; i < jsonRoads.length(); i++) {
                JSONObject jsonRoad = jsonRoads.getJSONObject(i);
                float w = (float) jsonRoad.getDouble("width");
                
                JSONArray jsonSpine = jsonRoad.getJSONArray("spine");
                List<Point3D> spine = new ArrayList<>();

                for (int j = 0; j < jsonSpine.length()/2; j++) {
                    spine.add(new Point3D((float)jsonSpine.getDouble(2*j),0, (float)jsonSpine.getDouble(2*j+1)));
                }
                terrain.addRoad(w, spine);
            }
        }

        return terrain;
    }

    private static List<Integer> getIndices(int width, int depth) {
        List<Integer> indices   = new ArrayList<>();

        for (int i = 0; i < width*(depth-1); i++) {
            // Ignore rightmost vertices
            if (i % width == (width-1)) continue;

            // Top left triangle
            indices.add(i);
            indices.add(i + width);
            indices.add(i + 1);

            // Bottom right triangle
            indices.add(i + 1);
            indices.add(i + width);
            indices.add(i + width + 1);
        }

        return indices;
    }

    private List<Point3D> getFaceNormalVertices(List<Point3D> vertices, int width, int depth) {
        List<Point3D> faceVertices = new ArrayList<>();

        for (int i = 0; i < width*(depth-1); i++) {
            // Ignore rightmost vertices
            if (i % width == (width-1)) continue;

            // Top left triangle
            faceVertices.add(vertices.get(i + width));
            faceVertices.add(vertices.get(i + 1));
            faceVertices.add(vertices.get(i));

            // Bottom right triangle
            faceVertices.add(vertices.get(i + width));
            faceVertices.add(vertices.get(i + width + 1));
            faceVertices.add(vertices.get(i + 1));
        }

        return faceVertices;
    }
}
