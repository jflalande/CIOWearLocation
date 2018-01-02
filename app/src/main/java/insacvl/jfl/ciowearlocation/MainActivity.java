package insacvl.jfl.ciowearlocation;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DecimalFormat;

public class MainActivity extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnSuccessListener<Location> {

    private FusedLocationProviderClient flpc;
    private LocationCallback lc;
    private TextView gpsPresence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsPresence = (TextView) findViewById(R.id.GPSpresence);

        // Enables Always-on
        setAmbientEnabled();

        ImageView imgGPS = (ImageView)findViewById(R.id.gps);
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            Log.d("CIO", "This watch has no GPS.");
            gpsPresence.setText("No. Using:");
            imgGPS.setMaxWidth(250);
            imgGPS.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.telgps4));
        }
        else {
            Log.d("CIO", "GPS available :)");
            gpsPresence.setText("Yes :)");
            imgGPS.setImageBitmap(BitmapFactory.decodeResource(getResources(), android.R.drawable.presence_online));
        }



        GoogleApiClient gapi = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gapi.connect();
    }

    // Should check if the permission is granted for this app
    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("CIO", "GAPI connected");

        // Get the location provider through the GoogleApi
        flpc = LocationServices.getFusedLocationProviderClient(this);

        // Check location availability
        @SuppressLint("MissingPermission") Task<LocationAvailability> locAvailable = flpc.getLocationAvailability();
        locAvailable.addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
            @Override
            public void onSuccess(LocationAvailability locationAvailability) {
                Log.i("CIO", "Location is available = " + locationAvailability.toString());
            }
        });

        // Ask for update of the location
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000).setFastestInterval(1000);
        lc = new LocationCallback();
        flpc.requestLocationUpdates(locationRequest, lc, null);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("CIO", "Error with GAPI connection");
    }


    // Should check if the permission is granted for this app
    @SuppressLint("MissingPermission")
    public void getlocation(View view) {
        Task<Location> loc = flpc.getLastLocation();
        loc.addOnSuccessListener(this);
    }

    // Callback for the location task
    @Override
    public void onSuccess(Location loc) {
        Log.i("CIO", "Task completed.");
        if (loc != null) {
            DecimalFormat df = new DecimalFormat("#.##");
            String lon = df.format(loc.getLongitude());
            String lat = df.format(loc.getLatitude());
            Log.i("CIO", "Location: " + lon  + " , " + lat );
            TextView latTV = (TextView)findViewById(R.id.lat);
            latTV.setText("latitude: " + lon);
            TextView lonTV = (TextView)findViewById(R.id.lon);
            lonTV.setText("longitude: " + lat);
        }
        else
        {
            Log.i("CIO", "No defined location ! Are you inside ? Have you authorized location on the smartwatch ?");
        }
    }






    @Override
    public void onConnectionSuspended(int i) {
        Log.i("CIO", "GAPI supsended");
    }



    @Override
    protected void onStop() {
        super.onStop();
        if (lc != null)
            flpc.removeLocationUpdates(lc);
    }
}
