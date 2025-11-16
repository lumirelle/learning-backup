package com.waoap.trafficinfodesk.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 数据集合，提供了操作数据的基本方法
 */
public class Data implements Serializable {
    private final ArrayList<City> cities;

    public Data() {
        cities = new ArrayList<>();
    }

    public boolean containCity(String name) {
        for (City city : cities) {
            if (city.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addCity(City city) {
        cities.add(city);
    }

    public void removeCity(String name) {
        for (City city : cities) {
            if (city.getName().equals(name)) {
                cities.remove(city);
                break;
            }
        }
    }

    public City getCity(String name) {
        for (City city : cities) {
            if (city.getName().equals(name)) {
                return city;
            }
        }
        return null;
    }

    public ArrayList<City> getCities() {
        return cities;
    }

    public ArrayList<String> getCitiesNames() {
        ArrayList<String> names = new ArrayList<>();
        for (City city : cities) {
            names.add(city.getName());
        }
        return names;
    }
}
