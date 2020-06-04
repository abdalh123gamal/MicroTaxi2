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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.model.Rider;
import com.bumptech.glide.Glide;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;

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

    private String mName;
    private String mEmail;
    private String mProfileImageUrl;

    private MaterialAnimatedSwitch location_switch;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;

    private  BottomSheetDialog bottomSheetDialog;
    private ImageView mDriverProfileImage;
    private TextView mDriverName,mDriverPhone,mDriverCar;

    private Spinner spLine;
    CardView DialogSearch;
    ConstraintLayout layoutContainer;
    String destination ;
    private LocationManager manager;
    private Button btn_request;

    boolean requestBol= false;
    private int number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);

        drawerLayout=findViewById(R.id.drawer_layout_rider_home);
        navigationView=findViewById(R.id.rider_home_nav_view);
        layoutContainer=findViewById(R.id.rider_home_layout_container);
        DialogSearch=findViewById(R.id.rider_home_view_search_and_selected_Dialog);
        //check the Gbs is enable or not
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }



        setToolbar();
        updateNavHeaderInfo();

        DialogSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutContainer.setVisibility(View.VISIBLE);

            }
        });
        ImageView show_bottom_sheet=findViewById(R.id.rider_home_iv_info_driver_show_bottom_sheet);
        show_bottom_sheet.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 bottomSheetDialog.show();
             }
         });

        bottomSheetDialog=new BottomSheetDialog(RiderHome.this,R.style.BottomSheetDialogTheme);
        View bottomSheetView= LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_sheet_driver, (LinearLayout)findViewById(R.id.bottom_sheet_driver_container));
        bottomSheetDialog.setContentView(bottomSheetView);
        mDriverProfileImage = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_iv_profile);
        mDriverName = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_name);
        mDriverPhone = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_phone);
        mDriverCar = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_car_type);

        // Array of choices
        String lines [] =getResources().getStringArray(R.array.lines);
        // Selection of the spinner
        final Spinner spinner =findViewById(R.id.rider_home_sp_lines);
        // Application of the Array to the Spinner
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, lines);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String item_position = String.valueOf(position);
                destination = spinner.getSelectedItem().toString();
                int positionInt = Integer.valueOf(item_position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

//
//                listener=mLineDatabase.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        for (DataSnapshot lineSnapshot: dataSnapshot.getChildren()) {
//                            spinnerListLines.add(lineSnapshot.getValue().toString());
//
//                        }
//                        adapter.notifyDataSetChanged();
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                        Toast.makeText(RiderHome.this,"aaa",Toast.LENGTH_LONG).show();
//
//                    }
//                });



        btn_request=findViewById(R.id.rider_home_btn_request);

        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if(requestBol){
                        requestBol=false;
                        geoQuery.removeAllListeners();
                        if(driverLocationRefListener != null){
                            driverLocationRef.removeEventListener(driverLocationRefListener);
                        }
                        if(driverFoundID!=null){
                            DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
                            driverRef.removeValue();
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
                        if(MDriverMarker!=null)
                        {
                            MDriverMarker.remove();
                        }
                        btn_request.setText("بحث");

                        mDriverProfileImage.setImageResource(R.drawable.ic_driver_svg);
                        mDriverName.setText("");
                        mDriverPhone.setText("");
                        mDriverCar.setText("");


                    } else{
                        requestBol=true;
                        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RiderRequest");
                        GeoFire geoFire=new GeoFire(ref);
                        geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                        pickupLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                        pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation).title("مكانك").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin_location)));

                        btn_request.setText("جاري البحث...");

                        getClosestDriver();
                    }
                }
                else {
                    buildAlertMessageNoGps();
                }


            }


        });

        




        try {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_rider);
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
                        startActivity(new Intent(getApplicationContext(), SettingRiderInfo.class));
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
                        intent.putExtra(Intent.EXTRA_TEXT, " https://MicroTaxi.Met.com/ مرحبًا ، قم بتنزيل هذا التطبيق ! ");
                        startActivity(Intent.createChooser(intent, "choose one"));

                        return true;

                }
                return true;
            }
        });

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("يعني أنت داخل تطبيق عشان تحدد ال location بتاعك واحنا مكرمين هنحددلك الطريق اللي يوصلك لميكروباص يوصلك مكانك انت واللي معاك في المكان اللي انت عاوزه وحضرتك مش مشغل ال Gbs  ؟")
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

    public void updateNavHeaderInfo(){
        navigationView=findViewById(R.id.rider_home_nav_view);
        View headerView=navigationView.getHeaderView(0);
        final ImageView navImage=headerView.findViewById(R.id.header_rider_iv_profile);
        final TextView navUserName=headerView.findViewById(R.id.header_rider_tv_name);
        final TextView navUserEmail=headerView.findViewById(R.id.header_rider_tv_email);

         String userID=FirebaseAuth.getInstance().getCurrentUser().getUid();
         DatabaseReference mRiderDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(userID);
         mRiderDatabase.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                     Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                     if (map.get("name") != null) {
                         mName = map.get("name").toString();
                         navUserName.setText(mName);
                     }
                     if (map.get("email") != null) {
                         mEmail = map.get("email").toString();
                         navUserEmail.setText(mEmail);
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
                    DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
                    String riderID=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map=new HashMap();
                    map.put("customerRideId",riderID);
                    map.put("destination",destination);
                    driverRef.child("customerRideId").setValue(riderID);
                    driverRef.child("destination").setValue(destination);

                    final AlertDialog dialog = new SpotsDialog(RiderHome.this,"جاري البحث علي سائق قريب",R.style.CustomDialog);
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();

                        }
                    },2000);

                    getDriverLocation();
                    getAssignedDriverInfo();

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
                {
                    if(radius != 5){
                        radius++;
                        getClosestDriver();
                    }else {
                        btn_request.setText("الغاء البحث ");

                        final AlertDialog dialog = new SpotsDialog(RiderHome.this," نأسف لا يوجد سائقين متاحين الان",R.style.CustomDialog);


                        dialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                            }
                        },2000);
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    // عرض مكان السائق علي الخريطة وعرض بياناته

    private Marker MDriverMarker;
    private DatabaseReference driverLocationRef ;
    private ValueEventListener driverLocationRefListener;
    private void getDriverLocation() {
        driverLocationRef=FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&requestBol) {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng =0;

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
                        btn_request.setText("السائق بالقرب منك ");
                    }
                    else{
                        btn_request.setText(" تحب تكمل الرحلة ولا نلغيها ؟ ");

                    }
                    {

                    }

                     MDriverMarker=mMap.addMarker(new MarkerOptions().position(driverLngLat).title("ميكروباص").icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_bus)));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    private void getAssignedDriverInfo() {
        bottomSheetDialog.show();
        DatabaseReference mDriverDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);

        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map=(Map <String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mDriverName.setText(map.get("name").toString());
                    }
                    if(map.get("phone")!=null){
                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if(map.get("carType")!=null){
                        mDriverCar.setText(map.get("carType").toString());
                    }
                    if(map.get("profileImageUri")!=null){
                        Glide.with(getApplicationContext()).load(map.get("profileImageUri").toString()).placeholder(R.drawable.ic_waiting).into(mDriverProfileImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


        private void setToolbar() {
              Toolbar toolbar = findViewById(R.id.toolbar_rider_home);
              setSupportActionBar(toolbar);
              drawerLayout = findViewById(R.id.drawer_layout_rider_home);
              navigationView = findViewById(R.id.rider_home_nav_view);

              drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
              drawerLayout.addDrawerListener(drawerToggle);
              drawerToggle.syncState();
              //remove name app
              getSupportActionBar().setDisplayShowTitleEnabled(false);


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


        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


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
                    supportMapFragment.getMapAsync(this);
                } else {
                    Toast.makeText(this,"please provide the permission ",Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

}
