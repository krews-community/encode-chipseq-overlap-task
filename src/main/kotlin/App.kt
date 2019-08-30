import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.*
import step.*
import util.*
import java.nio.file.*
import util.CmdRunner


fun main(args: Array<String>) = Cli().main(args)

class Cli : CliktCommand() {
    private val outputPrefix: String by option("-outputPrefix", help = "output file name prefix; defaults to 'output'").default("idr")
    private val outDir by option("-outputDir", help = "path to output Directory")
        .path().required()
    private val peak1: Path by option("-peak1", help = "path for Peak file 1.")
            .path().required()
    private val peak2: Path by option("-peak2", help = "path for Peak file 2.")
            .path().required()
    private val pooledPeak: Path by option("-pooledPeak", help = "path for Peak file 1.")
            .path().required()
    private val peakType:String by option("-peakType",help = "Peak File Type").choice("narrowPeak","regionPeak","broadPeak","gappedPeak").required()
    private val keepIrregularChr: Boolean by option("-keep-irregular-chr", help = "Keep reads with non-canonical chromosome names.").flag()
    private val nonamecheck: Boolean by option("-nonamecheck", help = "bedtools intersect -nonamecheck. \\\n" +
            "                       use this if you get bedtools intersect \\\n" +
            "                       naming convenction warnings/errors").flag()
    private val fraglen: Path by option("-fraglen", help = "Fragment Length file").path().required()
    private val taFile: Path by option("-ta", help = "path for TAGALIGN file for frip.")
            .path().required()
    private val chrsz:Path by option("-chrsz",help = "2-col chromosome sizes file.").path().required()

    private val blacklistFile: Path? by option("-blacklist", help = "Blacklist BED file.")
            .path()

    override fun run() {
        val cmdRunner = DefaultCmdRunner()
        cmdRunner.runTask(peak1, peak2, pooledPeak,chrsz, peakType, keepIrregularChr, nonamecheck,fraglen, taFile,  blacklistFile, outDir, outputPrefix)
    }
}

/**
 * Runs pre-processing and bwa for raw input files
 *
 * @param taFiles pooledTa Input
 * @param outDir Output Path
 */
fun CmdRunner.runTask(peak1:Path,peak2:Path,pooledPeak:Path,chrsz:Path,peakType:String,keepIrregularChr:Boolean,nonamecheck:Boolean,fraglen:Path,taFile:Path,blacklistFile:Path?, outDir:Path,outputPrefix:String) {

    overlap(peak1, peak2, pooledPeak,chrsz, peakType, keepIrregularChr,nonamecheck, fraglen, taFile,  blacklistFile, outDir, outputPrefix)
}