package kr.touchingbox.mobile.retrofit

import com.hellobiz.mission2.RetrofitInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 레트로핏 객체 생성 클래스
object RetrofitHelper {
    fun newRetrofit(): RetrofitInterface {
        val builder = Retrofit.Builder()
        builder.baseUrl("https://api.vimeo.com/")
        builder.addConverterFactory(GsonConverterFactory.create())

        val retrofit = builder.build().create(RetrofitInterface::class.java)

        return retrofit
    }
}