import org.junit.jupiter.api.*
import step.*
import testutil.*
import testutil.cmdRunner
import testutil.setupTest
import org.assertj.core.api.Assertions.*

class OverlapTests {
    @BeforeEach fun setup() = setupTest()
    @AfterEach fun cleanup() = cleanupTest()

     @Test fun `run overlap step `() {

         cmdRunner.overlap(PEAK1,PEAK2, POOLEDPEAK,CHR,"narrowPeak",false,true,FL,TA,BL, testOutputDir,"overlapopt")
         assertThat(testOutputDir.resolve("overlapopt.overlap.narrowPeak.gz")).exists()
         assertThat(testOutputDir.resolve("overlapopt.overlap.bfilt.narrowPeak.gz")).exists()
         assertThat(testOutputDir.resolve("overlapopt.overlap.bfilt.frip.qc")).exists()
         assertThat(testOutputDir.resolve("overlapopt.overlap.bfilt.narrowPeak.bb")).exists()
    }


}