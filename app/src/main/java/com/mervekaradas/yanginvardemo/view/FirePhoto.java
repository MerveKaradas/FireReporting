package com.mervekaradas.yanginvardemo.view;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mervekaradas.yanginvardemo.R;
import com.mervekaradas.yanginvardemo.databinding.ActivityFirePhotoBinding;
import com.mervekaradas.yanginvardemo.model.Place;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FirePhoto extends AppCompatActivity {

    ActivityFirePhotoBinding binding;
    Place selectedPlace;
    ActivityResultLauncher<String> permissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<Intent> galleryActivityResultLauncher;
    ActivityResultLauncher<Intent> cameraActivityResultLauncher;
    Uri cameraPhotoUri;
    Uri imageData;
  //  Bitmap selectedImage;

    ArrayList<Uri> imageUris = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFirePhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        selectedPlace = (Place) getIntent().getSerializableExtra("place");
        registerLauncher();

        binding.btnUploadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageSourceDialog(view);
            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUris.size() >= 3) {
                    Toast.makeText(FirePhoto.this, "Fotoğraflar başarıyla yüklendi", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(FirePhoto.this, FireRaiting.class);
                    intent.putExtra("place", selectedPlace);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(FirePhoto.this, "Lütfen en az 3 adet fotoğraf yükleyin", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showImageSourceDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Fotoğraf Kaynağı Seçin")
                .setItems(new CharSequence[]{"Galeriden Seç", "Kamerayla Çek"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            selectImages(view);
                            break;
                        case 1:
                            takePhoto(view);
                            break;
                    }
                })
                .show();
    }

    public void selectImages(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentToGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryActivityResultLauncher.launch(intentToGallery);
        }
    }

    public void takePhoto(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Snackbar.make(view, "Permission needed for camera", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        permissionLauncher.launch(Manifest.permission.CAMERA);
                    }
                }).show();
            } else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (photoFile != null) {
                    cameraPhotoUri = FileProvider.getUriForFile(this, "com.mervekaradas.yanginvardemo.fileprovider", photoFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri);
                    cameraActivityResultLauncher.launch(takePictureIntent);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private void registerLauncher() {
        galleryActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        if (intentFromResult.getClipData() != null) {
                            int count = intentFromResult.getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = intentFromResult.getClipData().getItemAt(i).getUri();
                                imageUris.add(imageUri);
                                addImageView(imageUri);
                            }
                        } else if (intentFromResult.getData() != null) {
                            Uri imageUri = intentFromResult.getData();
                            imageUris.add(imageUri);
                            addImageView(imageUri);
                        }
                    }
                }
            }
        });

        cameraActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    imageUris.add(cameraPhotoUri);
                    addImageView(cameraPhotoUri);
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    Toast.makeText(FirePhoto.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FirePhoto.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addImageView(Uri imageUri) {
        LinearLayout layout = binding.linearlayoutFirePhotos; // ImageView'ların bulunduğu LinearLayout
        View imageContainer = getLayoutInflater().inflate(R.layout.image_container, layout, false);
        ImageView imageView = imageContainer.findViewById(R.id.imageView);
        ImageButton deleteButton = imageContainer.findViewById(R.id.deleteButton);

        imageView.setImageURI(imageUri);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.removeView(imageContainer);
                imageUris.remove(imageUri);
            }
        });

        layout.addView(imageContainer);
    }
}