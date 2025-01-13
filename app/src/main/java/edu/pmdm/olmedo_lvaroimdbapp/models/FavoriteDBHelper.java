package edu.pmdm.olmedo_lvaroimdbapp.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDBHelper extends SQLiteOpenHelper {

    //Nombre y versión de la base de datos
    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    //Nombre de la tabla y el tipo de datos de sus columnas
    private static final String TABLE_NAME = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_MOVIE_ID = "movie_id";
    private static final String COLUMN_IMAGE_URL = "image_url";
    private static final String COLUMN_TITLE = "title";

    //Consulta para crear la tabla
    private static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_USER_ID + " TEXT NOT NULL, " +
            COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
            COLUMN_IMAGE_URL + " TEXT NOT NULL, " +
            COLUMN_TITLE + " TEXT NOT NULL);";

    public FavoriteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    //Método para insertar en la tabla los datos de la película
    public boolean insertFavorite(String userId, String movieId, String imageUrl, String title) {
        if (isFavorite(userId, movieId)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_MOVIE_ID, movieId);
        values.put(COLUMN_IMAGE_URL, imageUrl);
        values.put(COLUMN_TITLE, title);

        long result = db.insert(TABLE_NAME, null, values);
        return result!=-1; //Devuelve 'true' si el insert se realizó de manera correcta
    }

    //Método para obtener los datos de todas las películas favoritas, que se usará para compartir el JSON de las películas favoritas
    public List<Movie> getAllMovies(String userId) {
        List<Movie> movies = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //Consulta para obtener solo las películas del usuario actual
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{userId});

        if (cursor.moveToFirst()) {
            do {
                String movieId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOVIE_ID));
                String imageUrl = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));

                //Crea un objeto Movie con los datos que hay en la base de datos
                Movie movie = new Movie(movieId, title, imageUrl);

                //Se añaden a la lista
                movies.add(movie);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return movies;
    }

    //Método para borrar la película de favoritos
    public boolean deleteFavorite(String userId, String movieId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_USER_ID + "=? AND " + COLUMN_MOVIE_ID + "=?",
                new String[]{userId, movieId});
        return result > 0; //Devuelve 'true' si el delete se realizó de manera correcta
    }

    //Comprueba que ya esté una película en la lista de favoritos
    public boolean isFavorite(String userId, String movieId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_USER_ID + "=? AND " + COLUMN_MOVIE_ID + "=?",
                new String[]{userId, movieId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}