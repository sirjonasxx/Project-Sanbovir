package photo;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;

// modified version of Sulakore's implementation
public class HPhoto {

    private JsonArray planes;
    private JsonArray sprites;
    private JsonObject modifiers;
    private JsonArray filters;

    private int roomId;
    private float zoom;

    public HPhoto(String jsonData) {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(jsonData, JsonObject.class);
        planes = obj.getAsJsonArray("planes");
        sprites = obj.getAsJsonArray("sprites");
        modifiers = obj.getAsJsonObject("modifiers");
        filters = obj.getAsJsonArray("filters");

        roomId = obj.get("roomid").getAsInt();
        zoom = !obj.has("zoom") ? -1 : obj.get("zoom").getAsInt();
    }

    private long getStatus(long timestamp) {
        return timestamp / 100 % 23; // + security level, canceled
    }
    private long getChecksum(long mod, long key) {
        return (mod + 13) * (key + 29);
    }
    private long getTimestamp(String blob, long timestamp, long key) {
        byte[] data = blob.getBytes(StandardCharsets.UTF_8);
        return timestamp + calculate(data, key, roomId);
    }
    private long calculate(byte[] data, long key, int roomId) {
        long tKey = key, tRoomId = roomId;
        for (byte d : data) {
            tKey = (tKey + ((((int) d) + 256) % 256)) % 255;
            tRoomId = (tKey + tRoomId) % 255;
        }
        return (tKey + tRoomId) % 100;
    }

    public String getJsonData() {
        JsonObject obj = new JsonObject();

        obj.add("planes", planes);
        obj.add("sprites", sprites);
        obj.add("modifiers", modifiers);
        obj.add("filters", filters);

        obj.addProperty("roomid", roomId);
        if (zoom != -1) obj.addProperty("zoom", zoom);

        String json = obj.toString();
        json = json.substring(0, json.length() - 1);
        json = json
                .replace("\"planes\":", "\"planes\" : ")
                .replace("\"sprites\":", "\"sprites\" : ")
                .replace("\"modifiers\":", "\"modifiers\" : ")
                .replace("\"filters\":", "\"filters\" : ")
                .replace("\"roomid\":", "\"roomid\" : ")
                .replace("\"zoom\":", "\"zoom\" : ");
        json = "{ " + json.substring(1);

        long timestamp = System.currentTimeMillis();
        long mod = timestamp % 100;
        timestamp -= mod;
        json += ",\"status\" : " + getStatus(timestamp);

        long key = (json.length() + timestamp / 100 * 17) % 1493;

        json += ",\"timestamp\" : " + getTimestamp(json, timestamp, key);
        json += ",\"checksum\" : " + getChecksum(mod, key) + " }";
        return json;
    }

    public JsonArray getPlanes() {
        return planes;
    }

    public void setPlanes(JsonArray planes) {
        this.planes = planes;
    }

    public JsonArray getSprites() {
        return sprites;
    }

    public void setSprites(JsonArray sprites) {
        this.sprites = sprites;
    }

    public JsonObject getModifiers() {
        return modifiers;
    }

    public void setModifiers(JsonObject modifiers) {
        this.modifiers = modifiers;
    }

    public JsonArray getFilters() {
        return filters;
    }

    public void setFilters(JsonArray filters) {
        this.filters = filters;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }
}
