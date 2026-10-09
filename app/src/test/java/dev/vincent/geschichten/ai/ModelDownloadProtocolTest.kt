package dev.vincent.geschichten.ai

import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ModelDownloadProtocolTest {
    private val size = 2_588_147_712L

    @Test fun fullDownloadAcceptsExpectedSize() {
        val body = ModelDownloadProtocol.validate(200, 0, size, null, size, null)
        assertEquals(0L, body.writeOffset)
        assertEquals(size, body.byteCount)
    }

    @Test fun ignoredRangeRestartsInsteadOfAppendingFullFile() {
        val body = ModelDownloadProtocol.validate(200, 1024, size, null, size, "identity")
        assertEquals(0L, body.writeOffset)
        assertEquals(size, body.byteCount)
    }

    @Test fun resumeRetainsExactOffsetAndRemainingBytes() {
        val offset = 1_400_000_000L
        val body = ModelDownloadProtocol.validate(
            206, offset, size, "bytes $offset-${size - 1}/$size", size - offset, null,
        )
        assertEquals(offset, body.writeOffset)
        assertEquals(size - offset, body.byteCount)
    }

    @Test fun wrongRangeStartCannotCorruptExistingPrefix() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(206, 1024, size, "bytes 0-${size - 1}/$size", size, null)
        }
    }

    @Test fun missingRangeCannotBeAppended() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(206, 1024, size, null, size - 1024, null)
        }
    }

    @Test fun changedRemoteFileIsRejectedBeforeWriting() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(206, 1024, size, "bytes 1024-$size/${size + 1}", size - 1023, null)
        }
    }

    @Test fun compressedBodiesCannotInvalidateByteOffsets() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(200, 0, size, null, size, "gzip")
        }
    }

    @Test fun mismatchedBodyLengthIsRejectedBeforeWriting() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(206, 1024, size, "bytes 1024-${size - 1}/$size", size, null)
        }
    }

    @Test fun chunkedResponseStillHasKnownExpectedSize() {
        val body = ModelDownloadProtocol.validate(206, 1024, size, "bytes 1024-${size - 1}/$size", -1, null)
        assertEquals(size - 1024, body.byteCount)
    }

    @Test fun rangeOutsideSignedLongAndModelIsRejected() {
        assertThrows(IOException::class.java) {
            ModelDownloadProtocol.validate(206, 1024, size, "bytes 1024-999999999999999999999/$size", -1, null)
        }
    }
}
