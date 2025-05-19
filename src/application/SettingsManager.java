package application;

import javafx.beans.property.*;
import java.util.Locale;

public class SettingsManager {
    private static final SettingsManager instance = new SettingsManager();

    private final StringProperty theme = new SimpleStringProperty("Default");
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<>(Locale.ENGLISH);

    private SettingsManager() {}

    public static SettingsManager getInstance() {
        return instance;
    }

    public StringProperty themeProperty() { return theme; }
    public ObjectProperty<Locale> localeProperty() { return locale; }

    public String getTheme() { return theme.get(); }
    public void setTheme(String value) { theme.set(value); }

    public Locale getLocale() { return locale.get(); }
    public void setLocale(Locale value) { locale.set(value); }
}