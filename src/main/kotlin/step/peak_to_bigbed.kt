package step
import util.*
import java.io.File
import java.nio.file.Path

fun CmdRunner.peak_to_bigbed(peak:String, peak_type:String, chrsz:Path,keepIrregularChr:Boolean, out_dir:Path):String {
    var prefix = out_dir.resolve(strip_ext(peak))
    val bigbed = "${prefix}.${peak_type}.bb"
    val as_file = "${prefix}.as"
    val chrsz_tmp = "${prefix}.chrsz.tmp"
    val bigbed_tmp = "${prefix}.bb.tmp"
    val bigbed_tmp2 = "${prefix}.bb.tmp2"
    var bed_param:String
    var as_file_contents:String
    if(peak_type.toLowerCase()=="narrowpeak" || peak_type.toLowerCase()=="regionpeak")
    {
         as_file_contents = """table narrowPeak
        "BED6+4 Peaks of signal enrichment based on pooled, normalized (interpreted) data."
        (
                string chrom; "Reference sequence chromosome or scaffold"
        uint chromStart; "Start position in chromosome"
        uint chromEnd; "End position in chromosome"
        string name; "Name given to a region (preferably unique). Use . if no name is assigned"
        uint score; "Indicates how dark the peak will be displayed in the browser (0-1000) "
        char[1] strand; "+ or - or . for unknown"
        float signalValue; "Measurement of average enrichment for the region"
        float pValue; "Statistical significance of signal value (-log10). Set to -1 if not used."
        float qValue; "Statistical significance with multiple-test correction applied (FDR -log10). Set to -1 if not used."
        int peak; "Point-source called for this peak; 0-based offset from chromStart. Set to -1 if no point-source called."
        )
        """
        bed_param = "-type=bed6+4 -as=${as_file}"

    } else  if(peak_type.toLowerCase()=="broadpeak")
    {
        as_file_contents = """table broadPeak
        "BED6+3 Peaks of signal enrichment based on pooled, normalized (interpreted) data."
        (
                string chrom;        "Reference sequence chromosome or scaffold"
        uint   chromStart;   "Start position in chromosome"
        uint   chromEnd;     "End position in chromosome"
        string name;     "Name given to a region (preferably unique). Use . if no name is assigned."
        uint   score;        "Indicates how dark the peak will be displayed in the browser (0-1000)"
        char[1]   strand;     "+ or - or . for unknown"
        float  signalValue;  "Measurement of average enrichment for the region"
        float  pValue;       "Statistical significance of signal value (-log10). Set to -1 if not used."
        float  qValue;       "Statistical significance with multiple-test correction applied (FDR -log10). Set to -1 if not used."
        )
        """
        bed_param = "-type=bed6+3 -as=${as_file}"

    } else  if(peak_type.toLowerCase()=="gappedpeak")
    {
        as_file_contents = """table gappedPeak
        "This format is used to provide called regions of signal enrichment based on pooled, normalized (interpreted) data where the regions may be spliced or incorporate gaps in the genomic sequence. It is a BED12+3 format."
        (
                string chrom;   "Reference sequence chromosome or scaffold"
        uint chromStart;    "Pseudogene alignment start position"
        uint chromEnd;      "Pseudogene alignment end position"
        string name;        "Name of pseudogene"
        uint score;          "Score of pseudogene with gene (0-1000)"
        char[1] strand;     "+ or - or . for unknown"
        uint thickStart;    "Start of where display should be thick (start codon)"
        uint thickEnd;      "End of where display should be thick (stop codon)"
        uint reserved;      "Always zero for now"
        int blockCount;     "Number of blocks"
        int[blockCount] blockSizes; "Comma separated list of block sizes"
        int[blockCount] chromStarts; "Start positions relative to chromStart"
        float  signalValue;  "Measurement of average enrichment for the region"
        float  pValue;       "Statistical significance of signal value (-log10). Set to -1 if not used."
        float  qValue;       "Statistical significance with multiple-test correction applied (FDR). Set to -1 if not used."
        )
        """
        bed_param = "-type=bed12+3 -as=${as_file}"

    }
    else {
        throw Exception ("Unsupported peak file type ${peak_type}!")
    }

    File(as_file).writeText(as_file_contents)
    var cmd1:String
    if(!keepIrregularChr){
        cmd1 = "cat ${chrsz} | grep -P \"chr[\\dXY]+\\b\" > ${chrsz_tmp}"
    }
    else{
        cmd1 = "cat ${chrsz} > ${chrsz_tmp}"
    }
    this.run(cmd1)

    var cmd2 = "zcat -f ${peak} | LC_COLLATE=C  sort -k1,1 -k2,2n | "
    cmd2 += "awk \'BEGIN{{OFS='\\t'}} {{if ($5>1000) $5=1000; if ($5<0) $5=0; printf \"%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\t%s\\n\",\$1,\$2,\$3,\$4,\$5,\$6,\$7,\$8,\$9,\$10}}\' > ${bigbed_tmp}"
    this.run(cmd2)
    var cmd3 = "bedClip ${bigbed_tmp} ${chrsz_tmp} ${bigbed_tmp2}"
    this.run(cmd3)
    var cmd4 = "bedToBigBed ${bed_param} ${bigbed_tmp2} ${chrsz_tmp} ${bigbed}"
    this.run(cmd4)

    var tmpFiles = mutableListOf<String>()
    tmpFiles.add(as_file)
    tmpFiles.add(bigbed_tmp)
    tmpFiles.add(chrsz_tmp)
    tmpFiles.add(bigbed_tmp2)

    // # remove temporary files
    rm_f(tmpFiles)

    return bigbed
}