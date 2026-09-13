/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2960
 */
package com.killeffect.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.killeffect.client.BBAnimation;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;

@Environment(value=EnvType.CLIENT)
public final class BBModel {
    public final String modelId;
    public final String name;
    public final float width;
    public final float height;
    public final List<Texture> textures;
    public final List<Cube> cubes;
    public final List<Bone> roots;
    public final List<BBAnimation> animations;

    private BBModel(String modelId, String name, float width, float height, List<Texture> textures, List<Cube> cubes, List<Bone> roots, List<BBAnimation> animations) {
        this.modelId = modelId;
        this.name = name;
        this.width = width;
        this.height = height;
        this.textures = textures;
        this.cubes = cubes;
        this.roots = roots;
        this.animations = animations;
    }

    public static BBModel load(String modelId, String namespace, String folder) {
        BBModel bBModel;
        block9: {
            String resource = "/assets/" + namespace + "/" + folder + "/" + modelId + ".bbmodel";
            InputStream stream = BBModel.class.getResourceAsStream(resource);
            try {
                if (stream == null) {
                    throw new IllegalStateException("Missing model resource: " + resource);
                }
                JsonObject root = JsonParser.parseReader((Reader)new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                bBModel = BBModel.parse(root, modelId, namespace, folder);
                if (stream == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (stream != null) {
                        try {
                            stream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (Exception e) {
                    throw new IllegalStateException("Could not load bbmodel " + modelId, e);
                }
            }
            stream.close();
        }
        return bBModel;
    }

    public static BBModel parse(JsonObject root, String modelId, String namespace, String folder) {
        String name = BBModel.text(root, "name", modelId);
        JsonObject resolution = root.getAsJsonObject("resolution");
        float resWidth = BBModel.number(resolution, "width", 16.0f);
        float resHeight = BBModel.number(resolution, "height", 16.0f);
        String textureFolder = folder.startsWith("models/") ? folder.substring("models/".length()) : folder;
        ArrayList<Texture> textures = new ArrayList<Texture>();
        JsonArray textureEntries = root.getAsJsonArray("textures");
        if (textureEntries != null) {
            int index = 0;
            for (JsonElement element : textureEntries) {
                JsonObject t = element.getAsJsonObject();
                float w = BBModel.number(t, "width", resWidth);
                float h = BBModel.number(t, "height", resHeight);
                float uvW = BBModel.number(t, "uv_width", w);
                float uvH = BBModel.number(t, "uv_height", h);
                class_2960 fileId = class_2960.method_60655((String)namespace, (String)("textures/" + textureFolder + "/" + modelId + "_" + index + ".png"));
                textures.add(new Texture(fileId, BBModel.text(t, "path", fileId.toString()), w, h, uvW, uvH));
                ++index;
            }
        }
        if (textures.isEmpty()) {
            textures.add(new Texture(class_2960.method_60655((String)namespace, (String)("textures/" + textureFolder + "/" + modelId + ".png")), "", resWidth, resHeight, resWidth, resHeight));
        }
        HashMap<String, Cube> cubeMap = new HashMap<String, Cube>();
        ArrayList<Cube> cubes = new ArrayList<Cube>();
        JsonArray elements = root.getAsJsonArray("elements");
        if (elements != null) {
            for (JsonElement element : elements) {
                JsonObject o = element.getAsJsonObject();
                if (!"cube".equals(BBModel.text(o, "type", "cube"))) continue;
                Cube cube = BBModel.parseCube(o);
                cubeMap.put(cube.uuid(), cube);
                cubes.add(cube);
            }
        }
        ArrayList<Bone> roots = new ArrayList<Bone>();
        JsonArray outliner = root.getAsJsonArray("outliner");
        if (outliner != null) {
            for (JsonElement element : outliner) {
                if (!element.isJsonObject()) continue;
                roots.add(BBModel.parseBone(element.getAsJsonObject(), cubeMap));
            }
        }
        ArrayList<String> referenced = new ArrayList<String>();
        for (Bone b : roots) {
            BBModel.collectReferenced(b, referenced);
        }
        ArrayList<Cube> loose = new ArrayList<Cube>();
        for (Cube cube : cubes) {
            if (referenced.contains(cube.uuid())) continue;
            loose.add(cube);
        }
        if (!loose.isEmpty()) {
            roots.add(new Bone("loose", "loose", Vec.ZERO, Vec.ZERO, loose, List.of(), true));
        }
        ArrayList<BBAnimation> animations = new ArrayList<BBAnimation>();
        JsonArray animationEntries = root.getAsJsonArray("animations");
        if (animationEntries != null) {
            for (JsonElement element : animationEntries) {
                animations.add(BBAnimation.parse(element.getAsJsonObject()));
            }
        }
        return new BBModel(modelId, name, resWidth, resHeight, textures, cubes, roots, animations);
    }

    private static void collectReferenced(Bone bone, List<String> out) {
        for (Cube cube : bone.cubes()) {
            out.add(cube.uuid());
        }
        for (Bone child : bone.children()) {
            BBModel.collectReferenced(child, out);
        }
    }

    private static Cube parseCube(JsonObject o) {
        Vec from = BBModel.vector(o.getAsJsonArray("from"));
        Vec to = BBModel.vector(o.getAsJsonArray("to"));
        Vec origin = o.has("origin") ? BBModel.vector(o.getAsJsonArray("origin")) : Vec.ZERO;
        Vec rotation = o.has("rotation") ? BBModel.negXY(BBModel.vector(o.getAsJsonArray("rotation"))) : Vec.ZERO;
        ArrayList<Face> faces = new ArrayList<Face>();
        JsonObject faceObject = o.getAsJsonObject("faces");
        if (faceObject != null) {
            for (Map.Entry entry : faceObject.entrySet()) {
                JsonArray uv;
                JsonObject face = ((JsonElement)entry.getValue()).getAsJsonObject();
                if (!face.has("texture") || face.get("texture").isJsonNull() || (uv = face.getAsJsonArray("uv")) == null || uv.size() < 4) continue;
                faces.add(new Face((String)entry.getKey(), uv.get(0).getAsFloat(), uv.get(1).getAsFloat(), uv.get(2).getAsFloat(), uv.get(3).getAsFloat(), (int)BBModel.number(face, "rotation", 0.0f), face.get("texture").getAsInt()));
            }
        }
        return new Cube(BBModel.text(o, "uuid", ""), BBModel.text(o, "name", ""), from, to, origin, rotation, faces);
    }

    private static Bone parseBone(JsonObject source, Map<String, Cube> cubeMap) {
        ArrayList<Cube> ownCubes = new ArrayList<Cube>();
        ArrayList<Bone> children = new ArrayList<Bone>();
        String name = BBModel.text(source, "name", "bone");
        boolean visible = !source.has("visibility") || source.get("visibility").getAsBoolean();
        JsonArray childElements = source.getAsJsonArray("children");
        if (childElements != null) {
            for (JsonElement child : childElements) {
                if (child.isJsonPrimitive()) {
                    Cube cube = cubeMap.get(child.getAsString());
                    if (cube == null) continue;
                    ownCubes.add(cube);
                    continue;
                }
                if (!child.isJsonObject()) continue;
                children.add(BBModel.parseBone(child.getAsJsonObject(), cubeMap));
            }
        }
        return new Bone(BBModel.text(source, "uuid", ""), name, BBModel.vector(source.getAsJsonArray("origin")), source.has("rotation") ? BBModel.negXY(BBModel.vector(source.getAsJsonArray("rotation"))) : Vec.ZERO, ownCubes, children, visible);
    }

    private static Vec negXY(Vec v) {
        return new Vec(-v.x(), -v.y(), v.z());
    }

    private static Vec vector(JsonArray array) {
        if (array == null || array.size() < 3) {
            return Vec.ZERO;
        }
        return new Vec(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private static float number(JsonObject o, String key, float fallback) {
        if (o == null || !o.has(key)) {
            return fallback;
        }
        try {
            return o.get(key).getAsFloat();
        }
        catch (RuntimeException ignored) {
            return fallback;
        }
    }

    private static String text(JsonObject o, String key, String fallback) {
        return o != null && o.has(key) ? o.get(key).getAsString() : fallback;
    }

    @Environment(value=EnvType.CLIENT)
    public record Texture(class_2960 id, String path, float width, float height, float uvWidth, float uvHeight) {
    }

    @Environment(value=EnvType.CLIENT)
    public record Cube(String uuid, String name, Vec from, Vec to, Vec origin, Vec rotation, List<Face> faces) {
        public float width() {
            return Math.abs(this.to.x - this.from.x);
        }

        public float height() {
            return Math.abs(this.to.y - this.from.y);
        }

        public float depth() {
            return Math.abs(this.to.z - this.from.z);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Bone(String uuid, String name, Vec origin, Vec rotation, List<Cube> cubes, List<Bone> children, boolean visible) {
    }

    @Environment(value=EnvType.CLIENT)
    public record Vec(float x, float y, float z) {
        public static final Vec ZERO = new Vec(0.0f, 0.0f, 0.0f);
        public static final Vec ONE = new Vec(1.0f, 1.0f, 1.0f);

        public Vec add(Vec other) {
            return new Vec(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        public Vec sub(Vec other) {
            return new Vec(this.x - other.x, this.y - other.y, this.z - other.z);
        }

        public static Vec lerp(Vec a, Vec b, float t) {
            return new Vec(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t, a.z + (b.z - a.z) * t);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record Face(String direction, float u1, float v1, float u2, float v2, int rotation, int texture) {
    }

    @Environment(value=EnvType.CLIENT)
    public record Key(float time, Vec value, String interpolation) {
    }
}

