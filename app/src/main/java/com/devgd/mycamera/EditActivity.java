package com.devgd.mycamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.devgd.mycamera.TakePicture.EditConstant;

public class EditActivity extends AppCompatActivity {

    ImageView captureImage;
    byte[] byteArray;
    Bitmap bmp;
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    List<Bitmap> undo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        captureImage=findViewById(R.id.capturedImage);
        Intent editIntent=getIntent();
        byteArray = editIntent.getByteArrayExtra(EditConstant);
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        captureImage.setImageBitmap(bmp);
        undo=new ArrayList<>();
        undo.add(bmp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                try {
                    bmp=MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getUri());
                    undo.add(bmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                captureImage.setImageBitmap(bmp);
                Toast.makeText(this, "Cropping successful, Sample: ", Toast.LENGTH_SHORT).show();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Toast.makeText(this, "Cropping failed: " + result.getError(), Toast.LENGTH_LONG).show();
            }
        }
    }

    public void crop(View view) {
            cropImage(getImageUri(EditActivity.this,bmp));
    }

   public void cropImage(Uri uri){
        try {
            CropImage.activity(uri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMultiTouchEnabled(true)
                    .setAllowRotation(false)
                    .start(this);
        }catch (Exception e){
            e.printStackTrace();
        }
   }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void rotate(View view) {
        rotateImage();
    }
    public void rotateImage(){
        executorService.execute(() -> {
            Bitmap bInput =bmp;
            float degrees = 90;
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees);
            bmp = Bitmap.createBitmap(bInput, 0, 0, bInput.getWidth(), bInput.getHeight(), matrix, true);
            EditActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    captureImage.setImageBitmap(bmp);
                    Log.i("Sizeeeebbb", String.valueOf(undo.size()));
                    undo.add(bmp);
                    Log.i("Sizeeeeaaa", String.valueOf(undo.size()));
                }
            });
        });


    }

    public void undo(View view) {
        performUndo();
    }

    public void performUndo() {
        int lastPosition;
        if (undo.size() > 0) {
            if (undo.size() > 1) {
                lastPosition = undo.size() - 1;
                undo.remove(lastPosition);
                Log.i("last possssi bfrrreee", String.valueOf(lastPosition));
                lastPosition = undo.size() - 1;
                Log.i("last possssi aff", String.valueOf(lastPosition));
                captureImage.setImageBitmap(undo.get(lastPosition));
                bmp = undo.get(lastPosition);
            } else {
                Log.i("in zerooo","ooo");
                lastPosition = 0;
                captureImage.setImageBitmap(undo.get(lastPosition));
                bmp = undo.get(lastPosition);
                undo.remove(lastPosition);
            }

        }
    }

    public void save(View view) {
        try {
            bitmapToByteArray(bmp);
            saveToGallery();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToGallery() throws IOException {
        File file = new File(Environment.getExternalStorageDirectory()+"/"+ UUID.randomUUID().toString()+".jpg");
        OutputStream outputStream = null;
        try{
            outputStream = new FileOutputStream(file);
            outputStream.write(byteArray);
        }catch (Exception e){
            e.printStackTrace();
        }

        finally {
            if(outputStream != null)
                outputStream.close();
        }
        addImageToGallery(file.getAbsolutePath(),EditActivity.this);

    }

    public void bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray= stream.toByteArray();
    }

    public static void addImageToGallery(final String filePath, final Context context) {

        ContentValues values = new ContentValues();

        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, filePath);

        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

}