package fr.usubelli.ia.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class RetrofitSttClient {

    interface SttApiService {

        @POST("speechtotext")
        fun speechToText(@Body voice: RequestBody):
                Observable<ResponseBody>

    }

    private val objectMapper = jacksonObjectMapper()
    private val sttApi = Retrofit.Builder()
            .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
            .baseUrl("http://localhost:8080/")
            .build()
            .create(SttApiService::class.java)

    fun speechToText(voice: ByteArray): Observable<String> {
        val request = RequestBody.create(MediaType.parse("application/octet-stream"), voice)
        return sttApi
                .speechToText(request)
                .map { responseBody ->
                    val string = responseBody.string()
                    val response = objectMapper.readValue(string, Response::class.java)
                    response.text
                }
    }

}

data class Response(val text: String?)