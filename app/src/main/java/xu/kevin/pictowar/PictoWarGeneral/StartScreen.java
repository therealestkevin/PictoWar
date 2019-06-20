package xu.kevin.pictowar.PictoWarGeneral;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import xu.kevin.pictowar.PictoDB.FaceInfo;
import xu.kevin.pictowar.R;

public class StartScreen extends AppCompatActivity {
    private Button battleBtn;
    private Button profile;
    private ImageView face;
    public static FaceViewModel mainModel;
    private FaceInfo localFaceInfo;
    private boolean canBattle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        canBattle = false;
        battleBtn = findViewById(R.id.Battle);
        profile = findViewById(R.id.Profile);
        face = findViewById(R.id.face1);
        mainModel = ViewModelProviders.of(this).get(FaceViewModel.class);
        localFaceInfo = mainModel.getFaceInfo();
        if(localFaceInfo!=null && localFaceInfo.getUserFace()!=null){
            face.setImageBitmap(BitmapFactory.decodeFile(localFaceInfo.getImageFilePath()));
            canBattle = true;
        }
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        battleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canBattle == false) {


                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StartScreen.this);

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
                                AlertDialog.Builder checkUser = new AlertDialog.Builder(StartScreen.this);
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
                                battleIntent.putExtra("bool", true);
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
                                AlertDialog.Builder checkUser = new AlertDialog.Builder(StartScreen.this);
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
                                battleIntent.putExtra("bool", false);
                                startActivity(battleIntent);
                                closeDia = true;
                            }
                            if (closeDia) {
                                dialog.dismiss();
                            }


                        }
                    });
                }
            }

        });
    }
}
