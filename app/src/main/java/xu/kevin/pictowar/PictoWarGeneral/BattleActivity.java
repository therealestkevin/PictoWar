package xu.kevin.pictowar.PictoWarGeneral;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.UUID;

import id.zelory.compressor.Compressor;
import xu.kevin.pictowar.R;

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
    private UUID opponentUUID;
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
                                opponentUUID = getUUIDFromBytes(payload.asBytes());
                                int temp = 0;
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
                        if(!isHost){
                            UUID temp = battleModel.getFaceInfo().getUserFace();
                            Payload userUUID = Payload.fromBytes(getBytesFromUUID(temp));
                            Nearby.getConnectionsClient(getApplicationContext()).sendPayload(opponentEndpointId,userUUID);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    connectionStatus.setText("Disconnected");
                   Toast.makeText(getApplicationContext(),"Disconnected, Game Ending",Toast.LENGTH_LONG).show();

                }
            };


    @Override
    protected void onStart() {
        super.onStart();
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
       battleModel = ViewModelProviders.of(this).get(FaceViewModel.class);
        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle.getString("username")!=null){
                userName = bundle.getString("username");
                nameTextView.setText("Your Name: "+userName);
                isHost = bundle.getBoolean("bool");
            }

        }

    connectionsClient = Nearby.getConnectionsClient(this);

        sendImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SelectImageActivity.class);
                    startActivityForResult(intent, 0);
            }
        });

        if(!isHost){
            startAdvertising();

        }else{
            startAdvertising();
            startDiscovery();
        }
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

            try{
                String temp = uri.getPath();
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri,"r" );
                Payload filePayload = Payload.fromFile(pfd);
                connectionsClient.sendPayload(opponentEndpointId,filePayload);
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }

        }

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
