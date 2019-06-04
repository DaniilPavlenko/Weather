package ru.dpav.weather.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MainWeatherInfo(
    @SerializedName("temp") @Expose val temp: Float,
    @SerializedName("pressure") @Expose val pressure: Float,
    @SerializedName("humidity") @Expose val humidity: Float): Parcelable {

    constructor(parcel: Parcel): this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readFloat()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(temp)
        parcel.writeFloat(pressure)
        parcel.writeFloat(humidity)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<MainWeatherInfo> {
        override fun createFromParcel(parcel: Parcel): MainWeatherInfo {
            return MainWeatherInfo(parcel)
        }

        override fun newArray(size: Int): Array<MainWeatherInfo?> {
            return arrayOfNulls(size)
        }
    }
}