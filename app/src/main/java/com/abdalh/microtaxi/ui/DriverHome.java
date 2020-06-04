package com.abdalh.microtaxi.ui;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverHome extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener, RoutingListener {
    private AppBarConfiguration mAppBarConfiguration;
    private GoogleMap mMap;
    // play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    FirebaseAuth auth;
    MaterialAnimatedSwitch location_switch;
    SupportMapFragment mapFragment;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private  BottomSheetDialog bottomSheetDialog;
    private String customerId = "";
    private boolean isLoginOut = false;

    private Switch switch_location;
    private  LocationManager manager;

    private ImageView mRiderProfileImage;
    private TextView mRiderName,mRiderPhone,mRiderDestination;

    private String mName;
    private String mEmail;
    private String mProfileImageUrl;
    private String mCarImageUrl;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        polyLines = new ArrayList<>();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_driver);

        drawerLayout = findViewById(R.id.drawer_layout_driver_home);
        navigationView = findViewById(R.id.driver_home_nav_view);
        switch_location=findViewById(R.id.driver_home_switch_run);
        switch_location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getApplication(), "لسه تحت العمل ", Toast.LENGTH_LONG).show();

//                if (isChecked== true){
//                    LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//
//                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        buildAlertMessageNoGps();
//                    }
//                }


            }
        });





        bottomSheetDialog=new BottomSheetDialog(DriverHome.this,R.style.BottomSheetDialogTheme);
        View bottomSheetView= LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_sheet_rider, (LinearLayout)findViewById(R.id.bottom_sheet_rider_container));
                 bottomSheetDialog.setContentView(bottomSheetView);
                 mRiderProfileImage = bottomSheetView.findViewById(R.id.bottom_sheet_rider_container_iv_image);
        ImageView show_bottom_sheet=findViewById(R.id.driver_home_iv_info_rider_show_bottom_sheet);
        show_bottom_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });

        mRiderName = bottomSheetView.findViewById(R.id.bottom_sheet_rider_container_tv_name);
        mRiderPhone = bottomSheetView.findViewById(R.id.bottom_sheet_rider_container_tv_phone);
        mRiderDestination = bottomSheetView.findViewById(R.id.bottom_sheet_rider_container_tv_destination);


        setToolbar();
        updateNavHeader();
        Snackbar.make(drawerLayout, R.string.main_toast_the_application_is_now_operational, Snackbar.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHome.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);

        } else {
            mapFragment.getMapAsync(this);

        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_menu_ride_record:
                        Toast.makeText(getApplication(), "لسه سجل رحلاتك فاضي ", Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.drawer_menu_about_micro:
                        startActivity(new Intent(getApplication(), AboutMicroTaxi.class));
                        return true;
                    case R.id.drawer_menu_setting:
                        startActivity(new Intent(getApplicationContext(), SettingDriverInfo.class));
                        return true;
                    case R.id.drawer_menu_feedback:
                        startActivity(new Intent(getApplication(), FeedBack.class));
                        return true;
                    case R.id.drawer_menu_logout:
                        isLoginOut = true;
                        disconnectDriver();
                        Toast.makeText(getApplication(), "جاري تسجيل الخروج", Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(), ActivitySelectType.class));
                        finish();
                        return true;
                    case R.id.drawer_menu_nav_send:
                        Intent Intent = new Intent();
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
        getAssignedRider();

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("يعني أنت داخل تطبيق عشان تحدد ال location بتاعك وتاكل عيش واحنا مكرمين هنحددلك الطريق اللي يوصلك للزباين مش مشغل ال Gbs  ؟")
                .setCancelable(false)
                .setPositiveButton("ادخل الاعدادات ياعم ومتقرفناش معاك", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("اغلاق ومشفش وشك في التطبيق", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    private void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_driver_home);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout_driver_home);
        navigationView = findViewById(R.id.driver_home_nav_view);

        drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        //remove name app
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    public void updateNavHeader(){
        navigationView=findViewById(R.id.driver_home_nav_view);
        View headerView=navigationView.getHeaderView(0);
        final ImageView navImage=headerView.findViewById(R.id.header_driver_iv_profile);

        final TextView navDriverName=headerView.findViewById(R.id.header_driver_tv_name);
        final TextView navDriverEmail=headerView.findViewById(R.id.header_driver_tv_email);

        String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRiderDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userID);
        mRiderDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mName = map.get("name").toString();
                        navDriverName.setText(mName);
                    }
                    if (map.get("email") != null) {
                        mEmail = map.get("email").toString();
                        navDriverEmail.setText(mEmail);
                    }
                    if (map.get("profileImageUri") != null) {
                        mProfileImageUrl = map.get("profileImageUri").toString();
                        navImage.setImageURI(Uri.parse(mProfileImageUrl));
                        Glide.with(getApplicationContext()).load(mProfileImageUrl).into(navImage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getAssignedRider() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedRiderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRequest").child("customerRideId");
        assignedRiderRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedRiderPickupLocation();
                    getAssignedRiderDestination();
                    getAssignedRiderInfo();

                } else{
                    erasePolyLines();
                    customerId = "";
                    if (pickupMarker !=null) {
                        pickupMarker.remove();
                    }

                    if (assignedRiderPickupLocationRef != null) {
                        assignedRiderPickupLocationRef.removeEventListener(assignedRiderRefPickupLocationListener);
                    }

                    bottomSheetDialog.dismiss();
                    mRiderProfileImage.setImageResource(R.drawable.ic_rider);
                    mRiderName.setText("");
                    mRiderPhone.setText("");
                    mRiderDestination.setText("");



                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }



    private Marker pickupMarker;
    private DatabaseReference assignedRiderPickupLocationRef;
    private ValueEventListener assignedRiderRefPickupLocationListener;
    private void getAssignedRiderPickupLocation() {

        assignedRiderPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("RiderRequest").child(customerId).child("l");
        assignedRiderRefPickupLocationListener = assignedRiderPickupLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() &&!customerId.equals("")) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng pickupLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker = mMap.addMarker(new MarkerOptions()
                            .position(pickupLatLng)
                            .title("طلب مقعد")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_location)));

                    getRouteMarker(pickupLatLng);
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }



    private void getAssignedRiderInfo() {
        bottomSheetDialog.show();
        DatabaseReference mRiderDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(customerId);

        mRiderDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                        Map<String,Object> map=(Map <String, Object>) dataSnapshot.getValue();
                        if(map.get("name")!=null){
                            mRiderName.setText(map.get("name").toString());
                        }
                       if(map.get("phone")!=null){
                            mRiderPhone.setText(map.get("phone").toString());
                        }
                        if(map.get("profileImageUri")!=null){
                           Glide.with(getApplicationContext()).load(map.get("profileImageUri").toString()).placeholder(R.drawable.ic_waiting).into(mRiderProfileImage);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
    }

    private void getAssignedRiderDestination() {
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedRiderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRequest").child("destination");
        assignedRiderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                   String destination  = dataSnapshot.getValue().toString();
                   mRiderDestination.setText(destination);


                } else{
                    mRiderDestination.setText("نسي يقول هيروح فين");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void disconnectDriver() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.removeLocation(userId);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (!isLoginOut) {
            disconnectDriver();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();


        }





    @Override
    protected void onRestart() {
        super.onRestart();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleMapApi();
        mMap.setMyLocationEnabled(true);
    }
    protected synchronized void buildGoogleMapApi() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onLocationChanged(Location location) {
        if (getApplicationContext() != null) {
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("driversAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("driversWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);
            switch (customerId) {
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    getAssignedRiderPickupLocation();
                    break;

            }

        }

    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(DriverHome.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(this, "please provide the permission ", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void getRouteMarker(LatLng pickupLatLng) {
//        Routing routing = new Routing.Builder()
//                .key(String.valueOf(R.string.google_direction_key))
//               .travelMode(AbstractRouting.TravelMode.DRIVING)
//                .withListener(this)
//                .alternativeRoutes(false)
//                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), pickupLatLng)
//                .build();
//        routing.execute();
        if(mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .key(String.valueOf(R.string.google_map_key))
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .build();
            routing.execute();

        }


    }


    private List<Polyline> polyLines;
    private static final int[] COLORS = new int[]{R.color.colorPrimaryDark,R.color.md_deep_orange_A100,R.color.md_deep_orange_100,R.color.colorAccent,R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.i(e.getMessage(), "Routing was cancelled.");

        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoutIndex) {
        if(polyLines.size()>0) {
            for (Polyline poly : polyLines) {
                poly.remove();
            }
        }

        polyLines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polyLines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onRoutingCancelled() {
    }

    private void erasePolyLines(){
        for(Polyline line : polyLines){
            line.remove();
        }
        polyLines.clear();
    }
}
