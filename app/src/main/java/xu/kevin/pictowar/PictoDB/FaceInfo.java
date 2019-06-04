package xu.kevin.pictowar.PictoDB;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "face_table")
public class FaceInfo {
    private String userName;
    private UUID userFace;

    @PrimaryKey(autoGenerate = true)
    private int Id;


    public FaceInfo(String userName, UUID userFace){
        this.userName = userName;
        this.userFace = userFace;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UUID getUserFace() {
        return userFace;
    }

    public void setUserFace(UUID userFace) {
        this.userFace = userFace;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }
}
