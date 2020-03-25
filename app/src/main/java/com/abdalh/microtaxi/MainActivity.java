package com.abdalh.microtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
 import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    Toolbar toolbar;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         drawerLayout=findViewById(R.id.drawer);

         Snackbar.make(drawerLayout,R.string.main_toast_the_application_is_now_operational,Snackbar.LENGTH_SHORT).show();

/*
         Toast.makeText(getApplicationContext(),R.string.main_toast_the_application_is_now_operational,Toast.LENGTH_LONG).show();
*/
         navigationView=findViewById(R.id.navigation_view);


         setToolbar();

      // navigationView click item
         navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.drawer_menu_ride_record:
                        Toast.makeText(getApplication(),"لسه سجل رحلاتك فاضي ",Toast.LENGTH_LONG).show();
                        return true;
                    case R.id.drawer_menu_about_micro:
                       startActivity(new Intent(getApplication(),AboutMicroTaxi.class));
                       return true;
                    case R.id.drawer_menu_setting:
                        Toast.makeText(getApplication(),"الاعدادات",Toast.LENGTH_LONG).show();
                        return  true;
                    case R.id.drawer_menu_feedback:
                        startActivity(new Intent(getApplication(),FeedBack.class));
                        return  true;
                    case R.id.drawer_menu_logout:
                        Toast.makeText(getApplication(),"جاري تسجيل الخروج",Toast.LENGTH_LONG).show();
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(getApplicationContext(),ActivitySelectType.class));
                        finish();


/*
                        startActivity(new Intent(getApplication(),SingIn.class));
*/
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


    public void setToolbar(){
        drawerLayout=findViewById(R.id.drawer);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle=new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        //remove name app
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //To replace the drawer indicator icon with your own drawable
       /* drawerToggle.setDrawerIndicatorEnabled(false);
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_list,getApplication().getTheme());
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
        });
*/
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(drawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
