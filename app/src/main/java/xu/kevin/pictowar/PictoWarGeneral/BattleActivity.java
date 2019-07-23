package xu.kevin.pictowar.PictoWarGeneral;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import id.zelory.compressor.Compressor;
import xu.kevin.pictowar.R;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BattleActivity extends AppCompatActivity {
    private String userName;
    private ConnectionsClient connectionsClient;
    private String opponentEndpointId;
    private String opponentName;
    private Bitmap recievedImage;
    private ImageView recievedImg;
    private TextView nameTextView;
    private TextView opponentNm;
    private TextView connectionStatus;
    private Button sendImageBtn;
    private Button switchCamera;
    private UUID opponentUUID;
    private CameraKitView cameraKitView;
    private boolean isHost = true;
    public static FaceViewModel battleModel;
    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,

            };
    private Bitmap winningImage;

    private static final float zoomIn =2f;

    private static final float zoomOut = 1f;

    private boolean isZoomed = false;

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {

                    int type = payload.getType();
                    try{
                        if(type == Payload.Type.BYTES){
                              if(new String(payload.asBytes(),UTF_8).equals("YES")){
                                    cameraKitView.setVisibility(View.GONE);
                                    sendImageBtn.setVisibility(View.GONE);
                                    switchCamera.setVisibility(View.GONE);
                                  recievedImg.setImageResource(R.drawable.ic_thumb_down_black_24dp);
                                    connectionStatus.setText("YOU LOST");
                                    nameTextView.setText("YOU LOST");
                                }else{
                                    opponentUUID = getUUIDFromBytes(payload.asBytes());
                                    int temp = 0;
                                }

                        }else {
                            recievedImage = BitmapFactory.decodeFile(payload.asFile().asJavaFile().getAbsolutePath());
                            //String path = MediaStore.Images.Media.insertImage(getContentResolver(), recievedImage, "temporary", "test");
                            int temp = 0;
                        }
                    }catch(NullPointerException e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS && recievedImage != null) {
                            recievedImg.setImageBitmap(recievedImage);
                    }
                }
            };

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {

                    connectionsClient.requestConnection(userName, endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {}
            };


    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {

                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    opponentName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {


                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        opponentEndpointId = endpointId;
                        //setOpponentName(opponentName);
                        opponentNm.setText("Opponent Name: "+opponentName);
                        connectionStatus.setText("Connected");

                            UUID temp = battleModel.getFaceInfo().getUserFace();
                            Payload userUUID = Payload.fromBytes(getBytesFromUUID(temp));
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId,userUUID);

                        nameTextView.setText("Game Starting in 5 Seconds");
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                cameraKitView.setVisibility(View.VISIBLE);
                                sendImageBtn.setVisibility(View.VISIBLE);
                                switchCamera.setVisibility(View.VISIBLE);
                            }
                        }, 5000);
                    } else {
                        Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    connectionStatus.setText("Disconnected");
                   Toast.makeText(getApplicationContext(),"Disconnected, Game Ending",Toast.LENGTH_LONG).show();
                   cameraKitView.setVisibility(View.GONE);

                }
            };


    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
        verifyStoragePermissions(this);
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Long high = byteBuffer.getLong();
        Long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    1
            );
        }
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @CallSuper
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Missing Permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);
        recievedImg = findViewById(R.id.recievedImg);
        nameTextView = findViewById(R.id.nameTextView);
        opponentNm = findViewById(R.id.opponentName);
        connectionStatus = findViewById(R.id.connectionStatus);
        sendImageBtn = findViewById(R.id.sendImageBtn);
        switchCamera = findViewById(R.id.switchCamera);
       battleModel = ViewModelProviders.of(this).get(FaceViewModel.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle.getString("username")!=null){
                userName = bundle.getString("username");
                nameTextView.setText("Your Name: " + userName);
                isHost = bundle.getBoolean("bool");
            }

        }

       cameraKitView = findViewById(R.id.battleCamera);
        cameraKitView.setGestureListener(new CameraKitView.GestureListener() {
            @Override
            public void onTap(CameraKitView cameraKitView, float v, float v1) {

            }

            @Override
            public void onLongTap(CameraKitView cameraKitView, float v, float v1) {

            }

            @Override
            public void onDoubleTap(CameraKitView cameraKitView, float v, float v1) {

                if(isZoomed){
                    cameraKitView.setZoomFactor(zoomOut);
                }else{
                    cameraKitView.setZoomFactor(zoomIn);
                    isZoomed=true;
                }

            }

            @Override
            public void onPinch(CameraKitView cameraKitView, float v, float v1, float v2) {
                //Flesh out zooming functionality later
                //Need to figure what the three floats represent in order
                //Might use the onDoubleTap for a simpler approach to this issue
            }
        });
        switchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cameraKitView.getFacing() == CameraKit.FACING_BACK){
                    cameraKitView.setFacing(CameraKit.FACING_FRONT);
                }else{
                    cameraKitView.setFacing(CameraKit.FACING_BACK);
                }
            }
        });
    connectionsClient = Nearby.getConnectionsClient(this);

        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               new sendingInfoTask().execute();




                    //Intent intent = new Intent(getApplicationContext(), SelectImageActivity.class);
                   // startActivityForResult(intent, 0);
            }
        });

        if(!isHost){
            startAdvertising();

        }else{
            startAdvertising();
            startDiscovery();
        }
    }
    private class sendingInfoTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            cameraKitView.captureImage(new  CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inMutable = true;
                    //Making a Bitmap to be able to be used by Face API
                    Bitmap bmp = BitmapFactory.decodeByteArray(capturedImage, 0, capturedImage.length, options);

                    winningImage = bmp;

                    Toast.makeText(getApplicationContext(),"Image Captured",Toast.LENGTH_SHORT).show();
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());



                    try {
                        Face[] allFaces = new DetectionTask().execute(inputStream).get();
                        UUID[] allUUID = new UUID[allFaces.length];

                        for(int i =0; i<allFaces.length;i++){
                            allUUID[i] = allFaces[i].faceId;
                        }

                        boolean isWin = false;

                        for(UUID i : allUUID){
                            VerifyResult tempResult = new VerificationTask(opponentUUID,i).execute().get();
                            String temp = "s";
                            isWin = tempResult.isIdentical;
                        }

                        if(isWin){
                            connectionsClient.sendPayload(opponentEndpointId,Payload.fromBytes("YES".getBytes(UTF_8)));


                            cameraKitView.setVisibility(View.GONE);
                            sendImageBtn.setVisibility(View.GONE);
                            switchCamera.setVisibility(View.GONE);
                            //recievedImg.setImageResource(R.drawable.ic_thumb_up_black_24dp);
                            connectionStatus.setText("YOU WON");
                            nameTextView.setText("YOU WON");

                            Uri winningUri = getImageUri(getApplicationContext(),winningImage);

                            try {
                                File compressedImageFile = new Compressor(getApplicationContext()).compressToFile(new File(winningUri.getPath()));


                                Uri newUri = Uri.fromFile(compressedImageFile);
                                try{
                                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(newUri,"r" );
                                    Payload filePayload = Payload.fromFile(pfd);
                                    connectionsClient.sendPayload(opponentEndpointId,filePayload);
                                }catch(FileNotFoundException e){
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        recievedImg.setImageBitmap(winningImage);

                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }


                }
            });
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.disable();
            return null;
        }
    }
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

            FaceServiceClient faceServiceClient = new FaceServiceRestClient(getString(R.string.endpoint), getString(R.string.subscription_key));
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
    @Override
    public void onBackPressed(){
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        adapter.disable();
        finish();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            adapter.disable();
            finish();

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();

            try {
                File compressedImageFile = new Compressor(this).compressToFile(new File(uri.getPath()));


                Uri newUri = Uri.fromFile(compressedImageFile);
                try{
                    ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(newUri,"r" );
                    Payload filePayload = Payload.fromFile(pfd);
                    connectionsClient.sendPayload(opponentEndpointId,filePayload);
                }catch(FileNotFoundException e){
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*try{
                String temp = uri.getPath();
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri,"r" );
                Payload filePayload = Payload.fromFile(pfd);
                connectionsClient.sendPayload(opponentEndpointId,filePayload);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }*/

        }

    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    private Uri getImageUri(Context context,  Uri Image) {
        try {
            Bitmap inImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Image);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            File tempDir= Environment.getExternalStorageDirectory();
            tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
            tempDir.mkdir();
            File tempFile = File.createTempFile("title", ".jpg", tempDir);
            //   ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.PNG, 1, bytes);
            //Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));
            byte[] bitmapData = bytes.toByteArray();
            //write the bytes in file
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return Image;
        }
    }


    private void startDiscovery() {
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
    }

    private void startAdvertising() {

        connectionsClient.startAdvertising(
                userName, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_CLUSTER).build());
    }
}
