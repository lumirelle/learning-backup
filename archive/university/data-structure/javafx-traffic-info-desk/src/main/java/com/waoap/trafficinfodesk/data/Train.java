package com.waoap.trafficinfodesk.data;

import java.io.Serializable;

public class Train implements Serializable {
    private final String name;

    private final String departureCityName;

    private final String arrivalCityName;

    private final int departureHour;

    private final int departureMinute;

    private final int arrivalDay;

    private final int arrivalHour;

    private final int arrivalMinute;

    private final int costMinutes;

    private final double price;

    /**
     * TODO:近 15 天排班信息，待完善
     */
    private final boolean[] onWork;

    public Train(String name, String departureCityName, String arrivalCityName, String departureTime, String arrivalTime, double price, boolean[] onWork) {
        this.name = name;
        this.departureCityName = departureCityName;
        this.arrivalCityName = arrivalCityName;
        String[] departureDateParts = departureTime.split(":");
        this.departureHour = Integer.parseInt(departureDateParts[0]);
        this.departureMinute = Integer.parseInt(departureDateParts[1]);
        String[] arrivalDateParts = arrivalTime.split(":");
        this.arrivalDay = Integer.parseInt(arrivalDateParts[0]);
        this.arrivalHour = Integer.parseInt(arrivalDateParts[1]);
        this.arrivalMinute = Integer.parseInt(arrivalDateParts[2]);
        int costMinutes = 0;
        // 分钟不足，借 1
        if (arrivalMinute < departureMinute) {
            costMinutes += arrivalMinute - departureMinute + 60;
            // 小时不足，借 1
            if (arrivalHour - 1 < departureHour) {
                costMinutes += (arrivalHour - 1 - departureHour + 24) * 60;
                costMinutes += (arrivalDay - 1) * 24 * 60;
            } else {
                costMinutes += (arrivalHour - 1 - departureHour) * 60;
                costMinutes += arrivalDay * 24 * 60;
            }
        }
        // 分钟足够
        else {
            costMinutes += arrivalMinute - departureMinute;
            if (arrivalHour < departureHour) {
                costMinutes += (arrivalHour - departureHour + 24) * 60;
                costMinutes += (arrivalDay - 1) * 24 * 60;
            } else {
                costMinutes += (arrivalHour - departureHour) * 60;
                costMinutes += arrivalDay * 24 * 60;
            }
        }
        this.costMinutes = costMinutes;
        this.price = price;
        this.onWork = onWork;
    }

    public String getName() {
        return name;
    }

    public String getDepartureCityName() {
        return departureCityName;
    }

    public String getArrivalCityName() {
        return arrivalCityName;
    }

    public int getDepartureHour() {
        return departureHour;
    }

    public int getDepartureMinute() {
        return departureMinute;
    }

    public int getArrivalDay() {
        return arrivalDay;
    }

    public int getArrivalHour() {
        return arrivalHour;
    }

    public int getArrivalMinute() {
        return arrivalMinute;
    }

    public int getCostTime() {
        return costMinutes;
    }

    public double getPrice() {
        return price;
    }

    public boolean isOnWork(int day) {
        return onWork != null && onWork[day];
    }

    @Override
    public String toString() {
        return "{" + name + "|"
                + departureHour + ":" + departureMinute + "->"
                + arrivalDay + ":" + arrivalHour + ":" + arrivalMinute + "|"
                + price + "}";
    }
}
