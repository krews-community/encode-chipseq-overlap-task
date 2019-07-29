package testutil
import java.nio.file.*

fun getResourcePath(relativePath: String): Path {

    val url = TestCmdRunner::class.java.classLoader.getResource(relativePath)
     return Paths.get(url.toURI())
}

// Resource Directories
val testInputResourcesDir = getResourcePath("test-input-files")
//val testOutputResourcesDir = getResourcePath("test-output-files")


// Test Working Directories
val testDir = Paths.get("/tmp/chipseq-test")!!
val testInputDir = testDir.resolve("input")!!
val testOutputDir = testDir.resolve("output")!!


val PEAK1 = testInputDir.resolve("rep1_align_output.pval0.01.300K.narrowPeak.gz")
val PEAK2 = testInputDir.resolve("rep1_R1_R2_align_output.pval0.01.300K.narrowPeak.gz")
val POOLEDPEAK = testInputDir.resolve("pooled_macs2.pval0.01.300K.narrowPeak.gz")
val TA = testInputDir.resolve("pooled_ta.pooled.tagAlign.gz")
val BL = testInputDir.resolve("hg38.blacklist.bed.gz")
val CHR = testInputDir.resolve("hg38.chrom.sizes.txt")
val FL = testInputDir.resolve("fraglen.txt")