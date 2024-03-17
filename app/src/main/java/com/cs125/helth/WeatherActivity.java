import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class WeatherActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // make the weather API call, first get the location
                    try {
                        URL url = new URL("https://api.weather.gov/points/" + latitude + "," + longitude);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");

                        int responseCode = connection.getResponseCode();
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String inputLine;
                            StringBuilder response = new StringBuilder();

                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            String jsonResponse = response.toString();
                            // Parse JSON response and extract the forecast endpoint here

                            int forecastIndex = jsonResponse.indexOf("forecastHourly");
                            int forecastEnd = jsonResponse.indexOf("forecast/hourly");
                            String newURL = jsonResponse.substring(forecastIndex + 18, forecastEnd + 15);
                            URL forecastURL = new URL(newURL);

                            try {
                                HttpURLConnection connection = (HttpURLConnection) newURL.openConnection();
                                connection.setRequestMethod("GET");

                                int responseCode = connection.getResponseCode();
                                if (responseCode == HttpURLConnection.HTTP_OK) {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                                    String inputLine;
                                    StringBuilder response = new StringBuilder();

                                    while ((inputLine = in.readLine()) != null) {
                                        response.append(inputLine);
                                    }
                                    in.close();

                                    String jsonResponse = response.toString();

                                    int weatherStart = jsonResponse.indexOf("\"number\": 1,");

                                    int tempStart = jsonResponse.indexOf("\"temperature\":", weatherStart);
                                    int tempEnd = jsonResponse.indexOf("\"temperatureUnit\":", weatherStart);
                                    int temperature = Integer.parseInt(jsonResponse.substring(tempStart + 15, tempEnd - 17));

                                    int fcStart = jsonResponse.indexOf("\"shortForecast\":", weatherStart);
                                    int fcEnd = jsonResponse.indexOf("\"detailedForecast\":", weatherStart);
                                    String forecast = jsonResponse.substring(fcStart + 18, fcEnd - 18);

                                    // this should hold the temperature (deg F) and forecast (Sunny, Cloudy, etc)
                                    System.out.println(temperature);
                                    System.out.println(forecast);

                                } else {
                                    System.out.println("Failed to fetch forecast. Error code: " + responseCode);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        } else {
                            System.out.println("Failed to fetch forecast. Error code: " + responseCode);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            requestLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30000); // Update location every 30 seconds

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
}