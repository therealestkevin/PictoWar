package xu.kevin.pictowar.PictoWarGeneral;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import xu.kevin.pictowar.R;

public class SplashScreen extends AppCompatActivity {
    private Button start;
    private ImageView icon;
    private Animation fromBottom;
    private Animation fromTop;
    private TextView appName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        start = findViewById(R.id.btn_battle);
        icon = findViewById(R.id.Iv_icon);
        appName = findViewById(R.id.TV_appname);
        fromTop = AnimationUtils.loadAnimation(this, R.anim.from_top);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom);
        appName.setAnimation(fromTop);
        start.setAnimation(fromBottom);
        icon.setAnimation(fromTop);
        start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick( View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });


    }
}
