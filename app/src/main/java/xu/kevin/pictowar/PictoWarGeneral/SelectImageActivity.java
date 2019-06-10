
package xu.kevin.pictowar.PictoWarGeneral;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import xu.kevin.pictowar.R;

// The activity for the user to select a image and to detect faces in the image.
public class SelectImageActivity extends AppCompatActivity {
    // Flag to indicate the request of the next task to be performed
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_SELECT_IMAGE_IN_ALBUM = 1;

    // The URI of photo taken with camera
    private Uri mUriPhotoTaken;
    private String potentialPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_image);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    // Save the activity state when it's going to stop.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("ImageUri", mUriPhotoTaken);
    }

    // Recover the saved state when the activity is recreated.
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUriPhotoTaken = savedInstanceState.getParcelable("ImageUri");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_TAKE_PHOTO:
            case REQUEST_SELECT_IMAGE_IN_ALBUM:
                if (resultCode == RESULT_OK) {
                    Uri imageUri;
                    if (data == null || data.getData() == null) {
                        imageUri = mUriPhotoTaken;

                        potentialPath = imageUri.getPath();

                        String bob = "ekrjhqewrqwe";
                    } else {
                        imageUri = data.getData();
                        imageUri = compressURI(this, imageUri);
                        potentialPath = getRealPathFromURI(getApplicationContext(),imageUri);

                        String bob = "eqwqw";
                    }

                    Intent intent = new Intent();
                    intent.setData(imageUri);
                    intent.putExtra("realpath",potentialPath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
            default:
                break;
        }
    }
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    // Take Photo
    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null) {
            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                mUriPhotoTaken = Uri.fromFile(file);
                String temp = file.getPath();
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mUriPhotoTaken);
                startActivityForResult(intent, REQUEST_TAKE_PHOTO);
            } catch (IOException e) {
                setInfo(e.getMessage());
            }
        }
    }
    private Uri compressURI(Context context, Uri imageUri) {
        try{
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 10, 10, true);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, "Title", null);
            return Uri.parse(path);
        }catch(IOException e){
            e.printStackTrace();
            return imageUri;
        }

    }
    // Album/Gallery
    public void selectImageInAlbum(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM);
        }
    }

    // Set the information panel on screen.
    private void setInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }
}
