package xu.kevin.pictowar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private Button selectFace;
    private ImageView confirmedFace;
    private ListView faceList;


    private ImageView confirmedFace1;


    private Button verifyButton;


    //Faces
    private UUID mFaceId;


    //Images
    private Bitmap mBitmap;


    // Flag to indicate which level's image is being selected
    private static final int REQUEST_SELECT_IMAGE_0 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        selectFace = findViewById(R.id.selectFace);
        confirmedFace = findViewById(R.id.confirmedFace);
        faceList = findViewById(R.id.faceList);


        confirmedFace1 = findViewById(R.id.confirmedFace1);


        verifyButton = findViewById(R.id.verifyButton);

        selectFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    BitmapDrawable bmpd= (BitmapDrawable) confirmedFace.getDrawable();
                    confirmedFace1.setImageBitmap(bmpd.getBitmap());

                    //With further implementation of database, upon set face click, the selected face will be
                    //Saved within DB as the official face of the user
            }
        });


    }

    // Select the image indicated by index.
    private void selectImage() {
        Intent intent = new Intent(this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE_0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK){
            Bitmap bmp = ImageHelper.loadSizeLimitedBitmapFromUri(data.getData(),getContentResolver());

            if(bmp!=null){


                    mBitmap = bmp;
                    //confirmedFace.setImageBitmap(bmp);
                    mFaceId = null;
                }

            detect(bmp);

        }

    }


    private void detect(Bitmap bmp){
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        try {
            Face[] allFaces = new DetectionTask().execute(inputStream).get();
            FaceListAdapter fld = new FaceListAdapter(allFaces);
            if(fld.faces.size()!=0){

                mFaceId = fld.faces.get(0).faceId;

                confirmedFace.setImageBitmap(fld.faceThumbnails.get(0));

            }

            faceList.setAdapter(fld);

            faceList.setVisibility(View.VISIBLE);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }






    /*
    private class VerificationTask extends AsyncTask<Void, Void, VerifyResult> {
        // The IDs of two face to verify.
        private UUID mFaceId;
        private UUID mFaceId1;

        VerificationTask(UUID faceId, UUID faceId1) {
            mFaceId = faceId;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {

            FaceServiceClient faceServiceClient = PictoFaceClient.getFaceServiceClient();
            try {


                // Start verification.
                return faceServiceClient.verify(
                        mFaceId,      //The first face ID to verify
                        mFaceId1);     // The second face ID to verify
            } catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            if (result != null) {


            }


        }
    }
    */

    private class DetectionTask extends AsyncTask<InputStream, Void, Face[]> {
        // Index indicates detecting in which of the two images.

        private boolean mSucceed = true;



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
                        , FaceServiceClient.FaceAttributeType.Gender, FaceServiceClient.FaceAttributeType.Smile});
            }  catch (Exception e) {
                mSucceed = false;
                return null;
            }
        }
    }

    private class FaceListAdapter extends BaseAdapter {
        // The detected faces.
        List<Face> faces;



        // The thumbnails of detected faces.
        List<Bitmap> faceThumbnails;

        // Initialize with detection result and index indicating on which image the result is got.
        FaceListAdapter(Face[] detectionResult) {
            faces = new ArrayList<>();
            faceThumbnails = new ArrayList<>();


            if (detectionResult != null) {
                faces = Arrays.asList(detectionResult);
                for (Face face: faces) {
                    try {
                        // Crop face thumbnail without landmarks drawn.
                        faceThumbnails.add(ImageHelper.generateFaceThumbnail(
                                mBitmap,face.faceRectangle));
                    } catch (IOException e) {
                        // Show the exception when generating face thumbnail fails.
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public int getCount() {
            return faces.size();
        }

        @Override
        public Object getItem(int position) {
            return faces.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater layoutInflater =
                        (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.item_face, parent, false);
            }
            convertView.setId(position);

            Bitmap thumbnailToShow = faceThumbnails.get(position);
            if (faces.get(position).faceId.equals(mFaceId)) {
                thumbnailToShow = ImageHelper.highlightSelectedFaceThumbnail(thumbnailToShow);
            }

            // Show the face thumbnail.
            ((ImageView)convertView.findViewById(R.id.image_face)).setImageBitmap(thumbnailToShow);

            return convertView;
        }
    }
}
