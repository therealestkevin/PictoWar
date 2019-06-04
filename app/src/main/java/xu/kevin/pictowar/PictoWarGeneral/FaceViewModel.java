package xu.kevin.pictowar.PictoWarGeneral;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import xu.kevin.pictowar.PictoDB.FaceInfo;
import xu.kevin.pictowar.PictoDB.FaceRepository;

public class FaceViewModel extends AndroidViewModel {

    private FaceRepository faceRepo;

    public FaceViewModel(@NonNull Application application) {
        super(application);
        faceRepo = new FaceRepository(application);

    }

    public void insertFaceInfo(FaceInfo faceInfo){
        faceRepo.insertFaceInfo(faceInfo);
    }

    public FaceInfo getFaceInfo(){
        return faceRepo.getFaceInfo();
    }
}
