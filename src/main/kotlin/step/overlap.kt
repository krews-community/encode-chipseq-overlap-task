package step

import util.CmdRunner
import java.nio.file.Files
import java.nio.file.Path
import util.*
fun CmdRunner.overlap(peak1: Path, peak2: Path, pooledPeak: Path,chrsz:Path, peakType:String, keepIrregularChr:Boolean,nonamecheck:Boolean, fraglen:Path, taFile: Path,  blacklistFile: Path?, outDir: Path, outputPrefix:String){

    Files.createDirectories(outDir)

    val overlap_peak = overlapFun(outputPrefix,peak1,peak2,pooledPeak,peakType,nonamecheck,outDir)

    var bfilt_idr_peak = blacklist_filter(overlap_peak, blacklistFile, keepIrregularChr, outDir)

    peak_to_bigbed(bfilt_idr_peak, peakType, chrsz, keepIrregularChr, outDir)

    var frip_qc:String
    if(taFile!=null){ // if TAG-ALIGN is given
       // # chip-seq
            //log.info('Shifted FRiP with fragment length...')
            frip_qc = frip_shifted( taFile, bfilt_idr_peak,
               fraglen,   chrsz,outDir)
        //: # atac-seq
            //log.info('FRiP without fragment length...')
           /* frip_qc = frip( taFile, bfilt_idr_peak, outDir)*/

    }
    else {
        frip_qc = "/dev/null"
    }

}
fun CmdRunner.overlapFun(outputPrefix:String, peak1:Path, peak2:Path, peak_pooled:Path, peak_type:String,nonamecheck:Boolean,  out_dir:Path):String{
    Files.createDirectories(out_dir)
    val prefix = out_dir.resolve(outputPrefix)
    val overlap = "${prefix}.overlap"
    val overlap_peak = "${overlap}.${peak_type}.gz"
    val  nonamecheck_param =  if(nonamecheck) { "-nonamecheck" } else  ""
    var awk_param = "{s1=$3-$2; s2=$13-$12; "
    awk_param += "if ((s1!=0 && $21/s1 >= 0.5) || (s2!=0 && $21/s2 >= 0.5)); {print $0}}"
    val cut_param = "1-10"
    val tmp1 = gunzip(peak1.toString(),  out_dir,"tmp1")
    val tmp2 = gunzip(peak2.toString(),  out_dir,"tmp2")
    val tmp_pooled = gunzip(peak_pooled.toString(),  out_dir,"tmp_pooled")

    var cmd1 = "bedtools intersect \\\n" +
            "                    -a ${tmp_pooled} -b ${tmp1} -f 0.50 -F 0.50 -e -u |\\\n" +
            "                    bedtools intersect \\\n" +
            "                    -a stdin -b ${tmp2} -f 0.50 -F 0.50 -e -u | gzip -nc > ${overlap_peak}"

   /* var cmd1 = "intersectBed ${nonamecheck_param} -wo "
    cmd1 += "-a ${tmp_pooled} -b ${tmp1} | "
    cmd1 += "awk \'BEGIN{{FS='\\t';OFS='\\t'}} ${awk_param}\' | "
    cmd1 += "cut -f ${cut_param} | sort | uniq | "
    cmd1 += "intersectBed ${nonamecheck_param} -wo "
    cmd1 += "-a stdin -b ${tmp2} | "
    cmd1 += "awk \'BEGIN{{FS='\\t';OFS='\\t'}} ${awk_param}\' | "
    cmd1 += "cut -f ${cut_param} | sort | uniq | gzip -nc > ${overlap_peak}"*/

    this.run(cmd1)
    rm_f(listOf(tmp1,tmp2,tmp_pooled))
    return overlap_peak

}

fun CmdRunner.rm_f(tmpFiles: List<String>)
{
    val cmd ="rm -f ${tmpFiles.joinToString(" ")}"
    this.run(cmd)
}