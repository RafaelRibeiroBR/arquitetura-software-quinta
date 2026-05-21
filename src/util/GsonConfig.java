package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Configuração centralizada do Gson com adaptadores personalizados.
 */
public class GsonConfig {

    private static Gson gson;

    /**
     * Retorna instância singleton do Gson configurado.
     */
    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();
        }
        return gson;
    }
}