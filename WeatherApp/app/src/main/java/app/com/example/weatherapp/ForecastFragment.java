package app.com.example.weatherapp;



import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Flash on 6/8/2016.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG=FetchWeatherTask.class.getSimpleName();
    private ArrayAdapter<String> mForecastAdapter;
    public EditText cityId;


    public ForecastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
       FetchWeatherTask weatherTask=new FetchWeatherTask();
        weatherTask.execute("94043");

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        if(id==R.id.action_refresh){

//            FetchWeatherTask weatherTask=new FetchWeatherTask();
//
//            weatherTask.execute("94043");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] forecastArray= {"Today - sunny - 88/63",
                "Tom - sunny - 88/63",
                "wed - sunny - 88/63",
                "thu - sunny - 88/63",
                "fri - sunny - 88/63",
                "sat - sunny - 88/63",
                "sun - sunny - 88/63"
        };
        List<String> weekForecat=new ArrayList<String>(Arrays.asList(forecastArray));
        mForecastAdapter=new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekForecat
        );
        ListView listview= (ListView) rootView.findViewById(R.id.listview_forecast);
        listview.setAdapter(mForecastAdapter);

        Button getWeatherButton= (Button) rootView.findViewById(R.id.button);
         cityId= (EditText) rootView.findViewById(R.id.editText);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityId.getText();
                if(cityId.length()<3||cityId.length()>5||cityId.toString().startsWith(""+0)){
                    cityId.setText(null);
                    Toast.makeText(getActivity(), "Please Enter a valid Postal Code",
                            Toast.LENGTH_LONG).show();
                }
                else {

                    FetchWeatherTask weatherTask=new FetchWeatherTask();
                    weatherTask.execute(cityId.getText().toString());


                }

            }
        });





        return rootView;

    }
    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{



        @Override
        protected String[] doInBackground(String... params) {
            if(params.length==0){
                return null;
            }
             HttpURLConnection urlConnection=null;
             BufferedReader reader=null;
             String forecastJsonStr=null;
            String format="json";
            String units="metric";
            int numDays=7;
            try {
                final String FORECAST_BASE_URL="http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM="q";
                final String FORMAT_PARAM="mode";
                final String UNITS_PARAM="units";
                final String DAYS_PARAM="cnt";

                Uri builtUri=Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM,params[0])
                        .appendQueryParameter(FORMAT_PARAM,format)
                        .appendQueryParameter(UNITS_PARAM,units)
                        .appendQueryParameter(DAYS_PARAM,Integer.toString(numDays))
                        .build();


                URL url=new URL(builtUri.toString());
                Log.v(LOG_TAG,"BuiltUri:"+builtUri.toString());

                urlConnection=(HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream=urlConnection.getInputStream();

                StringBuffer buffer=new StringBuffer();
                if(inputStream==null){
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Forecast JSON String: "+forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
try{
    return getWeatherDataFromJson(forecastJsonStr,numDays);
}
catch (JSONException e){
    e.printStackTrace();
}

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings!=null){
                mForecastAdapter.clear();
            }
            for(String dayForecastString:strings ){
                mForecastAdapter.add(dayForecastString);
            }
        }
    }
    /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.

        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);



        Time dayTime = new Time();
        dayTime.setToNow();

        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v(LOG_TAG, "Forecast entry: " + s);
        }
        return resultStrs;

    }

}
