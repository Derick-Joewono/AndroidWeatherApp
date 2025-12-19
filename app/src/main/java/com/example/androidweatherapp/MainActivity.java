package com.example.androidweatherapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.androidweatherapp.api.ApiService;
import com.example.androidweatherapp.api.RetrofitClient;
import com.example.androidweatherapp.model.GeoLocation;
import com.example.androidweatherapp.model.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "9cd759e77e8c7d969cd10bce622d7bd1";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMM", Locale.getDefault());

    private ImageView imgWeather;
    private AutoCompleteTextView etCity;
    private Button btnSearch;
    private ProgressBar progressBar;
    private TextView tvCity, tvTemp, tvHumidity, tvDesc, tvWind, tvFeels, tvDate, tvLocation;
    private ArrayAdapter<String> cityAdapter;
    private List<String> defaultCities;
    private ApiService apiService;
    private Call<List<GeoLocation>> suggestionCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        apiService = RetrofitClient.getClient().create(ApiService.class);
        initViews();
        setupAutocomplete();
        setupSearchAction();

        // Muat data awal agar UI tidak kosong
        etCity.setText("Jakarta");
        performSearch("Jakarta");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (suggestionCall != null) {
            suggestionCall.cancel();
        }
    }

    private void initViews() {
        etCity = findViewById(R.id.etCity);
        btnSearch = findViewById(R.id.btnSearch);
        tvCity = findViewById(R.id.tvCity);
        tvTemp = findViewById(R.id.tvTemp);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvDesc = findViewById(R.id.tvDesc);
        tvWind = findViewById(R.id.tvWind);
        tvFeels = findViewById(R.id.tvFeels);
        tvDate = findViewById(R.id.tvDate);
        tvLocation = findViewById(R.id.tvLocation);
        imgWeather = findViewById(R.id.imgWeather);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupAutocomplete() {
        defaultCities = new ArrayList<>(Arrays.asList(
                getResources().getStringArray(R.array.city_suggestions)
        ));
        cityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                defaultCities
        );
        etCity.setAdapter(cityAdapter);
        etCity.setThreshold(1);

        etCity.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH;
            boolean isEnterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;
            if (isSearchAction || isEnterPressed) {
                triggerSearch();
                return true;
            }
            return false;
        });

        etCity.setOnItemClickListener((parent, view, position, id) -> triggerSearch());

        etCity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean hasText = !s.toString().trim().isEmpty();
                btnSearch.setEnabled(hasText && progressBar.getVisibility() != View.VISIBLE);
                if (hasText && s.length() >= 2) {
                    fetchSuggestions(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void setupSearchAction() {
        btnSearch.setOnClickListener(v -> triggerSearch());
    }

    private void triggerSearch() {
        String city = etCity.getText().toString().trim();
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter city name", Toast.LENGTH_SHORT).show();
            return;
        }
        etCity.dismissDropDown();
        performSearch(city);
    }

    private void fetchSuggestions(String query) {
        if (suggestionCall != null) {
            suggestionCall.cancel();
        }
        suggestionCall = apiService.searchCity(query, 8, API_KEY);
        suggestionCall.enqueue(new Callback<List<GeoLocation>>() {
            @Override
            public void onResponse(Call<List<GeoLocation>> call, Response<List<GeoLocation>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> names = new ArrayList<>();
                    for (GeoLocation loc : response.body()) {
                        if (loc == null) continue;
                        String display = loc.getName();
                        if (loc.getState() != null && !loc.getState().isEmpty()) {
                            display += ", " + loc.getState();
                        }
                        if (loc.getCountry() != null && !loc.getCountry().isEmpty()) {
                            display += ", " + loc.getCountry();
                        }
                        names.add(display);
                    }
                    cityAdapter.clear();
                    if (!names.isEmpty()) {
                        cityAdapter.addAll(names);
                    } else {
                        cityAdapter.addAll(defaultCities);
                    }
                    cityAdapter.notifyDataSetChanged();
                    if (etCity.hasFocus()) {
                        etCity.showDropDown();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GeoLocation>> call, Throwable t) {
                cityAdapter.clear();
                cityAdapter.addAll(defaultCities);
                cityAdapter.notifyDataSetChanged();
            }
        });
    }

    private void performSearch(String city) {
        showLoading(true);
        Call<WeatherResponse> call = apiService.getWeather(city, API_KEY);
        call.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    bindWeatherData(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(MainActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindWeatherData(WeatherResponse data) {
        double tempValue = data.getMain().getTemp() - 273.15;
        double feelsValue = data.getMain().getFeelsLike() - 273.15;
        String unitLabel = "°C";

        String description = "";
        String iconCode = null;
        if (data.getWeather() != null && !data.getWeather().isEmpty()) {
            description = data.getWeather().get(0).getDescription();
            iconCode = data.getWeather().get(0).getIcon();
        }

        double windKmh = 0;
        if (data.getWind() != null) {
            windKmh = data.getWind().getSpeed() * 3.6;
        }

        tvCity.setText(data.getName());
        tvLocation.setText(data.getName());
        tvDate.setText(dateFormat.format(new Date()));
        tvTemp.setText(String.format(Locale.getDefault(), "%.1f%s", tempValue, unitLabel));
        tvHumidity.setText(String.format(Locale.getDefault(), "%d%%", data.getMain().getHumidity()));
        tvDesc.setText(description.isEmpty() ? "-" : description);
        tvWind.setText(String.format(Locale.getDefault(), "%.1f km/h", windKmh));
        tvFeels.setText(String.format(Locale.getDefault(), "%.1f%s", feelsValue, unitLabel));

        if (iconCode != null) {
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
            Glide.with(this).load(iconUrl).into(imgWeather);
        } else {
            imgWeather.setImageDrawable(null);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSearch.setEnabled(!loading && !etCity.getText().toString().trim().isEmpty());
    }
}
