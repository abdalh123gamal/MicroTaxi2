package com.abdalh.microtaxi.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.List;

public class RiderHome extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private AppBarConfiguration mAppBarConfiguration;


    private GoogleMap mMap;

    // play services

    private static final int MY_PERMISSION_REQUEST_CODE=7000;
    private static final int PLAY_SERVICES_RES_REQUEST=7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment supportMapFragment ;
    private Location mLastLocation;


    private static int UPDATE_INTERVAL=5000;
    private static int FATEST_INTERVAL=3000;
    private static int DISPLACEMENT=10;

    private LatLng pickupLocation;
    private Marker pickupMarker;

    private FirebaseAuth auth ;

    private MaterialAnimatedSwitch location_switch;
    private SupportMapFragment mapFragment;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private Button btn_request;

    boolean requestBol= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        drawerLayout=findViewById(R.id.drawer_layout_rider_home);
        navigationView=findViewById(R.id.rider_home_nav_view);


        btn_request=findViewById(R.id.rider_home_btn_request);

        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if(requestBol){
                  requestBol=false;
                  geoQuery.removeAllListeners();
                  driverLocationRef.removeEventListener(driverLocationRefListener);

                  if(driverFoundID!=null){
                      DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
                      driverRef.setValue(true);
                      driverFoundID=null;

                  }
                  driverFound=false;
                  radius=1;


                  String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                  DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RiderRequest");
                  GeoFire geoFire=new GeoFire(ref);
                  geoFire.removeLocation(userId);

                  if(pickupMarker!=null){
                      pickupMarker.remove();
                  }
                  btn_request.setText("طلب سسارة");




              }
              else{
                  requestBol=true;
                  String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                  DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RiderRequest");
                  GeoFire geoFire=new GeoFire(ref);
                  geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                  pickupLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                  pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("pickup here").icon(BitmapDescriptorFactory.fromResource(R.drawable.attachment)));

                  btn_request.setText("جاري طلب ميكروباص ..");

                  getClosestDriver();
              }

            }

           
        });

        


        setToolbar();

        try {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_home);
            supportMapFragment.getMapAsync(this);
        }catch (Exception e){
            Log.i("MapException" , e.getMessage());
        }

        Snackbar.make(drawerLayout,R.string.main_toast_the_application_is_now_operational,Snackbar.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(RiderHome.this,new String []{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);



        }
        else{
            supportMapFragment.getMapAsync(this);

        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.drawer_menu_ride_record:
                        Toast.makeText(getApplication(),"لسه سجل رحلاتك فاضي ",Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.drawer_menu_about_micro:
                        startActivity(new Intent(getApplication(), AboutMicroTaxi.class));
                        return true;
                    case R.id.drawer_menu_setting:
                        startActivity(new Intent(getApplicationContext(), SettingProfile.class));
                        return  true;
                    case R.id.drawer_menu_feedback:
                        startActivity(new Intent(getApplication(), FeedBack.class));
                        return  true;
                    case R.id.drawer_menu_logout:
                        Toast.makeText(getApplication(),"جاري تسجيل الخروج",Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), ActivitySelectType.class));
                        finish();
                        return  true;
                    case R.id.drawer_menu_nav_send:
                        Intent Intent=new Intent();
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Micro Taxi");
                        intent.putExtra(Intent.EXTRA_TEXT, "Hey, download this app! https://MicroTaxi.Met.com/");
                        startActivity(Intent.createChooser(intent, "choose one"));

                        return true;

                }
                return true;
            }
        });

    }

    // البحث عن أقرب سائق للراكب

    private int radius =-1;
    private boolean driverFound=false;
    GeoQuery geoQuery;
    private String  driverFoundID;

    private void getClosestDriver() {
        DatabaseReference driverLocation=FirebaseDatabase.getInstance().getReference().child("driversAvailable");

        GeoFire geoFire =new GeoFire(driverLocation);

        geoQuery=geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude,pickupLocation.longitude),radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound&&requestBol)
                {
                    driverFound=true;
                    driverFoundID=key;

                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
                    String riderID=FirebaseAuth.getInstance().getCurrentUser().getUid();

                    HashMap map=new HashMap();
                    map.put("customerRideId",riderID);
                    driverRef.updateChildren(map);

                    getDriverLocation();
                    btn_request.setText("أبحث عن موقع سائق");
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

                if(!driverFound)
                {  radius++;

                    getClosestDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker MDriverMarker;
    private DatabaseReference driverLocationRef ;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("1");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&requestBol) {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng =0;
                    btn_request.setText("Driver Found");

                    if(map.get(0)!=null)
                    {
                    locationLat=Double.parseDouble(map.get(0).toString());
                    }

                    if(map.get(1)!=null)
                    {
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLngLat=new LatLng(locationLat,locationLng);

                     if(MDriverMarker!=null)
                     {
                         MDriverMarker.remove();
                     }
                     Location loc1=new Location("");
                     loc1.setLatitude(pickupLocation.latitude);
                     loc1.setLongitude(pickupLocation.longitude);


                    Location loc2=new Location("");
                    loc1.setLatitude(driverLngLat.latitude);
                    loc1.setLongitude(driverLngLat.longitude);

                    float distance =loc1.distanceTo(loc2);
                    if (distance<100){
                        btn_request.setText("Driver is here ");
                    }
                    else{
                        btn_request.setText("Driver Found :" +distance);

                    }
                    {

                    }


                     MDriverMarker=mMap.addMarker(new MarkerOptions().position(driverLngLat).title("Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.transportt)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void setToolbar() {
//         drawerLayout=findViewById(R.id.drawer_layout_driver);
//        Toolbar toolbar = findViewById(R.id.rider_home_activity_toolbar);
//         setSupportActionBar(toolbar);
//        drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
//         drawerLayout.addDrawerListener(drawerToggle);
//         drawerToggle.syncState();
//        //remove name app
//        getSupportActionBar().setDisplayShowTitleEnabled(false);



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        buildGoogleMapApi();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleMapApi() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation=location;

        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));


        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));




    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest =new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(RiderHome.this,new String []{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSION_REQUEST_CODE);
        }
        if(mGoogleApiClient.isConnected()){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(this,"please provide the permission ",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

}
