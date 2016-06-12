package com.huskyyy.dailyweather.model;

import com.google.gson.annotations.SerializedName;
import com.litesuits.orm.db.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.annotation.Table;
import com.litesuits.orm.db.enums.AssignType;

import java.io.Serializable;

/**
 * Created by Wang on 2016/5/11.
 */
@Table("weatherResults")
public class WeatherInfo implements Serializable {

    @PrimaryKey(AssignType.AUTO_INCREMENT) @Column("_id") public int id;

    @Column("cityInfo") public CityInfo cityInfo;
    @Column("todayForcast") @SerializedName("f1") public Forcast todayForcast;
    @Column("secondDayForcast") @SerializedName("f2") public Forcast secondDayForcast;
    @Column("thirdDayForcast") @SerializedName("f3") public Forcast thirdDayForcast;
    @Column("fourthDayForcast") @SerializedName("f4") public Forcast fourthDayForcast;
    @Column("fifthDayForcast") @SerializedName("f5") public Forcast fifthDayForcast;
    @Column("sixthDayForcast") @SerializedName("f6") public Forcast sixthDayForcast;
    @Column("seventhDayForcast") @SerializedName("f7") public Forcast seventhDayForcast;
    @Column("nowState") @SerializedName("now") public NowState nowState;
    @Column("retCode") @SerializedName("ret_code") public int retCode;
    @Column("time") public String time;

    @Column("orderNum") public int orderNum;
}
