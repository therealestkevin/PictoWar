package xu.kevin.pictowar.PictoWarGeneral;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import xu.kevin.pictowar.PictoDB.FaceInfo;
import xu.kevin.pictowar.R;

public class MainActivity extends AppCompatActivity {
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private Button selectFace;
    private ImageView confirmedFace;
    private ListView faceList;
    private Button battleBtn;
    private FaceListAdapter officialFLD;
    private boolean hasConfirmedFace = false;
    private ImageView confirmedFace1;

    private Button backBTN;


    private Button verifyButton;


    //Faces
    private UUID mFaceId;


    //Images
    private Bitmap mBitmap;


    // Flag to indicate which level's image is being selected (Not really useful anymore)
    private static final int REQUEST_SELECT_IMAGE_0 = 0;

    public static FaceViewModel mainModel;
    private FaceInfo localFaceInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainModel = ViewModelProviders.of(this).get(FaceViewModel.class);

        localFaceInfo = mainModel.getFaceInfo();

        battleBtn = findViewById(R.id.battleBtn);
        selectFace = findViewById(R.id.selectFace);
        confirmedFace = findViewById(R.id.confirmedFace);
        faceList = findViewById(R.id.faceList);
        backBTN = findViewById(R.id.BackBTN);
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), StartScreen.class);
                startActivity(intent);
            }
        });
        faceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FaceListAdapter curAdapt = officialFLD;
                mFaceId = curAdapt.faces.get(position).faceId;
                confirmedFace.setImageBitmap(curAdapt.faceThumbnails.get(position));
                faceList.setAdapter(curAdapt);
            }
        });


        confirmedFace1 = findViewById(R.id.confirmedFace1);

        if(localFaceInfo!=null && localFaceInfo.getUserFace()!=null){
            confirmedFace1.setImageBitmap(BitmapFactory.decodeFile(localFaceInfo.getImageFilePath()));
            hasConfirmedFace = true;
        }

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
                    if(officialFLD == null || officialFLD.faces.size()  == 0){
                        Toast.makeText(getApplicationContext(), "No Image Selected", Toast.LENGTH_LONG ).show();
                    }else {

                        BitmapDrawable bmpd = (BitmapDrawable) confirmedFace.getDrawable();
                        confirmedFace1.setImageBitmap(bmpd.getBitmap());
                        storeImage(bmpd.getBitmap());
                        battleBtn.setVisibility(View.VISIBLE);
                        hasConfirmedFace = true;

                    }
                    //With further implementation of database, upon set face click, the selected face will be
                    //Saved within DB as the official face of the user
            }
        });

        battleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                    builder.setTitle("Enter Username");

                    final EditText userInput = new EditText(getApplicationContext());

                    userInput.setInputType(InputType.TYPE_CLASS_TEXT);

                    builder.setView(userInput);

                    builder.setPositiveButton("HOST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setNeutralButton("DONT HOST", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean closeDia = false;
                            String curUser = userInput.getText().toString();

                            if (curUser.equals("")) {
                                AlertDialog.Builder checkUser = new AlertDialog.Builder(MainActivity.this);
                                checkUser.setMessage("Please Enter A Username").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                            } else {
                                //Start Battle Activity
                                //Attach User in the Intent
                                Intent battleIntent = new Intent(getApplicationContext(), BattleActivity.class);
                                battleIntent.putExtra("username", curUser);
                                battleIntent.putExtra("bool",true);
                                startActivity(battleIntent);
                                closeDia = true;
                            }
                            if (closeDia) {
                                dialog.dismiss();
                            }


                        }
                    });
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean closeDia = false;
                        String curUser = userInput.getText().toString();

                        if (curUser.equals("")) {
                            AlertDialog.Builder checkUser = new AlertDialog.Builder(MainActivity.this);
                            checkUser.setMessage("Please Enter A Username").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                        } else {
                            //Start Battle Activity
                            //Attach User in the Intent
                            Intent battleIntent = new Intent(getApplicationContext(), BattleActivity.class);
                            battleIntent.putExtra("username", curUser);
                            battleIntent.putExtra("bool",false);
                            startActivity(battleIntent);
                            closeDia = true;
                        }
                        if (closeDia) {
                            dialog.dismiss();
                        }


                    }
                });
                }

        });


    }

    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("Media",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();
            mainModel.insertFaceInfo(new FaceInfo("",mFaceId,pictureFile.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            Log.d("File", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("File", "Error accessing file: " + e.getMessage());
        }
    }
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
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
            if (bmp != null) {
                detect(bmp);
            }

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

            }else {
                Toast.makeText(getApplicationContext(),"No Faces Detected",Toast.LENGTH_LONG).show();
            }

            faceList.setAdapter(fld);
            officialFLD = fld;

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

            FaceServiceClient faceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
            //Use Direct Reference Next Time instead of indirect Application reference that doesn't work
            //PictoFaceClient.getFaceServiceClient();
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
