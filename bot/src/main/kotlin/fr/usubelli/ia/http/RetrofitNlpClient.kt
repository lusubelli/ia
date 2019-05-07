package fr.usubelli.ia.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class RetrofitNlpClient {

    interface NlpApiService {

        @GET("nlp/classify")
        fun classify(@Query("query") text: String):
                Observable<ResponseBody>

    }

    private val objectMapper = jacksonObjectMapper()
    private val nlpApi = Retrofit.Builder()
            .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
            .baseUrl("http://localhost:8181/")
            .build()
            .create(NlpApiService::class.java)

    fun classify(text: String): Observable<Classification> {
        return nlpApi.classify(text).map { responseBody -> objectMapper.readValue(responseBody.string(), Classification::class.java) }
    }

}

data class Classification(
        val id: String,
        val text: String,
        val intentClassification: List<IntentClassification>,
        val nameClassification: List<NameClassification>)

data class IntentClassification(
        val outcome: String,
        val probability: Double)

data class NameClassification(
        val type: String,
        val start: Int,
        val end: Int,
        val probability: Double)