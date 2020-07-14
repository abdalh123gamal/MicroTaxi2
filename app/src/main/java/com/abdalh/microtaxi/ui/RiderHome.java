package com.abdalh.microtaxi.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.abdalh.microtaxi.R;
import com.abdalh.microtaxi.model.Rider;
import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.NavigationUI;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import dmax.dialog.SpotsDialog;

public class RiderHome extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener , RoutingListener {

    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private SupportMapFragment supportMapFragment;
    private Location mLastLocation;
    private LatLng pickupLocation;
    private Marker pickupMarker;
    private String mName;
    private String mEmail;
    private String mProfileImageUrl;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private BottomSheetDialog bottomSheetDialog;
    private ImageView mDriverProfileImage;
    private TextView mDriverName, mDriverPhone, mDriverCar;
    private String destination;
    private LatLng destinationLatLng;
    private LocationManager manager;
    private Button btn_request;
    boolean requestBol = false;
    private int number;
    private RatingBar ratingBar_driver;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);
        drawerLayout = findViewById(R.id.drawer_layout_rider_home);
        navigationView = findViewById(R.id.rider_home_nav_view);
        polyLines = new ArrayList<>();
        destinationLatLng = new LatLng(0.0,0.0);
        //check the Gbs is enable or not
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
        setToolbar();
        updateNavHeaderInfo();

        //swipe up the info sheet
        ImageView show_bottom_sheet = findViewById(R.id.rider_home_iv_info_driver_show_bottom_sheet);
        show_bottom_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.show();
            }
        });
        bottomSheetDialog = new BottomSheetDialog(RiderHome.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_sheet_driver, (LinearLayout) findViewById(R.id.bottom_sheet_driver_container));
        bottomSheetDialog.setContentView(bottomSheetView);
        mDriverProfileImage = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_iv_profile);
        mDriverName = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_name);
        mDriverPhone = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_phone);
        mDriverCar = bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_tv_car_type);
        ratingBar_driver=bottomSheetView.findViewById(R.id.bottom_sheet_driver_container_ratingBar);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG,Place.Field.ID, Place.Field.NAME));
        // Initialize Places.
        Places.initialize(getApplicationContext(),getString(R.string.google_maps_key));
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();

