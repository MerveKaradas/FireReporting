package com.mervekaradas.yanginvardemo.model;

import java.util.List;

public class WeatherResponse {
    private Main main;
    private String name;
    private List<Weather> weather;

    public Main getMain() {
        return main;
    }

    public String getName() {
        return name;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public class Main {
        private float temp;

        public float getTemp() {
            return temp;
        }
    }

    public class Weather {
        private String description;

        public String getDescription() {
            return description;
        }
    }
}
