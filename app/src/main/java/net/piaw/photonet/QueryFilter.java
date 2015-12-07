package net.piaw.photonet;

import java.util.Date;

/**
 * Created by xiaoqin on 12/3/2015.
 */
public class QueryFilter {
    boolean dateBool;
    boolean placeBool;
    Date dateStart;
    Date dateEnd;
    String cityName;
    String countryName;

    QueryFilter() {
        dateBool = false;
        placeBool = false;
    }

    QueryFilter(Date date_start_val, Date date_end_val) {
        setDate(date_start_val, date_end_val);
    }

    QueryFilter(String city_name, String country_name) {
       setPlace(city_name, country_name);
    }

    QueryFilter(Date date_start_val, Date date_end_val, String city_name, String country_name) {
        setDate(date_start_val, date_end_val);
        setPlace(city_name, country_name);
    }

    void setDate(Date date_start_val, Date date_end_val) {
        dateStart = date_start_val;
        dateEnd = date_end_val;
        dateBool = true;
    }

    void setPlace(String city_name, String country_name) {
        cityName = city_name;
        countryName = country_name;
        placeBool = true;
    }

    boolean queryMatch(Date date_val, String addr) {
        boolean match_date = true;
        if (dateBool &&
                (date_val.getTime() < dateStart.getTime()
                        || date_val.getTime() > dateEnd.getTime())) {
            match_date = false;
        }

        boolean match_place = true;
        if (placeBool && (!addr.contains(cityName) ||
            !addr.contains(countryName))) {
            match_place = false;
        }

        if (match_date && match_place)
            return true;

        return false;
    }
}
