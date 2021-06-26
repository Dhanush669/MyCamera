package com.devgd.mycamera;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import static com.devgd.mycamera.TakePicture.EditConstant;

public class EditActivity extends AppCompatActivity {

    ImageView captureImage;
    final int PIC_CROP = 1;
    byte[] byteArray;
    Bitmap bmp;
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        captureImage=findViewById(R.id.capturedImage);
        Intent editIntent=getIntent();
        byteArray = editIntent.getByteArrayExtra(EditConstant);
        bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        captureImage.setImageBitmap(bmp);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                captureImage.setImageURI(result.getUri());
                Toast.makeText(this, "Cropping successful, Sample: " + result.getSampleSize(), Toast.LENGTH_LONG).show();
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
//        Bitmap myImg = bmp;
//
//        Matrix matrix = new Matrix();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//        matrix.postRotate(90);
//
//        Bitmap rotated = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
//                matrix, true);
//
//        captureImage.setImageBitmap(rotated);
//            }
//        });
        executorService.execute(new Runnable() {
            @Override
            public void run() {
        Log.i("clickeeed","clicked");
        RotateImage rotateImage=new RotateImage();
        try {
            rotateImage.HandleSamplingAndRotationBitmap(EditActivity.this,getImageUri(EditActivity.this,bmp));
        } catch (IOException e) {
            e.printStackTrace();
        }
            }
        });
    }
}