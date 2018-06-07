package primetechnologies.faith_breed.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

public class AudioProvider extends ContentProvider {
    AudioDbHelper mAudioDbHelper;
    private static final int AUDIO =100;
    private static final int AUDIO_WITH_ID =101;
    private static final UriMatcher surimatcher=buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AudioContract.CONTENT_AUTHORITY, AudioContract.AudioEntry.TABLE_NAME, AUDIO);
        matcher.addURI(AudioContract.CONTENT_AUTHORITY, AudioContract.AudioEntry.TABLE_NAME+ "/#", AUDIO_WITH_ID);
        return matcher;
    }


    @Override
    public boolean onCreate() {
        mAudioDbHelper =new AudioDbHelper(getContext());

        AudioContract.BASE_CONTENT_URI.buildUpon().appendPath(AudioContract.AudioEntry.TABLE_NAME).build();

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortorder) {
        SQLiteDatabase sqLiteDatabase = mAudioDbHelper.getReadableDatabase();
        int match = surimatcher.match(uri);
        Cursor cursor;
        switch (match) {
            case AUDIO:
                cursor= sqLiteDatabase.query(AudioContract.AudioEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortorder);
                break;
            case AUDIO_WITH_ID:
                selection= AudioContract.AudioEntry._ID+"=?";
                selectionArgs=new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor= sqLiteDatabase.query(AudioContract.AudioEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortorder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final SQLiteDatabase sqLiteDatabase= mAudioDbHelper.getWritableDatabase();
        Uri returnUri;
        int match=surimatcher.match(uri);
        switch (match){
            case AUDIO: {
                long id = sqLiteDatabase.insert(AudioContract.AudioEntry.TABLE_NAME,

                        null,
                        contentValues);
                if (id > 0) {
                    returnUri = AudioContract.AudioEntry.buildProductUri(id);
                } else {
                    throw new SQLException("failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown Uri " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase sqLiteDatabase= mAudioDbHelper.getWritableDatabase();
        int numdeleted;
        int match=surimatcher.match(uri);
        switch (match){
            case AUDIO:
                numdeleted=sqLiteDatabase.delete(AudioContract.AudioEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"
                        + AudioContract.AudioEntry.TABLE_NAME+"'");
                break;
            case AUDIO_WITH_ID:
                numdeleted=sqLiteDatabase.delete(AudioContract.AudioEntry.TABLE_NAME,
                        AudioContract.AudioEntry._ID+"=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '"
                        + AudioContract.AudioEntry.TABLE_NAME+"'");
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return numdeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase= mAudioDbHelper.getWritableDatabase();
        int numupdated=0;
        if (contentValues==null){
            throw new IllegalArgumentException("cannot have null content values");
        }
        int match=surimatcher.match(uri);
        switch (match){
            case AUDIO:
                numupdated=sqLiteDatabase.update(AudioContract.AudioEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            case AUDIO_WITH_ID:
                numupdated=sqLiteDatabase.update(AudioContract.AudioEntry.TABLE_NAME,
                        contentValues,
                        AudioContract.AudioEntry._ID+"=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri "+uri);
        }
        if (numupdated>0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numupdated;
    }


}
