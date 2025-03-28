package net.ixdarklord.ultimine_addition.datagen.model;

import com.google.gson.*;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class ItemTransformDeserializer implements JsonDeserializer<ItemTransform> {
    public static final Vector3f DEFAULT_ROTATION = new Vector3f(0.0F, 0.0F, 0.0F);
    public static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0.0F, 0.0F, 0.0F);
    public static final Vector3f DEFAULT_SCALE = new Vector3f(1.0F, 1.0F, 1.0F);
    public static final float MAX_TRANSLATION = 5.0F;
    public static final float MAX_SCALE = 4.0F;

    public ItemTransformDeserializer() {
    }

    public ItemTransform deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonobject = json.getAsJsonObject();
        Vector3f vector3f = this.getVector3f(jsonobject, "rotation", DEFAULT_ROTATION);
        Vector3f vector3f1 = this.getVector3f(jsonobject, "translation", DEFAULT_TRANSLATION);
        vector3f1.mul(0.0625F);
        vector3f1.set(Mth.clamp(vector3f1.x, -5.0F, 5.0F), Mth.clamp(vector3f1.y, -5.0F, 5.0F), Mth.clamp(vector3f1.z, -5.0F, 5.0F));
        Vector3f vector3f2 = this.getVector3f(jsonobject, "scale", DEFAULT_SCALE);
        vector3f2.set(Mth.clamp(vector3f2.x, -4.0F, 4.0F), Mth.clamp(vector3f2.y, -4.0F, 4.0F), Mth.clamp(vector3f2.z, -4.0F, 4.0F));
        return new ItemTransform(vector3f, vector3f1, vector3f2);
    }

    private Vector3f getVector3f(JsonObject json, String key, Vector3f fallback) {
        if (!json.has(key)) {
            return fallback;
        } else {
            JsonArray jsonarray = GsonHelper.getAsJsonArray(json, key);
            if (jsonarray.size() != 3) {
                throw new JsonParseException("Expected 3 " + key + " values, found: " + jsonarray.size());
            } else {
                float[] afloat = new float[3];

                for(int i = 0; i < afloat.length; ++i) {
                    afloat[i] = GsonHelper.convertToFloat(jsonarray.get(i), key + "[" + i + "]");
                }

                return new Vector3f(afloat[0], afloat[1], afloat[2]);
            }
        }
    }
}
