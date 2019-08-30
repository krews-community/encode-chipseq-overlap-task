package step

import util.CmdRunner
import util.*
import java.nio.file.Path


fun CmdRunner.blacklist_filter(peak:String, blacklist:Path?,keepIrregularChr:Boolean, outDir:Path):String {

    var prefix = outDir.resolve(strip_ext(peak))
    val  peak_ext = get_ext(peak)
    val filtered = "${prefix}.bfilt.${peak_ext}.gz"
    var tmpFiles = mutableListOf<String>()

    var peaklc = this.runCommand("zcat -f ${outDir.resolve(peak)} | wc -l")


    var bllc = 0
    if(blacklist!==null)
    {
        bllc=   this.runCommand("zcat -f ${blacklist} | wc -l")!!.trim().toInt()
    }

    if (peaklc!!.trim().toInt()==0 || blacklist==null || bllc==0) {
        val cmd = "zcat -f ${peak} | gzip -nc > ${filtered}"
        this.run(cmd)
    }
    else {

        //bedtools doesn't support gzipped file so unzipping files
        var tmp1 = gunzip(peak,  outDir,"tmp1")
        var tmp2 = gunzip(blacklist.toString(), outDir,"tmp2")

        var cmd = "bedtools intersect -v -a ${tmp1} -b ${tmp2} | "
        cmd += "awk \'BEGIN{{OFS='\\t'}} "
        cmd += "{{if ($5>1000) $5=1000; print $0}}\' | "
        if(!keepIrregularChr)
        {
            cmd += "grep -P \'chr[\\dXY]+\\b\' | "
        }

        cmd += "gzip -nc > ${filtered}"

        this.run(cmd)

        tmpFiles.add(tmp1)
        tmpFiles.add(tmp2)

        //Delete temp files at the end
        rm_f(tmpFiles)
    }
    return filtered
}
