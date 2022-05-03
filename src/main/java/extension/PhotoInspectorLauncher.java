package extension;

import gearth.extensions.ThemedExtensionFormCreator;

import java.net.URL;

public class PhotoInspectorLauncher extends ThemedExtensionFormCreator {

    public static void main(String[] args) {
        runExtensionForm(args, PhotoInspectorLauncher.class);
    }

    @Override
    protected String getTitle() {
        return "Sanbovir Photo Inspector";
    }

    @Override
    protected URL getFormResource() {
        return getClass().getResource("photoinspector.fxml");
    }
}
