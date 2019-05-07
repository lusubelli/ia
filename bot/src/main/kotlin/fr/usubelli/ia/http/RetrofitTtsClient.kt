package fr.usubelli.ia.http

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RetrofitTtsClient {

    interface TtsApiService {

        @GET("texttospeech")
        fun textToSpeech(@Query("query") text: String):
                Observable<ResponseBody>

    }

    private val ttsApi = Retrofit.Builder()
            .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
            .baseUrl("http://localhost:8282/")
            .build()
            .create(TtsApiService::class.java)

    fun textToSpeech(text: String): Observable<ByteArray> {
        return ttsApi.textToSpeech(text).map { responseBody -> responseBody.bytes() }
    }

}
