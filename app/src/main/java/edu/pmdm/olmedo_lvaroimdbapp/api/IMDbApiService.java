package edu.pmdm.olmedo_lvaroimdbapp.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class IMDbApiService {

    private static final String API_KEY = "8099f3a016msh8e0cc7d1ce24bf7p1d06a3jsn80d118e4d8b4";
    private static final String HOST = "imdb-com.p.rapidapi.com";

    /**
     * Realiza una petición al endpoint /title/get-overview
     */
    public String getMovieOverview(String tconst) throws IOException {
        String url = "https://" + HOST + "/title/get-overview?tconst=" + tconst;
        return makeRequest(url);
    }

    /**
     * Realiza una petición al endpoint /title/get-top-meter
     */
    public String getTopMeterMovies() throws IOException {
        String url = "https://" + HOST + "/title/get-top-meter?topMeterTitlesType=ALL&limit=10";
        return makeRequest(url);
    }

    /**
     * Método para realizar peticiones HTTP GET a la API de
     * IMDb ubicada dentro de RapidAPI. Usamos la key de nuestra API
     * y el link del host, el cual como hemos dicho es RapidAPI, que
     * hemos declarado como variables al inicio del código.
     *
     * Después de pedir a la API lo que necesitemos, lo leeremos con
     * un BufferedReader y la parasermos a un String leyendo línea
     * por línea.
     */
    private String makeRequest(String urlString) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            //Configura la conexión
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-rapidapi-key", API_KEY);
            connection.setRequestProperty("x-rapidapi-host", HOST);
            //Verifica código de respuesta
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Error: Código de estado " + responseCode);
            }
            //Lee la respuesta
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            //Cerramos el bufferedReader
            if (reader != null) {
                reader.close();
            }
            //Cerramos la conexión
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}