package extension;

import com.google.gson.*;
import gearth.extensions.ExtensionForm;
import gearth.extensions.ExtensionInfo;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.ui.scheduler.InteractableScheduleItem;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import photo.HPhoto;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@ExtensionInfo(
        Title =  "Project Sanbovir",
        Description =  "Habbo Photo Inspector",
        Version =  "2.0",
        Author =  "sirjonasxx"
)
public class PhotoInspector extends ExtensionForm {

    public CheckBox cbEditMode;
    public RadioButton rdCapture;
    public RadioButton rdInject;

    public Pane editControls;

    public TextArea areaPlanes;
    public TextArea areaSprites;
    public TextArea areaFilters;

    public Label errorLbl;


    public void initialize() {
        cbEditMode.selectedProperty().addListener((ob, o, n) -> editControls.setDisable(!n));

        editControls.setDisable(!cbEditMode.isSelected());
    }

    @Override
    protected void initExtension() {
        intercept(HMessage.Direction.TOSERVER, "RenderRoom", this::onCameraRender);
    }

    private void onCameraRender(HMessage hMessage) {
        try {
            HPacket packet = hMessage.getPacket();
            int length = packet.readInteger();
            byte[] data = packet.readBytes(length);
            byte[] decompressed = decompress(data);


            String photoJson = new String(decompressed, StandardCharsets.UTF_8);

            HPhoto photo = new HPhoto(photoJson);
            onNewPhotoTaken(photo);
            maybeModifyPhoto(photo);

            String newPhoto = photo.getJsonData();

            byte[] compressed = compress(newPhoto.getBytes(StandardCharsets.UTF_8));
            packet.setBytes(packet.readBytes(6, 0));
            packet.appendInt(compressed.length);
            packet.appendBytes(compressed);

        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
    }

    private void onNewPhotoTaken(HPhoto photo) {
        if (rdCapture.isSelected()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            areaPlanes.setText(gson.toJson(photo.getPlanes()));
            areaSprites.setText(gson.toJson(photo.getSprites()));
            areaFilters.setText(gson.toJson(photo.getFilters()));
        }
    }

    private void maybeModifyPhoto(HPhoto photo) {
        if (rdInject.isSelected() && cbEditMode.isSelected()) {
            Gson gson = new Gson();

            try {
                JsonArray planes = gson.fromJson(areaPlanes.getText(), JsonArray.class);
                JsonArray sprites = gson.fromJson(areaSprites.getText(), JsonArray.class);
                JsonArray filters = gson.fromJson(areaFilters.getText(), JsonArray.class);

                photo.setPlanes(planes);
                photo.setSprites(sprites);
                photo.setFilters(filters);

                Platform.runLater(() -> {
                    errorLbl.setText("");
                });
            }
            catch (Exception e) {
                Platform.runLater(() -> {
                    errorLbl.setText("Error while parsing JSON");
                });
            }

        }
    }

    private static byte[] decompress(byte[] data) throws IOException, DataFormatException {
        Inflater inflater = new Inflater();
        inflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (inflater.getRemaining() > 0) {
            int count = inflater.inflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();

        return outputStream.toByteArray();
    }

    private static byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public void randomizeColorsClick(ActionEvent actionEvent) {
        Random rand = new Random();
        Gson gson = new Gson();

        try {
            JsonArray planes = gson.fromJson(areaPlanes.getText(), JsonArray.class);
            JsonArray sprites = gson.fromJson(areaSprites.getText(), JsonArray.class);

            for (JsonElement object : planes) {
                JsonObject plane = object.getAsJsonObject();
                if (plane.has("color")) {
                    plane.addProperty("color", rand.nextInt(256 * 256 * 256));
                }
            }

            for (JsonElement object : sprites) {
                JsonObject sprite = object.getAsJsonObject();
                if (sprite.has("color")) {
                    sprite.addProperty("color", rand.nextInt(256 * 256 * 256));
                }
            }

            gson = new GsonBuilder().setPrettyPrinting().create();
            areaPlanes.setText(gson.toJson(planes));
            areaSprites.setText(gson.toJson(sprites));

            Platform.runLater(() -> {
                errorLbl.setText("");
            });
        }
        catch (Exception e) {
            Platform.runLater(() -> {
                errorLbl.setText("Error while parsing JSON");
            });
        }

    }

    public void RandomizeLocationsClick(ActionEvent actionEvent) {
        Random rand = new Random();
        Gson gson = new Gson();

        try {
            JsonArray sprites = gson.fromJson(areaSprites.getText(), JsonArray.class);

            for (JsonElement object : sprites) {
                JsonObject sprite = object.getAsJsonObject();

                if (sprite.has("x"))
                    sprite.addProperty("x", rand.nextInt(320) - 10);

                if (sprite.has("y"))
                    sprite.addProperty("y", rand.nextInt(320) - 10);
            }

            gson = new GsonBuilder().setPrettyPrinting().create();
            areaSprites.setText(gson.toJson(sprites));

            Platform.runLater(() -> {
                errorLbl.setText("");
            });
        }
        catch (Exception e) {
            Platform.runLater(() -> {
                errorLbl.setText("Error while parsing JSON");
            });
        }
    }

    public void IntensifyFilterClick(ActionEvent actionEvent) {
        Random rand = new Random();
        Gson gson = new Gson();

        try {
            JsonArray filters = gson.fromJson(areaFilters.getText(), JsonArray.class);

            for (JsonElement object : filters) {
                JsonObject filter = object.getAsJsonObject();

                if (filter.has("alpha"))
                    filter.addProperty("alpha", rand.nextInt(2500) + 500);

            }

            gson = new GsonBuilder().setPrettyPrinting().create();
            areaFilters.setText(gson.toJson(filters));

            Platform.runLater(() -> {
                errorLbl.setText("");
            });
        }
        catch (Exception e) {
            Platform.runLater(() -> {
                errorLbl.setText("Error while parsing JSON");
            });
        }
    }

    public void saveClick(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        //Set photo filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setTitle("Save Photo File");

        //Show save file dialog
        File file = fileChooser.showSaveDialog(primaryStage);

        if(file != null){
            try {
                Gson gson = new Gson();

                JsonArray planes = gson.fromJson(areaPlanes.getText(), JsonArray.class);
                JsonArray sprites = gson.fromJson(areaSprites.getText(), JsonArray.class);
                JsonArray filters = gson.fromJson(areaFilters.getText(), JsonArray.class);

                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fileWriter);

                JsonObject object = new JsonObject();
                object.add("planes", planes);
                object.add("sprites", sprites);
                object.add("filters", filters);

                gson = new GsonBuilder().setPrettyPrinting().create();
                String fileContents = gson.toJson(object);
                out.write(fileContents);

                out.flush();
                out.close();
                fileWriter.close();

                Platform.runLater(() -> {
                    errorLbl.setText("");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    errorLbl.setText("Error (probably invalid JSON)");
                });
                ex.printStackTrace();
            }

        }
    }

    public void loadClick(ActionEvent actionEvent) {
        List<InteractableScheduleItem> list = new ArrayList<>();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Photo File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Photo Files", "*.json"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {

            FileReader fr;
            try {
                fr = new FileReader(selectedFile);
                BufferedReader br = new BufferedReader(fr);

                Gson gson = new Gson();
                JsonObject object = gson.fromJson(br, JsonObject.class);

                gson = new GsonBuilder().setPrettyPrinting().create();

                String planes = gson.toJson(object.getAsJsonArray("planes"));
                String sprites = gson.toJson(object.getAsJsonArray("sprites"));
                String filters = gson.toJson(object.getAsJsonArray("filters"));

                areaPlanes.setText(planes);
                areaSprites.setText(sprites);
                areaFilters.setText(filters);

                fr.close();
                br.close();

                Platform.runLater(() -> {
                    errorLbl.setText("");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    errorLbl.setText("Error (probably invalid JSON)");
                });
                e.printStackTrace();
            }
        }
    }
}
