package xu.kevin.pictowar;

import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button selectFace;
    private ImageView confirmedFace;
    private ListView faceList;

    private Button selectFace1;
    private ImageView confirmedFace1;
    private ListView faceList1;

    private Button verifyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectFace = findViewById(R.id.selectFace);
        confirmedFace = findViewById(R.id.confirmedFace);
        faceList = findViewById(R.id.faceList);

        selectFace1 = findViewById(R.id.selectFace1);
        confirmedFace1 = findViewById(R.id.confirmedFace1);
        faceList1 = findViewById(R.id.faceList1);

        verifyButton = findViewById(R.id.verifyButton);

        selectFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        selectFace1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }



    private class VerificationTask extends AsyncTask<Void, Void, VerifyResult> {
        // The IDs of two face to verify.
        private UUID mFaceId0;
        private UUID mFaceId1;

        VerificationTask (UUID faceId0, UUID faceId1) {
            mFaceId0 = faceId0;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            
            FaceServiceClient faceServiceClient = PictoFaceClient.getFaceServiceClient();
            try{


                // Start verification.
                return faceServiceClient.verify(
                        mFaceId0,      /* The first face ID to verify */
                        mFaceId1);     /* The second face ID to verify */
            }  catch (Exception e) {

                return null;
            }
        }



    }

    private class DetectionTask extends AsyncTask<InputStream, Void, Face[]> {
        // Index indicates detecting in which of the two images.
        private int mIndex;
        private boolean mSucceed = true;

        DetectionTask(int index) {
            mIndex = index;
        }

        @Override
        protected Face[] doInBackground(InputStream... params) {

            FaceServiceClient faceServiceClient = PictoFaceClient.getFaceServiceClient();
            try{


                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       // return face landmarks
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        new FaceServiceClient.FaceAttributeType[]{FaceServiceClient.FaceAttributeType.Age, FaceServiceClient.FaceAttributeType.Emotion
                        , FaceServiceClient.FaceAttributeType.Gender});
            }  catch (Exception e) {
                mSucceed = false;
                return null;
            }
        }
    }
}
