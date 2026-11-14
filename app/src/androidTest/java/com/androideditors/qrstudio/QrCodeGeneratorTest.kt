package com.androideditors.qrstudio

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QrCodeGeneratorTest {
    @Test
    fun roundTripTextAndUrl() {
        for (content in listOf("Hello from QR Studio!", "https://example.com")) {
            val image = QrCodeGenerator.generate(content, 512)
            val pixels = IntArray(image.width * image.height)
            image.getPixels(pixels, 0, image.width, 0, 0, image.width, image.height)
            val source = RGBLuminanceSource(image.width, image.height, pixels)
            val result = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source)))
            assertEquals(content, result.text)
        }
    }

    @Test
    fun blankTextIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            QrCodeGenerator.generate("   ")
        }
    }
}
