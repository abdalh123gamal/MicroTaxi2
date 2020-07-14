package com.abdalh.microtaxi.ui;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.model.Driver;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
import androidx.core.content.ContextCompat;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverHome extends AppCompatActivity implements OnMapReadyCallback,RoutingListener {
    private AppBarConfiguration mAppBarConfiguration;
    private GoogleMap mMap;
    // play services
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
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
    private boolean isLoginOut = false;

    private Switch mWorkingSwitch;
    private  LocationManager manager;

    private ImageView mRiderProfileImage;
    private TextView mRiderName,mRiderPhone,mRiderDestination;
    private Button btn_picked_customer;


    private int status=0;
    private String customerId = "", destination;
    private LatLng destinationLatLng, pickupLatLng;
    private float rideDistance;



    private String mName;
    private String mEmail;
    private String mProfileImageUrl;
    private String mCarImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        polyLines = new ArrayList<>();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_driver);
        mapFragment.getMapAsync(this);
        drawerLayout = findViewById(R.id.drawer_layout_driver_home);
        navigationView = findViewById(R.id.driver_home_nav_view);
        mWorkingSwitch=findViewById(R.id.driver_home_switch_run);
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    //check the Gbs is enable or not
                    manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        buildAlertMessageNoGps();
                    }
                    else{
                        connectDriver();
                    }

                }else{
                    disconnectDriver();
                }

            }
        });
        btn_picked_customer=findViewById(R.id.driver_home_btn_picked_customer);
        btn_picked_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch(status){
                    case 1:
                        status=2;
                        erasePolyLines();
                        if(destinationLatLng.latitude!=0.0 && destinationLatLng.longitude!=0.0){
                            getRouteMarker(destinationLatLng);
                        }
                        btn_picked_customer.setText("إتمام الرحلة ؟");

                        break;
                    case 2:
                        recordRide();
                        endRide();
                        btn_picked_customer.setText("قبول الراكب");
                        Toast.makeText(getApplication(), "تمت الرحلة بنجاح", Toast.LENGTH_SHORT).show();


                        break;
                    case 0:
                        Toast.makeText(getApplication(), "لم تتلقي أي طلبات ", Toast.LENGTH_SHORT).show();
                        break;
                }
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

        Snackbar.make(drawerLayout, R.string.main_toast_the_application_is_now_operational, Snackbar.LENGTH_SHORT).show();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_menu_ride_record:
                        Intent mIntent=new Intent(DriverHome.this,HistoryActivity.class);
                        mIntent.putExtra("RiderOrDriver", "Driver");
                        startActivity(mIntent);
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
        setToolbar();
        updateNavHeader();
        getAssignedRider();

    }
    private void buildAlertMessageNoGps() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View gbsDialogView = factory.inflate(R.layout.dialog_layout, null);
        final AlertDialog Dialog = new AlertDialog.Builder(this).create();
        Dialog.setView(gbsDialogView);
        gbsDialogView.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.dismiss();
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        gbsDialogView.findViewById(R.id.no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog.dismiss();
            }
        });
        Dialog.show();
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
        final RatingBar navDriverRating=headerView.findViewById(R.id.header_driver_rating_bar);


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
                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        navDriverRating.setRating(ratingsAvg);
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
                    status = 1;
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedRiderPickupLocation();
                    getAssignedRiderDestination();
                    getAssignedRiderInfo();

                } else{
                 endRide();

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
                     pickupLatLng = new LatLng(locationLat, locationLng);
                    pickupMarker=mMap.addMarker(new MarkerOptions()
                            .position(pickupLatLng)
                            .title("زبون")
                            .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_rider_mark)));
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
        DatabaseReference assignedRiderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRequest");
        assignedRiderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> map= (Map<String,Object>) dataSnapshot.getValue();
                    if (map.get("destination")!= null) {
                        destination = map.get("destination").toString();
                        mRiderDestination.setText(destination);
                    } else {
                        mRiderDestination.setText("نسي يقول هيروح فين");
                    }
                    Double destinationLat = 0.0;
                    Double destinationLng = 0.0;
                    if (map.get("destinationLat") != null) {
                        destinationLat = Double.valueOf(map.get("destinationLat").toString());
                    }
                    if (map.get("destinationLng") != null) {
                        destinationLng = Double.valueOf(map.get("destinationLng").toString());
                        destinationLatLng = new LatLng(destinationLat, destinationLng);
                    }
                }
                else{

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
          }

    private void endRide() {
//        btn_picked_customer.setText("تم الانتهاء ");
        erasePolyLines();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("customerRequest");
        driverRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId="";
        rideDistance=0;
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (assignedRiderRefPickupLocationListener != null) {
            assignedRiderPickupLocationRef.removeEventListener(assignedRiderRefPickupLocationListener);
        }
        bottomSheetDialog.dismiss();
        mRiderProfileImage.setImageResource(R.drawable.ic_rider);
        mRiderName.setText("");
        mRiderPhone.setText("");
        mRiderDestination.setText("");
    }

    private void recordRide(){
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userId).child("history");
        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(customerId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        driverRef.child(requestId).setValue(true);
        riderRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("driver", userId);
        map.put("customer", customerId);
        map.put("rating", 0);
        map.put("timestamp", getCurrentTimestamp());
        map.put("destination", destination);
        map.put("location/from/lat", pickupLatLng.latitude);
        map.put("location/from/lng", pickupLatLng.longitude);
        map.put("location/to/lat", destinationLatLng.latitude);
        map.put("location/to/lng", destinationLatLng.longitude);
        map.put("distance", rideDistance);
        historyRef.child(requestId).updateChildren(map);
    }
    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
      private void connectDriver(){
          checkLocationPermission();
          mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
          mMap.setMyLocationEnabled(true);
      }

      private void disconnectDriver() {
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driversAvailable");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);



        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                mMap.setMyLocationEnabled(true);
            } else {
                checkLocationPermission();
            }
        }
//        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//
//            }else{
//                checkLocationPermission();
//            }
//        }
//        mMap.setMyLocationEnabled(true);
    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for(Location location : locationResult.getLocations()) {
                if(getApplicationContext()!=null){

                if (!customerId.equals("") && mLastLocation != null && location != null) {
                    rideDistance += mLastLocation.distanceTo(location) / 1000;
                }
                mLastLocation = location;
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
            }

        }
    };

    private void checkLocationPermission() {
              if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(this)
                            .setTitle("السماح  للتطبيق ببعض الأوذنات علي جهازك")
                            .setMessage("give permission message")
                            .setPositiveButton("سماح", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    ActivityCompat.requestPermissions(DriverHome.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                                }
                            })
                            .create()
                            .show();
                }
                else{
                    ActivityCompat.requestPermissions(DriverHome.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    Toast.makeText(this, "please provide the permission ", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (!isLoginOut) {
//            disconnectDriver();
//        }
//          }






    private void getRouteToMarker(LatLng pickupLatLang) {

        String uri = String.format(Locale.ENGLISH,"http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                mLastLocation.getLatitude(), mLastLocation.getLongitude(), "Your Location"
                , pickupLatLang.latitude, pickupLatLang.longitude, "Pickup Location");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
    private void getRouteMarker(LatLng pickupLatLng) {
        if(pickupLatLng!=null &&mLastLocation != null){
           Routing routing = new Routing.Builder()
                   .key("AIzaSyCI7ZbHCIyABQbfc8WxuRs5BuI7JgIR2RE")
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                   .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                   .build();
            routing.execute();
        }

    }
    private List<Polyline> polyLines;

    private static final int[] COLORS = new int[]{R.color.spots_dialog_color};
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

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
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
