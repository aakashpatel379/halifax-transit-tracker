package com.developer.transitapp.display;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.developer.transitapp.R;
import com.developer.transitapp.background.VehiclePositionReader;
import com.developer.transitapp.model.MapState;
import com.developer.transitapp.model.MarkerData;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.transit.realtime.GtfsRealtime;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<GtfsRealtime.FeedEntity> feedEntities;
    LocationManager locationManager;
    private static final int REQUEST_CODE_PERMISSION = 21;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    ArrayList<MarkerData> busDataPoints=new ArrayList<>();
    Boolean permissionGranted=false;
    Handler mHandler;

    Marker marker;
    Runnable runnable;
    private int mInterval = 15000;
    LocationListener locationListener;
    String[] mPermission = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String TAG = "MapsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //restoring state
        if(savedInstanceState!=null)
         {  String jsonString = savedInstanceState.getString("mapState", null);
            MapState state = null;
            CameraPosition position = null;
            if (jsonString != null) {
                state = new Gson().fromJson(jsonString, MapState.class);
                position = new CameraPosition(state.getLatLng(), state.getZoom(), state.getTilt(), state.getBearing());

            }
            if (position != null) {
                CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
                if (mMap != null) {
                    mMap.moveCamera(update);
                    mMap.setMapType(state.getMapType());
                }
            }
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //pointCurrentLocation(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED)
        {

            requestPermissions(mPermission,REQUEST_CODE_PERMISSION);
            ActivityCompat.requestPermissions(MapsActivity.this,mPermission, REQUEST_CODE_PERMISSION);

        }

            createLocationRequest();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        CameraPosition position = mMap.getCameraPosition();
        MapState mapState;
        mapState = new MapState(position.target.latitude,position.target.longitude,position.zoom, position.bearing, position.tilt, mMap.getMapType());
        outState.putString("mapState",new Gson().toJson(mapState));
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
        String jsonString = savedInstanceState.getString("mapState",null);
        MapState state=null;
        CameraPosition position=null;
        if(jsonString!=null)
        {
            state = new Gson().fromJson(jsonString, MapState.class);
            position  = new CameraPosition(state.getLatLng(),state.getZoom() ,state.getTilt(), state.getBearing());

        }
        if(position!=null)
        {
            CameraUpdate update =CameraUpdateFactory.newCameraPosition(position);
            if(mMap!=null)
            {
            mMap.moveCamera(update);
            mMap.setMapType(state.getMapType());
            }
        }
        else
            Log.i(TAG, "onRestoreInstanceState: Failed");
    }


    private void pointCurrentLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        //get the location name from latitude and longitude
        Geocoder geocoder = new Geocoder(getApplicationContext());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(latitude, longitude, 1);
            String result = addresses.get(0).getLocality() + ":";
            result += addresses.get(0).getCountryName();
            LatLng latLng = new LatLng(latitude, longitude);
            if (marker != null) {
                marker.remove();
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result));
                //mMap.setMaxZoomPreference(20);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
            } else {
                marker = mMap.addMarker(new MarkerOptions().position(latLng).title(result));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));
                //mMap.setMaxZoomPreference(20);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void startRepeatingTask() {
        if(runnable!=null)
        runnable.run();
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId, String busno) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.iv_bus_icon);
        TextView busTextView = (TextView) customMarkerView.findViewById(R.id.tv_bus_no);
        busTextView.setText(busno);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private void addBusMarker() {
        mMap.clear();
        Log.d(TAG, "addingBusMarker");
        for(MarkerData md: busDataPoints)
        {
            if (mMap == null) {
                return;
            }
            mMap.addMarker(new MarkerOptions()
                    .position(md.getLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ic_bus,  md.getRouteId()))));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case 101:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        Log.d(TAG, "onActivityResult: ");
                        permissionGranted=true;
                        showDefaultLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(MapsActivity.this, "Permission not granted. Showing default location!", Toast.LENGTH_LONG).show();
                        showDefaultLocation();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        Boolean temp=true;
        if (requestCode == REQUEST_CODE_PERMISSION) {
                if (grantResults.length == 2
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted=true;
                    temp=false;
                }

            }
            }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startRepeatingTask();
        showDefaultLocation();
        MapsInitializer.initialize(this);
        mMap.setOnMyLocationButtonClickListener(onMyLocationButtonClickListener);
        mMap.setOnMyLocationClickListener(onMyLocationClickListener);
        enableMyLocation();
        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.setMinZoomPreference(11);
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void showDefaultLocation() {
        LatLng halifax = new LatLng(44.651070,  -63.582687);
        mMap.addMarker(new MarkerOptions().position(halifax).title("City of Halifax"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(halifax,14f));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mHandler!=null && runnable!=null)
            mHandler.removeCallbacks(runnable);
        locationManager.removeUpdates(locationListener);
    }

    protected void createLocationRequest() {
        //creating location request
        LocationRequest mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(10);
            mLocationRequest.setSmallestDisplacement(10);
            mLocationRequest.setFastestInterval(10);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);

            //dependent bus task on Permission task below
            mHandler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    VehiclePositionReader v = new VehiclePositionReader();
                    v.execute();
                    try {
                        feedEntities = v.get();
                    } catch (ExecutionException e) {
                        Log.d(TAG, "run: ExecutionException" + e.getMessage());
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        Log.d(TAG, "run: InterruptedException" + e.getMessage());
                        e.printStackTrace();
                    }

                    busDataPoints = new ArrayList<>();
                    for (GtfsRealtime.FeedEntity entity : feedEntities) {
                        MarkerData md;
                        GtfsRealtime.VehiclePosition vehicle;
                        double lattitude = 0, longitute = 0;
                        String routeId = "";
                        int directionId = 0;
                        if (entity.hasVehicle()) {
                            vehicle = entity.getVehicle();
                            GtfsRealtime.Position position;
                            if (vehicle.hasPosition()) {
                                position = vehicle.getPosition();
                                if (position.hasLatitude() && position.hasLongitude()) {
                                    lattitude = position.getLatitude();
                                    longitute = position.getLongitude();
                                }
                            }
                            GtfsRealtime.TripDescriptor trip;
                            if (vehicle.hasTrip()) {
                                trip = vehicle.getTrip();
                                if (trip.hasRouteId()) {
                                    routeId = trip.getRouteId();
                                    directionId = trip.getDirectionId();
                                }
                            }
                        }
                        md = new MarkerData(lattitude, longitute, directionId, routeId);
                        busDataPoints.add(md);
                    }
                    addBusMarker();
                    mHandler.postDelayed(runnable, mInterval);
                }
            };
            Task<LocationSettingsResponse> task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
            task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    resolvable.startResolutionForResult(
                                            MapsActivity.this,
                                            101);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                } catch (ClassCastException e) {
                                    // Ignore, should be an impossible error.
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                break;
                        }
                    }
                }
            });
        }


    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener =
            new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    //mMap.setMinZoomPreference(15);
                    return false;
                }
            };

    private GoogleMap.OnMyLocationClickListener onMyLocationClickListener =
            new GoogleMap.OnMyLocationClickListener() {
                @Override
                public void onMyLocationClick(@NonNull Location location) {
                  mMap.setMinZoomPreference(12);
                    CircleOptions circleOptions = new CircleOptions();
                    circleOptions.center(new LatLng(location.getLatitude(), location.getLongitude()));
                    circleOptions.radius(200);
                    circleOptions.fillColor(Color.RED);
                    circleOptions.strokeWidth(6);
                    mMap.addCircle(circleOptions);
                }
            };
}