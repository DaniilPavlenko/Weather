package ru.dpav.weather.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Weather(
	@SerializedName("id") @Expose val id: Int,
	@SerializedName("icon") @Expose val icon: String) : Parcelable {

	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readString()
	)

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(id)
		parcel.writeString(icon)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Weather> {
		override fun createFromParcel(parcel: Parcel): Weather {
			return Weather(parcel)
		}

		override fun newArray(size: Int): Array<Weather?> {
			return arrayOfNulls(size)
		}
	}
}