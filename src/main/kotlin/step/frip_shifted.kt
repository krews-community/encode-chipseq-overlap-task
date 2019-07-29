package step
import util.*
import java.io.File
import java.nio.file.Path


fun CmdRunner.frip(ta: Path, peak:String, out_dir:Path):String {
    val prefix =out_dir.resolve(strip_ext(peak))
    val frip_qc = "${prefix}.frip.qc"
    var lc = this.runCommand("zcat -f ${peak} | wc -l")
    var val1:String?
    if(lc!!.trim().toInt() == 0){
        val1 = "0.0"
    }
    else{
    //bedtools doesn't support gzipped file so unzipping files
    var tmp1 = gunzip(ta.toString(),  out_dir,"tmp1")
    var tmp2 = gunzip(peak,  out_dir,"tmp2")

    var cmd = "bedtools intersect -a ${tmp1} -b ${tmp2} -wa -u | wc -l"

    val1 = this.runCommand(cmd)

    //Delete temp files at the end
    var tmpFiles = mutableListOf<String>()
    tmpFiles.add(tmp1)
        tmpFiles.add(tmp2)
        rm_f(tmpFiles)
    }
    var val2 = this.runCommand("zcat -f ${ta} | wc -l")

    File(frip_qc).writeText((val1!!.trim().toFloat()/val2!!.trim().toFloat()).toString())
    return frip_qc
}
fun CmdRunner.frip_shifted(ta: Path, peak:String,fraglen:Path,chrsz:Path, out_dir:Path):String {
    val prefix =out_dir.resolve(strip_ext(peak))
    val frip_qc = "${prefix}.frip.qc"//.format(prefix)
    val fl = readFraglen(fraglen.toString())
    val half_fraglen = (fl+1)/2

    var lc = this.runCommand("zcat -f ${peak} | wc -l")
    var val1:String?
    if(lc!!.trim().toInt() == 0){
        val1 = "0.0"
    }
    else {

        //bedtools doesn't support gzipped file so unzipping files
        var tmp2 = gunzip(peak,  out_dir,"tmp2")

        var cmd = "bedtools slop -i ${ta} -g ${chrsz} "
        cmd += "-s -l -${half_fraglen} -r ${half_fraglen} | "
        cmd += "awk \'{{if ($2>=0 && $3>=0 && $2<=$3) print $0}}\' | "
        cmd += "bedtools intersect -a stdin -b ${tmp2} "
        cmd += "-wa -u | wc -l"
        val1 = this.runCommand(cmd)
        //Delete temp files at the end
        var tmpFiles = mutableListOf<String>()
        tmpFiles.add(tmp2)
        rm_f(tmpFiles)
    }
    var val2 = this.runCommand("zcat -f ${ta} | wc -l")
    File(frip_qc).writeText((val1!!.toFloat()/val2!!.toFloat()).toString())
    return frip_qc
}

private fun readFraglen(f:String):Int {
    val s = java.io.File(f).readText(Charsets.UTF_8)
    return s!!.trim().toInt()

}