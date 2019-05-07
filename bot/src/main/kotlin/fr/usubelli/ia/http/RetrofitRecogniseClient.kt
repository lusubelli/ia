package fr.usubelli.ia.http

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class RetrofitRecogniseClient {

    interface RecogniseApiService {

        @POST("recognise")
        fun recognise(@Body image: RequestBody):
                Observable<ResponseBody>

    }

    private val objectMapper = jacksonObjectMapper()
    private val recogniseApi = Retrofit.Builder()
            .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
            .baseUrl("http://localhost:8383/")
            .build()
            .create(RecogniseApiService::class.java)

    fun recognise(image: ByteArray): Observable<Recognitions> {
        val request = RequestBody.create(MediaType.parse("application/octet-stream"), image)
        return recogniseApi
                .recognise(request)
                .map { responseBody -> objectMapper.readValue<Recognitions>(responseBody.string()) }
    }

}

data class Detections(val detections: List<Detection>)

data class Detection(val x: Int, val y: Int, val width: Int, val height: Int)

data class Recognitions(val recognitions: List<Recognition>)

data class Recognition(val detection: Detection, val matchings: List<Matching>)

data class Matching(val annotation: String, val confidence: Float)
