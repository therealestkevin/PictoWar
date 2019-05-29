package xu.kevin.pictowar;

import android.app.Application;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

public class PictoFaceClient extends Application {
    private static FaceServiceClient PictoClient;

    @Override
    public void onCreate() {
        super.onCreate();
        PictoClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
    }

    public static FaceServiceClient getFaceServiceClient() {
        return PictoClient;
    }


}
