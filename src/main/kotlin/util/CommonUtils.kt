package util

import java.lang.Exception
import java.nio.file.Path


fun strip_ext_bam(bam:String):String{
    val regex = """.(bam|Bam)""".toRegex()
    return regex.replace(bam, "")
}
fun strip_ext(f:String,ext:String=""):String{
    var ex:String
    if (ext=="") {
        ex = get_ext(f)
    } else {
        ex=ext
    }

    val regex = """.((${ex}|${ex})\.gz)""".toRegex();
    return  regex.replace(f, "")
}
fun strip_ext_ta(ta:String):String{
    val regex = """\.[0-9]*(tagAlign|TagAlign|ta|Ta)\.gz$""".toRegex()
    return regex.replace(ta, "")
}

fun human_readable_number(num:Int):String {
    var number = num
    val units:Array<String> = arrayOf("","K","M","G","T","P")
    for(d in units){
        if(Math.abs(number) < 1000)
        {
            return "${number}${d}"
        }
        number = number /1000
    }
    return "${number}E"
}
fun get_ext(f:String):String {
    return f.split(".gz")[0].split(".")[ f.split(".gz")[0].split(".").size-1]
}


fun CmdRunner.gunzip(f:String, out_dir: Path, suffix:String?):String {
    if(!f.endsWith(".gz"))
    {
        throw Exception("Invalid file to gunzip ")
    }
    var gunzipped = out_dir.resolve(strip_ext_gz(f)).toString()
    if(suffix!==null)
    {
        gunzipped += ".${suffix}"//.format(suffix)
    }

    var cmd = "zcat -f ${f} > ${gunzipped}"//.format(f, gunzipped)
    this.run(cmd)
    return gunzipped
}
fun strip_ext_gz(gz:String):String{
    val regex = """.(gz)""".toRegex()
    return regex.replace(gz, "")
}