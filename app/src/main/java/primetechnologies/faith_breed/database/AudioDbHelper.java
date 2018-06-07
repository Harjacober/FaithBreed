package primetechnologies.faith_breed.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AudioDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="audioLibrary.db";
    private static final int DATABASE_VERSION=1;

    public AudioDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_CFI_TABLE = "CREATE TABLE "+ AudioContract.AudioEntry.TABLE_NAME+"("
                + AudioContract.AudioEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                + AudioContract.AudioEntry.COLUMN_NAME+" TEXT NOT NULL, "
                + AudioContract.AudioEntry.COLUMN_IMAGE_LINK+" TEXT NOT NULL, "
                + AudioContract.AudioEntry.COLUMN_ARTIST+" TEXT NOT NULL, "
                + AudioContract.AudioEntry.COLUMN_DOWNLOAD_LINK+" TEXT NOT NULL);";
        sqLiteDatabase.execSQL(String.valueOf(CREATE_CFI_TABLE));

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String DROP_TABLE = "DROP IF EXIST "+ AudioContract.AudioEntry.TABLE_NAME+";";
        sqLiteDatabase.execSQL(String.valueOf(DROP_TABLE));
        onCreate(sqLiteDatabase);
    }
}
