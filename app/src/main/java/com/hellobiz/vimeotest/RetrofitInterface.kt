package com.hellobiz.mission2

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*


interface RetrofitInterface{
    // Request
    @Headers(*[
        "Authorization: bearer b3e1d76e0feca663e5a49466268d1167",
        "Content-Type: application/json",
        "Accept: application/vnd.vimeo.*+json;version=3.4"])

    @FormUrlEncoded
    @POST("/me/videos")
    fun generateUploadTicket(@FieldMap upload : HashMap<String, String>) : Call<ResponseBody>
}