package com.example.androidweatherapp;

import android.util.Log;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.bumptech.glide.Glide;




import com.example.androidweatherapp.api.ApiService;
import com.example.androidweatherapp.api.RetrofitClient;
import com.example.androidweatherapp.model.WeatherResponse;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {

    ImageView imgWeather;
    EditText etCity;
    Button btnSearch;
    TextView tvCity, tvTemp, tvHumidity, tvDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvDesc = findViewById(R.id.tvDesc);
        imgWeather = findViewById(R.id.imgWeather);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        btnSearch.setOnClickListener(v -> {

            String city = etCity.getText().toString().trim();

            if (city.isEmpty()) {
                Toast.makeText(this, "Please enter city name", Toast.LENGTH_SHORT).show();
                return;
            }

            Call<WeatherResponse> call = apiService.getWeather(city, "9cd759e77e8c7d969cd10bce622d7bd1");

            call.enqueue(new retrofit2.Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, retrofit2.Response<WeatherResponse> response) {

                    if (response.isSuccessful() && response.body() != null) {

                        WeatherResponse data = response.body();

                        double tempCelsius = data.getMain().getTemp() - 273.15;

                        tvCity.setText("City: " + data.getName());
                        tvTemp.setText("Temperature: " + String.format("%.1f", tempCelsius) + " °C");
                        tvHumidity.setText("Humidity: " + data.getMain().getHumidity() + " %");
                        tvDesc.setText("Weather: " + data.getWeather().get(0).getDescription());

                    } else {
                        Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
                }
            });
        });


        Call<WeatherResponse> call = apiService.getWeather(
                "Jakarta",
                "9cd759e77e8c7d969cd10bce622d7bd1"
        );

        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call,
                                   Response<WeatherResponse> response) {

                if (response.isSuccessful() && response.body() != null) {

                    WeatherResponse data = response.body();

                    double tempCelsius = data.getMain().getTemp() - 273.15;

                    tvCity.setText("City: " + data.getName());
                    tvTemp.setText("Temperature: " +
                            String.format("%.1f", tempCelsius) + " °C");
                    tvHumidity.setText("Humidity: " +
                            data.getMain().getHumidity() + " %");
                    tvDesc.setText("Weather: " +
                            data.getWeather().get(0).getDescription());

                    // 👇 ICON DI SINI
                    String iconCode = data.getWeather().get(0).getIcon();
                    String iconUrl =
                            "https://openweathermap.org/img/wn/" +
                                    iconCode + "@2x.png";

                    Glide.with(MainActivity.this)
                            .load(iconUrl)
                            .into(imgWeather);
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this,
                        "Failed to connect",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