//               final LatLng searchLocation = place.getLatLng();
//               mMap.animateCamera(CameraUpdateFactory.newLatLng(searchLocation));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        btn_request = findViewById(R.id.rider_home_btn_request);
        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    if (requestBol) {
                        endRide();

                    } else {
                    requestBol=true;
                    String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref=FirebaseDatabase.getInstance().getReference("RiderRequest");
                    GeoFire geoFire=new GeoFire(ref);
                    geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                    pickupLocation=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
                    pickupMarker=mMap.addMarker(new MarkerOptions().position(pickupLocation)
                            .title("مكانك")
                            .icon(bitmapDescriptorFromVector(getApplicationContext(),R.drawable.ic_rider_mark)));
                    btn_request.setText("جاري البحث...");
                    getClosestDriver();
                }

                } else {
                    buildAlertMessageNoGps();
                }
            }
        });

        try {
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map_rider);
            supportMapFragment.getMapAsync(this);
        } catch (Exception e) {
            Log.i("MapException", e.getMessage());
        }

        Snackbar.make(drawerLayout, R.string.main_toast_the_application_is_now_operational, Snackbar.LENGTH_SHORT).show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RiderHome.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
        } else {
            supportMapFragment.getMapAsync(this);
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.drawer_menu_ride_record:
                        Intent mIntent=new Intent(RiderHome.this,HistoryActivity.class);
                        mIntent.putExtra("RiderOrDriver", "Rider");
                        startActivity(mIntent);
                        return true;
                    case R.id.drawer_menu_about_micro:
                        startActivity(new Intent(getApplication(), AboutMicroTaxi.class));
                        return true;
                    case R.id.drawer_menu_setting:
                        startActivity(new Intent(getApplicationContext(), SettingRiderInfo.class));
                        return true;
                    case R.id.drawer_menu_feedback:
                        startActivity(new Intent(getApplication(), FeedBack.class));
                        return true;
                    case R.id.drawer_menu_logout:
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
                        intent.putExtra(Intent.EXTRA_TEXT, " https://MicroTaxi.Met.com/ مرحبًا ، قم بتنزيل هذا التطبيق ! ");
                        startActivity(Intent.createChooser(intent, "choose one"));
                        return true;
                }
                return true;
            }
        });
    }

    private void buildAlertMessageNoGps() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View gbsDialogView = factory.inflate(R.layout.dialog_layout, null);
        final AlertDialog Dialog = new AlertDialog.Builder(this).create();
        Dialog.setView(gbsDialogView);
        gbsDialogView.findViewById(R.id.yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                Dialog.dismiss();
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

    public void updateNavHeaderInfo() {
        navigationView = findViewById(R.id.rider_home_nav_view);
        View headerView = navigationView.getHeaderView(0);
        final ImageView navImage = headerView.findViewById(R.id.header_rider_iv_profile);
        final TextView navUserName = headerView.findViewById(R.id.header_rider_tv_name);
        final TextView navUserEmail = headerView.findViewById(R.id.header_rider_tv_email);

        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mRiderDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(userID);
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

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    // البحث عن أقرب سائق للراكب
    private int radius = -1;
    private boolean driverFound = false;
    GeoQuery geoQuery;
    private String driverFoundID;
    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol) {
                    driverFound = true;
                    driverFoundID = key;

                    DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
                    String riderID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap map = new HashMap();
                    map.put("customerRideId",riderID);
                    map.put("destination",destination);
                    map.put("destinationLat",destinationLatLng.latitude);
                    map.put("destinationLng",destinationLatLng.longitude);
                    driverRef.updateChildren(map);
                    final AlertDialog dialog = new SpotsDialog(RiderHome.this, "جاري التواصل مع أقرب سائق متاح", R.style.CustomDialog);
                    dialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                        }
                    }, 2000);

                    getDriverLocation();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAssignedDriverInfo();

                        }
                    }, 2000);
                    getHasRideEnded();
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
                if (!driverFound) {
                    if (radius != 5) {
                        radius++;
                        getClosestDriver();
                    } else {
                        btn_request.setText("الغاء البحث ");
                        final AlertDialog dialog = new SpotsDialog(RiderHome.this, " نأسف لا يوجد سائقين متاحين الان", R.style.CustomDialog);
                        dialog.show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();

                            }
                        }, 2000);
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
    private DatabaseReference driverLocationRef;
    private ValueEventListener driverLocationRefListener;

    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("driversWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && requestBol) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLng = 0;
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLng = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng driverLngLat = new LatLng(locationLat, locationLng);

                    if (MDriverMarker != null) {
                        MDriverMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickupLocation.latitude);
                    loc1.setLongitude(pickupLocation.longitude);
                    Location loc2 = new Location("");
                    loc1.setLatitude(driverLngLat.latitude);
                    loc1.setLongitude(driverLngLat.longitude);
                    float distance = loc1.distanceTo(loc2)/1000;
                    if (distance < 100) {
                        btn_request.setText("السائق بالقرب منك ");
                    } else {
                        btn_request.setText("تحب تكمل الرحلة ولا نلغيها ؟");
                    }
                    {
                    }
                    MDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLngLat).title("ميكروباص").icon(bitmapDescriptorFromVector(getApplication(), R.drawable.ic_bus2)));
                    getRouteMarker(destinationLatLng);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedDriverInfo() {
        bottomSheetDialog.show();
        DatabaseReference mDriverDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);

        mDriverDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("name") != null) {
                        mDriverName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mDriverPhone.setText(map.get("phone").toString());
                    }
                    if (map.get("carType") != null) {
                        mDriverCar.setText(map.get("carType").toString());
                    }
                    if (map.get("profileImageUri") != null) {
                        Glide.with(getApplicationContext()).load(map.get("profileImageUri").toString()).placeholder(R.drawable.ic_waiting).into(mDriverProfileImage);
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
                        ratingBar_driver.setRating(ratingsAvg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    // اكتملت الرحلة
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRedListener;
    private void getHasRideEnded() {
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRedListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                }
                else {
                    endRide();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }
    private void endRide() {
        requestBol = false;
        geoQuery.removeAllListeners();
        if(driverLocationRefListener!=null){
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }
        if(driverLocationRefListener!=null){
            driveHasEndedRef.removeEventListener(driveHasEndedRedListener);
        }
        if (driverFoundID != null) {
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;
        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("RiderRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
        if (pickupMarker != null) {
            pickupMarker.remove();
        }
        if (MDriverMarker != null) {
            MDriverMarker.remove();
        }
        erasePolyLines();
        btn_request.setText("بحث");
        Toast.makeText(getApplication(), "تمت الرحلة بنجاح", Toast.LENGTH_SHORT).show();
//        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("حمدالله بالسلامه نتمني أن تكون الرحلة خفيفه علي قلب حضرتك بعد اذنك لو تكرمت ادخل سجل الرحلات قيم السائق من فضلك")
//                .setCancelable(false)
//                .setPositiveButton("تقيم", new DialogInterface.OnClickListener() {
//                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        Intent mIntent=new Intent(RiderHome.this,HistoryActivity.class);
//                        mIntent.putExtra("RiderOrDriver", "Rider");
//                        startActivity(mIntent);
//                    }
//                })
//                .setNegativeButton("ابقي فكرني مرة تانية", new DialogInterface.OnClickListener() {
//                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
//                        dialog.cancel();
//                    }
//                });
//        final AlertDialog alert = builder.create();
//        alert.show();
        mDriverProfileImage.setImageResource(R.drawable.ic_driver_svg);
        mDriverName.setText("");
        mDriverPhone.setText("");
        mDriverCar.setText("");
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
              //After instantiating your ActionBarDrawerToggle
                drawerToggle.setDrawerIndicatorEnabled(false);
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_toggle_menu,getApplicationContext().getTheme());
                drawerToggle.setHomeAsUpIndicator(drawable);
                drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            drawerLayout.openDrawer(GravityCompat.START);
                        }
                    }
            }); }

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
    View mapView;

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
        if(!getDriversAroundStarted){
            getDriversAround();
        }


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



    private void getRouteToMarker(LatLng pickupLatLang) {

        String uri = String.format(Locale.ENGLISH,"http://maps.google.com/maps?saddr=%f,%f(%s)&daddr=%f,%f (%s)",
                mLastLocation.getLatitude(), mLastLocation.getLongitude(), "Your Location"
                , pickupLatLang.latitude, pickupLatLang.longitude, "Pickup Location");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }
    private void getRouteMarker(LatLng pickupLatLng) {
        if(mLastLocation != null){
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


    boolean getDriversAroundStarted = false;
    List<Marker> markers = new ArrayList<Marker>();
    private void getDriversAround(){
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driversAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLongitude(), mLastLocation.getLatitude()), 999999999);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key))
                        return;
                }
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title(key).icon(bitmapDescriptorFromVector(getApplication(), R.drawable.ic_bus2)));
                mDriverMarker.setTag(key);

                markers.add(mDriverMarker);
            }
            @Override
            public void onKeyExited(String key) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.remove();
                        markers.remove(markerIt);
                    }
                }
            }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }
            @Override
            public void onGeoQueryReady() {
            }
            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}


