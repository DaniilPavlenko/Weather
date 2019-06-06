package ru.dpav.weather.api

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Clouds(@SerializedName("all") @Expose internal val all: Int) : Parcelable {

	constructor(parcel: Parcel) : this(parcel.readInt())

	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(all)
	}

	override fun describeContents(): Int {
		return 0
	}

	companion object CREATOR : Parcelable.Creator<Clouds> {
		override fun createFromParcel(parcel: Parcel): Clouds {
			return Clouds(parcel)
		}

		override fun newArray(size: Int): Array<Clouds?> {
			return arrayOfNulls(size)
		}
	}
}