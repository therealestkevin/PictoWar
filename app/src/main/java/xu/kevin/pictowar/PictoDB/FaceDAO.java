package xu.kevin.pictowar.PictoDB;


import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface FaceDAO {

    @Insert(onConflict = REPLACE)
    void insertFace(FaceInfo face);

    @Query("DELETE FROM face_table")
    void deleteAll();

    @Query("SELECT * FROM face_table")
    List<FaceInfo> getFaceInfo();

    class Converters{
        @TypeConverter
        public static UUID toUUID(String value){
            if(value == null){
                return null;
            }
            Gson gson = new Gson();
            Type listType = new TypeToken<UUID>() {}.getType();
            return new Gson().fromJson(value, listType);
        }

        @TypeConverter
        public static String fromUUID(UUID face){
            if(face == null){
                return null;
            }
            Gson gson = new Gson();
            Type typeString = new TypeToken<UUID>(){}.getType();
            return gson.toJson(face, typeString);

        }
    }

}
