package primetechnologies.faith_breed.database;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class AudioContract {
    public static final String CONTENT_AUTHORITY="primetechnologies.faith_breed";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    private AudioContract(){}

    public static final class AudioEntry implements BaseColumns {
        public final static String _ID= BaseColumns._ID;
        public static final String TABLE_NAME="audioList";
        public static final String COLUMN_NAME="audioName";
        public static final String COLUMN_ARTIST="audioArtist";
        public static final String COLUMN_IMAGE_LINK="audioImageLink";
        public static final String COLUMN_DOWNLOAD_LINK="downloadLink";
        public static final Uri CONTENT_URI=BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();
        public static Uri buildProductUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI,id);
        }

    }
}
