package xu.kevin.pictowar;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

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
}
