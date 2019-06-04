package xu.kevin.pictowar.PictoDB;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "face_table")
public class FaceInfo {
    private String userName;
    private UUID userFace;
    private String imageFilePath;

    @PrimaryKey(autoGenerate = true)
    private int Id;


    public FaceInfo(String userName, UUID userFace, String imageFilePath){
        this.userName = userName;
        this.userFace = userFace;
        this.imageFilePath = imageFilePath;
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

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }
}
