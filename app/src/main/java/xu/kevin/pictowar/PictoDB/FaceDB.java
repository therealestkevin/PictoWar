package xu.kevin.pictowar.PictoDB;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;


@Database(entities = {FaceInfo.class},version=1,exportSchema = false)
@TypeConverters({FaceDAO.Converters.class})
public abstract class FaceDB extends RoomDatabase {

    public abstract FaceDAO faceDAO();

    private static volatile FaceDB INSTANCE;

    static FaceDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FaceDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            FaceDB.class, "face_DB")
                            .fallbackToDestructiveMigration()
                            //.addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static RoomDatabase.Callback sRoomDatabaseCallback =
            new RoomDatabase.Callback(){

                @Override
                public void onOpen (@NonNull SupportSQLiteDatabase db){
                    super.onOpen(db);
                    new PopulateDbAsync(INSTANCE).execute();
                }
            };


    //Clears the DB for test purposes
    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final FaceDAO mDao;

        PopulateDbAsync(FaceDB db) {
            mDao = db.faceDAO();
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDao.deleteAll();


            return null;
        }
    }
}
