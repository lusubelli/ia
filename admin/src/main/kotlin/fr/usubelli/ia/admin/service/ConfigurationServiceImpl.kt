package fr.usubelli.ia.admin.service

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import fr.usubelli.ia.admin.repository.ConfigurationRepository
import fr.usubelli.ia.admin.repository.cache.CacheConfiguration
import fr.usubelli.ia.admin.repository.cache.CacheQr
import java.io.ByteArrayOutputStream
import java.util.*


class ConfigurationServiceImpl(
        private val host: String,
        private val configurationRepository: ConfigurationRepository) :
    ConfigurationService {

    override fun create(configuration: CacheConfiguration): CacheConfiguration {
        return configurationRepository.save(CacheConfiguration(
                configuration.id,
                configuration.name,
                configuration.wifi,
                qrCode(configuration)
        ))
    }

    override fun update(id: String, configuration: CacheConfiguration): CacheConfiguration {
        return configurationRepository.save(CacheConfiguration(
                id,
                configuration.name,
                configuration.wifi,
                qrCode(configuration)
        ))
    }

    override fun load(id: String): CacheConfiguration? {
        return configurationRepository.load(id)
    }

    override fun remove(id: String): CacheConfiguration? {
        return configurationRepository.remove(id)
    }

    override fun load(): List<CacheConfiguration> {
        return configurationRepository.load()
    }

    private fun qrCode(configuration: CacheConfiguration): CacheQr {

        val qrCodeWriter = QRCodeWriter()

        val bitMatrix = qrCodeWriter.encode(
                "URLTO:$host/rest/1.0/configuration/${configuration.id}",
                BarcodeFormat.QR_CODE,
                500,
                500
        )

        val pngOutputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream)

        return CacheQr(
                "data:image/png;base64," + Base64.getEncoder().encodeToString(pngOutputStream.toByteArray()),
                500,
                500)

    }

}
