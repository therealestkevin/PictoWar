package xu.kevin.pictowar.PictoWarGeneral;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;

import xu.kevin.pictowar.R;

public class BattleActivity extends AppCompatActivity {
    private String userName;
    private ConnectionsClient connectionsClient;
    private String opponentEndpointId;
    private String opponentName;
    private Bitmap recievedImage;
    private ImageView recievedImg;
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    recievedImage = BitmapFactory.decodeFile(payload.asFile().asJavaFile().getAbsolutePath());
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS && recievedImage != null) {

                    }
                }
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

                    } else {
                        Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {

                   Toast.makeText(getApplicationContext(),"Disconnected, Game Ending",Toast.LENGTH_LONG).show();
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
            if(bundle.getString("username")!=null){
                userName = bundle.getString("username");
            }

        }

        recievedImg = findViewById(R.id.recievedImg);
    connectionsClient = Nearby.getConnectionsClient(this);

    }
}
