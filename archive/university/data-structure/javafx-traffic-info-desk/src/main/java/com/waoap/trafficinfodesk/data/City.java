package com.waoap.trafficinfodesk.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class City implements Serializable {
    private final String name;

    private final HashMap<String, ArrayList<Train>> routeCities;

    public City(String name) {
        this.name = name;
        this.routeCities = new HashMap<>();
    }

    public City(String name, HashMap<String, ArrayList<Train>> routeCities) {
        this.name = name;
        this.routeCities = routeCities;
    }

    public String getName() {
        return name;
    }

    public void addRouteCity(String routeCityName) {
        routeCities.put(routeCityName, new ArrayList<>());
    }

    public HashMap<String, ArrayList<Train>> getTrainsOfRouteCities() {
        return routeCities;
    }

    @Override
    public String toString() {
        return "{" + name + "->" + routeCities + "}";
    }
}
