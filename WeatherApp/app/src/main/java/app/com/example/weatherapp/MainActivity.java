package app.com.example.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Flash on 6/8/2016.
 */

public class MainActivity extends ActionBarActivity {


    private static final String LOG_TAG =null ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ForecastFragment())
                    .commit();

        }
        System.out.println("ok");

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if(id==R.id.action_map){
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }
    public void openPreferredLocationInMap(){
        SharedPreferences sharedPrefs= PreferenceManager.getDefaultSharedPreferences(this);
String location=sharedPrefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        Uri geolocation=Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setData(geolocation);
        if(intent.resolveActivity(getPackageManager())!=null){
            startActivity(intent);
        }
        else{
            Log.d(LOG_TAG,"Couldn't call "+location+"no map");
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */

}
