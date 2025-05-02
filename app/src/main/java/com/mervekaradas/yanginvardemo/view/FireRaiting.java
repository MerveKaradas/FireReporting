package com.mervekaradas.yanginvardemo.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mervekaradas.yanginvardemo.R;
import com.mervekaradas.yanginvardemo.databinding.ActivityFireRaitingBinding;
import com.mervekaradas.yanginvardemo.model.Place;
import com.mervekaradas.yanginvardemo.model.WeatherResponse;
import com.mervekaradas.yanginvardemo.service.WeatherService;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FireRaiting extends AppCompatActivity {

    ActivityFireRaitingBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    double latitude;
    double longitude;
    Place selectedPlace;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFireRaitingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        // Intent ile gelen verileri al
        Intent intent = getIntent();
        selectedPlace = (Place) intent.getSerializableExtra("place");

        latitude = selectedPlace.getLatitude();
        longitude = selectedPlace.getLongitude();

        Toast.makeText(this, "Enlem : " + latitude + "\nBoylam : " + longitude + "", Toast.LENGTH_SHORT).show();

       getWeatherData(latitude, longitude, binding.tvCityName, binding.tvTemperature);


       binding.btnSaveSurvey.setOnClickListener(v -> {
           if (binding.radioGroupIntensity.getCheckedRadioButtonId() == -1 || binding.radioGroupDuration.getCheckedRadioButtonId() == -1) {
               Toast.makeText(FireRaiting.this, "Lütfen değerlendirmeleri doldurun!", Toast.LENGTH_SHORT).show();
               return;
           }
           showAlertDialog();
       });


    }




    private void getWeatherData(double latitude, double longitude, TextView cityNameTextView, TextView temperatureTextView) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherService service = retrofit.create(WeatherService.class);

        Call<WeatherResponse> call = service.getCurrentWeather(latitude, longitude, "c5b3bfc6ded28398bbb8f9313e9c2dc8");

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful()) {
                    WeatherResponse weatherResponse = response.body();
                    if (weatherResponse != null) {
                        float tempKelvin = weatherResponse.getMain().getTemp();
                        float tempCelsius = tempKelvin - 273.15f; // Kelvin'den Celsius'a dönüştürme
                        String cityName = weatherResponse.getName();
                        String weatherDescription = weatherResponse.getWeather().get(0).getDescription();

                        // Hava durumu verilerini UI üzerinde güncelleyin
                        String weatherInfo = "City: " + cityName + "\nTemperature: " + tempCelsius + "°C\nDescription: " + weatherDescription;
                        Toast.makeText(FireRaiting.this, weatherInfo, Toast.LENGTH_LONG).show();

                        binding.tvCityName.setText("City: " + cityName);
                        binding.tvTemperature.setText("Temperature: " + tempCelsius + "°C");
                        binding.tvDescription.setText("Description: " + weatherDescription);
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                // Hata durumunu yönetin
                Toast.makeText(FireRaiting.this, "Failed to get weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFireNotification()
    {

        System.out.println("saveFireNotificationda");
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid(); // Firebase Authentication'dan gelen kullanıcı kimliği

            Map<String, Object> rating = new HashMap<>();
            rating.put("fireDuration", binding.radioGroupDuration.getCheckedRadioButtonId());
            rating.put("fireIntensity", binding.radioGroupIntensity.getCheckedRadioButtonId());
            rating.put("latidude", selectedPlace.getLatitude());
            rating.put("longitude", selectedPlace.getLongitude());
            rating.put("placeName", selectedPlace.getName());


            System.out.println("id " +userId);
            System.out.println("kayıt içinde");

            // Firestore'a yaz
            firestore.collection("users").document(userId).collection("fireNotifications").add(rating)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedildi!", Toast.LENGTH_SHORT).show();
                     //   finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(FireRaiting.this, "Lütfen oturum açın.", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveFireNotification1()
    {
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {

            String uid = firebaseAuth.getCurrentUser().getUid();
            System.out.println("saveFireNotificationda");
            System.out.println("raitingden "+firebaseAuth.getCurrentUser().toString());
            System.out.println("uid " +uid);

            if (uid != null) {
                Map<String, Object> rating = new HashMap<>();
                rating.put("fireDuration", binding.radioGroupDuration.getCheckedRadioButtonId());
                rating.put("fireIntensity", binding.radioGroupIntensity.getCheckedRadioButtonId());
                rating.put("latidude", selectedPlace.getLatitude());
                rating.put("longitude", selectedPlace.getLongitude());
                rating.put("placeName", selectedPlace.getName());

                // Firestore'a yaz
                firestore.collection("users").document(uid).collection("fireNotifications")
                        .add(rating)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedildi!", Toast.LENGTH_SHORT).show();
                            //   finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedilemedi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
              //  Toast.makeText(FireRaiting.this, "Lütfen oturum açın.", Toast.LENGTH_SHORT).show();
                Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedildi!", Toast.LENGTH_SHORT).show();
            }

        } else {
           // Toast.makeText(FireRaiting.this, "Lütfen oturum açın.", Toast.LENGTH_SHORT).show();
            Toast.makeText(FireRaiting.this, "Yangın bildiriminiz kaydedildi!", Toast.LENGTH_SHORT).show();
        }


    }



    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yangın Bildirimi Gönderiliyor");
        builder.setMessage("Yangın bildirimi göndermek istiyor musunuz?");
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Evet butonuna tıklandığında yapılacak işlemler
                saveFireNotification();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Hayır butonuna tıklandığında yapılacak işlemler
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}