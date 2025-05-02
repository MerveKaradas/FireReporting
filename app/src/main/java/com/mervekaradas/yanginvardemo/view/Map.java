package com.mervekaradas.yanginvardemo.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.mervekaradas.yanginvardemo.R;
import com.mervekaradas.yanginvardemo.databinding.ActivityMapBinding;
import com.mervekaradas.yanginvardemo.model.Place;

public class Map extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    Double selectedLatitude;
    Double selectedLongitude;
    Place selectedPlace;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        registerLauncher();
        sharedPreferences = this.getSharedPreferences("com.mervekaradas.yanginvardemo.view", MODE_PRIVATE);
        info = false;


        binding.btnAdresOnayla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.etAddress.getText().toString().isEmpty())
                {
                    Toast.makeText(Map.this, "Lütfen adresinizi giriniz", Toast.LENGTH_SHORT).show();
                } else if (selectedLatitude == 0.0 || selectedLongitude == 0.0) {
                    Toast.makeText(Map.this, "Lütfen haritadan yangın noktasını seçiniz", Toast.LENGTH_SHORT).show();
                } else
                {
                    showAlertDialog();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapLongClickListener(this);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {

                info = sharedPreferences.getBoolean("info", false);

                if (!info)
                {
                    System.out.println("Location: " + location.getLatitude() + " " + location.getLongitude());
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
                    sharedPreferences.edit().putBoolean("info",true).apply();
                }


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(binding.getRoot(),"Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
                    }
                }).show();
            } else {
                //request permission
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);


            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                LatLng lastUserLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
            }


            mMap.setMyLocationEnabled(true);
        }
    }

    private void registerLauncher() {

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if (result) {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);

                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                        LatLng lastUserLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation,15));
                    }
                }
            }
            else {
                Toast.makeText(this, "Permission needed!", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adres Onayı");
        builder.setMessage("Adres bilgisini doğru girdiğinize emin misiniz?");
        builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Evet butonuna tıklandığında yapılacak işlemler
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(Map.this, FirePhoto.class);
                selectedPlace = new Place(binding.etAddress.getText().toString(), selectedLatitude, selectedLongitude);
                intent.putExtra("place", selectedPlace);

                Toast.makeText(Map.this, "KONUM : " + selectedLatitude + " " + selectedLongitude , Toast.LENGTH_SHORT).show();

                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // Hayır butonuna tıklandığında yapılacak işlemler
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}