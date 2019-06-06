package ru.dpav.weather.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class City(
	@SerializedName("id") @Expose val id: Int,
	@SerializedName("name") @Expose val name: String?,
	@SerializedName("coord") @Expose val coordinates: Coordinates?,
	@SerializedName("main") @Expose val main: MainWeatherInfo?,
	@SerializedName("wind") @Expose val wind: Wind?,
	@Expose val clouds: Clouds?,
	@SerializedName("weather") @Expose val weather: List<Weather>?) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readString(),
		parcel.readParcelable(Coordinates::class.java.classLoader),
		parcel.readParcelable(MainWeatherInfo::class.java.classLoader),
		parcel.readParcelable(Wind::class.java.classLoader),
		parcel.readParcelable(Clouds::class.java.classLoader),
		parcel.createTypedArrayList(Weather)
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeString(name)
		parcel.writeParcelable(coordinates, flags)
		parcel.writeParcelable(main, flags)
		parcel.writeParcelable(wind, flags)
		parcel.writeParcelable(clouds, flags)
		parcel.writeTypedList(weather)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<City> {
		override fun createFromParcel(parcel: Parcel): City {
			return City(parcel)
		}

		override fun newArray(size: Int): Array<City?> {
			return arrayOfNulls(size)
		}
	}
}