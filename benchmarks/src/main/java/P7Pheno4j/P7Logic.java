package P7Pheno4j;


import tudcomponents.MyGraph;
import tudcomponents.Node;

import java.util.*;

public class P7Logic {
    MyGraph my_g;

    private HashMap<String, String[]> phasingInfoMap;

    public void run(MyGraph g) {
        func1(g);
    }

    private static String variant_id;
    private static String allele_string;
    private static Integer start;
    private static Integer end;
    private static String seq_region_name;
    private static String most_severe_consequence;
    private static Integer strand;
    private static Integer AC;
    private static Double allele_freq;
    private static Integer AN;
    private static Double ExcessHet;//wrong on the github page, says it's int
    private static Double FS;
    private static Double InbreedingCoeff;
    private static Integer MLEAC;
    private static Double MLEAF;
    private static Double MQ;
    private static Double MQRankSum;
    private static Double ReadPosRankSum;
    private static Double VQSLOD;
    private static String Culprit;
    private static Integer gnomad_exomes_AC_AFR;
    private static Integer gnomad_exomes_AC_AMR;
    private static Integer gnomad_exomes_AC_ASJ;
    private static Integer gnomad_exomes_AC_raw;
    private static Double gnomad_exomes_AF_NFE;
    private static Double gnomad_exomes_AF_OTH;
    private static Double gnomad_exomes_AF_raw;
    private static Integer gnomad_exomes_AN_AFR;
    private static Integer gnomad_exomes_AC_EAS;
    private static Integer gnomad_exomes_AC_Female;
    private static Integer gnomad_exomes_AC_OTH;
    private static Integer gnomad_exomes_AC_NFE;
    private static Integer gnomad_exomes_AC_Male;
    private static Integer gnomad_exomes_AC_FIN;
    private static Double gnomad_exomes_AF_AFR;
    private static Double gnomad_exomes_AF_AMR;
    private static Double gnomad_exomes_AF_ASJ;
    private static Double gnomad_exomes_AF_EAS;
    private static Double gnomad_exomes_AF_FIN;
    private static Double gnomad_exomes_AF_Female;
    private static Double gnomad_exomes_AF_Male;
    private static Integer gnomad_exomes_AN_AMR;
    private static Integer gnomad_exomes_AN_ASJ;
    private static Integer gnomad_exomes_AN_EAS;
    private static Integer gnomad_exomes_AN_FIN;
    private static Integer gnomad_exomes_AN_Female;
    private static Integer gnomad_exomes_AN_Male;
    private static Integer gnomad_exomes_AN_NFE;
    private static Integer gnomad_exomes_AN_OTH;
    private static Integer gnomad_exomes_AN_raw;
    private static Integer gnomad_exomes_Hom_AFR;
    private static Integer gnomad_exomes_Hom_AMR;
    private static Integer gnomad_exomes_Hom_ASJ;
    private static Integer gnomad_exomes_Hom_EAS;
    private static Integer gnomad_exomes_Hom_FIN;
    private static Integer gnomad_exomes_Hom_Female;
    private static Integer gnomad_exomes_Hom_Male;
    private static Integer gnomad_exomes_Hom_NFE;
    private static Integer gnomad_exomes_Hom_OTH;
    private static Integer gnomad_exomes_Hom_raw;
    private static Integer gnomad_exomes_Hom;
    private static Integer gnomad_genomes_AC_AFR;
    private static Integer gnomad_genomes_AC_AMR;
    private static Integer gnomad_genomes_AC_ASJ;
    private static Integer gnomad_genomes_AC_raw;
    private static Double gnomad_genomes_AF_NFE;
    private static Double gnomad_genomes_AF_OTH;
    private static Double gnomad_genomes_AF_raw;
    private static Integer gnomad_genomes_AN_AFR;
    private static Integer gnomad_genomes_AC_EAS;
    private static Integer gnomad_genomes_AC_Female;
    private static Integer gnomad_genomes_AC_OTH;
    private static Integer gnomad_genomes_AC_NFE;
    private static Integer gnomad_genomes_AC_Male;
    private static Integer gnomad_genomes_AC_FIN;
    private static Double gnomad_genomes_AF_AFR;
    private static Double gnomad_genomes_AF_AMR;
    private static Double gnomad_genomes_AF_ASJ;
    private static Double gnomad_genomes_AF_EAS;
    private static Double gnomad_genomes_AF_FIN;
    private static Double gnomad_genomes_AF_Female;
    private static Double gnomad_genomes_AF_Male;
    private static Integer gnomad_genomes_AN_AMR;
    private static Integer gnomad_genomes_AN_ASJ;
    private static Integer gnomad_genomes_AN_EAS;
    private static Integer gnomad_genomes_AN_FIN;
    private static Integer gnomad_genomes_AN_Female;
    private static Integer gnomad_genomes_AN_Male;
    private static Integer gnomad_genomes_AN_NFE;
    private static Integer gnomad_genomes_AN_OTH;
    private static Integer gnomad_genomes_AN_raw;
    private static Integer gnomad_genomes_Hom_AFR;
    private static Integer gnomad_genomes_Hom_AMR;
    private static Integer gnomad_genomes_Hom_ASJ;
    private static Integer gnomad_genomes_Hom_EAS;
    private static Integer gnomad_genomes_Hom_FIN;
    private static Integer gnomad_genomes_Hom_Female;
    private static Integer gnomad_genomes_Hom_Male;
    private static Integer gnomad_genomes_Hom_NFE;
    private static Integer gnomad_genomes_Hom_OTH;
    private static Integer gnomad_genomes_Hom_raw;
    private static Integer gnomad_genomes_Hom;
    private static Integer kaviar_AN;
    private static Integer kaviar_AC;
    private static Double kaviar_AF;
    private static Double exac_AF;
    private static Double exac_AF_ADJ;
    private static Integer exac_AF_AFR;
    private static Double exac_AF_AMR;
    private static Integer exac_AF_CONSANGUINEOUS;
    private static Integer exac_AF_EAS;
    private static Integer exac_AF_FEMALE;
    private static Integer exac_AF_FIN;
    private static Double exac_AF_MALE;
    private static Integer exac_AF_NFE;
    private static Integer exac_AF_OTH;
    private static Double exac_AF_POPMAX;
    private static Double exac_AF_SAS;
    private static Integer cadd_phred;
    private static Double cadd_raw;

    public static void func1(MyGraph g) {
        Node n = g.getNodes("label").get(0);

        variant_id = n.properties.get("variant_id");
        allele_string = n.properties.get("allele");
        start = Integer.valueOf(n.properties.get("start"));
        end = Integer.valueOf(n.properties.get("end"));
        seq_region_name = n.properties.get("seq_region");
        most_severe_consequence = n.properties.get("most_severe_consequence");
        strand = Integer.valueOf(n.properties.get("strand"));

        ArrayList<Node> inputs = g.getConnectedNodes(n, "inputs");

        if (!inputs.isEmpty()) {

            Node input = inputs.get(0);
            HashMap<String, String> fields = input.properties;
            InbreedingCoeff = Double.valueOf(fields.get("inbreedingCoeff"));
            allele_freq = Double.valueOf(fields.get("allele_freq"));
            Culprit = fields.get("culprit");
            AN = Integer.valueOf(fields.get("an"));
            AC = Integer.valueOf(fields.get("ac"));
            FS = Double.valueOf(fields.get("fs"));
            MLEAC = Integer.valueOf(fields.get("mleac"));
            ReadPosRankSum = Double.valueOf(fields.get("readposranksum"));
            VQSLOD = Double.valueOf(fields.get("vqslod"));
            MLEAF = Double.valueOf(fields.get("mleaf"));
            ExcessHet = Double.valueOf(fields.get("excesshet"));
            MQRankSum = Double.valueOf(fields.get("mqranksum"));
            MQ = Double.valueOf(fields.get("mq"));
        }

        ArrayList<Node> exomes = g.getConnectedNodes(n, "exomes");

        if (!exomes.isEmpty()) {

            Node gnomad_exome = exomes.get(0);
            HashMap<String, String> fields = gnomad_exome.properties;


            gnomad_exomes_AC_AFR = convertToInteger(fields.get("ac_afr"));
            gnomad_exomes_AC_AMR = convertToInteger(fields.get("ac_amr"));
            gnomad_exomes_AC_ASJ = convertToInteger(fields.get("ac_asj"));
            gnomad_exomes_AC_raw = convertToInteger(fields.get("ac_raw"));
            gnomad_exomes_AF_NFE = convertToDouble(fields.get("af_nfe"));
            gnomad_exomes_AF_OTH = convertToDouble(fields.get("af_oth"));
            gnomad_exomes_AF_raw = convertToDouble(fields.get("af_raw"));
            gnomad_exomes_AN_AFR = convertToInteger(fields.get("an_afr"));
            gnomad_exomes_AC_EAS = convertToInteger(fields.get("ac_eas"));
            gnomad_exomes_AC_Female = convertToInteger(fields.get("ac_female"));
            gnomad_exomes_AC_OTH = convertToInteger(fields.get("ac_oth"));
            gnomad_exomes_AC_NFE = convertToInteger(fields.get("ac_nfe"));
            gnomad_exomes_AC_Male = convertToInteger(fields.get("ac_male"));
            gnomad_exomes_AC_FIN = convertToInteger(fields.get("ac_fin"));
            gnomad_exomes_AF_AFR = convertToDouble(fields.get("af_afr"));
            gnomad_exomes_AF_AMR = convertToDouble(fields.get("af_amr"));
            gnomad_exomes_AF_ASJ = convertToDouble(fields.get("af_asj"));
            gnomad_exomes_AF_EAS = convertToDouble(fields.get("af_eas"));
            gnomad_exomes_AF_FIN = convertToDouble(fields.get("af_fin"));
            gnomad_exomes_AF_Female = convertToDouble(fields.get("af_female"));
            gnomad_exomes_AF_Male = convertToDouble(fields.get("af_male"));
            gnomad_exomes_AN_AMR = convertToInteger(fields.get("an_amr"));
            gnomad_exomes_AN_ASJ = convertToInteger(fields.get("an_asj"));
            gnomad_exomes_AN_EAS = convertToInteger(fields.get("an_eas"));
            gnomad_exomes_AN_FIN = convertToInteger(fields.get("an_fin"));
            gnomad_exomes_AN_Female = convertToInteger(fields.get("an_female"));
            gnomad_exomes_AN_Male = convertToInteger(fields.get("an_male"));
            gnomad_exomes_AN_NFE = convertToInteger(fields.get("an_nfe"));
            gnomad_exomes_AN_OTH = convertToInteger(fields.get("an_oth"));
            gnomad_exomes_AN_raw = convertToInteger(fields.get("an_raw"));
            gnomad_exomes_Hom_AFR = convertToInteger(fields.get("hom_afr"));
            gnomad_exomes_Hom_AMR = convertToInteger(fields.get("hom_amr"));
            gnomad_exomes_Hom_ASJ = convertToInteger(fields.get("hom_asj"));
            gnomad_exomes_Hom_EAS = convertToInteger(fields.get("hom_eas"));
            gnomad_exomes_Hom_FIN = convertToInteger(fields.get("hom_fin"));
            gnomad_exomes_Hom_Female = convertToInteger(fields.get("hom_female"));
            gnomad_exomes_Hom_Male = convertToInteger(fields.get("hom_male"));
            gnomad_exomes_Hom_NFE = convertToInteger(fields.get("hom_nfe"));
            gnomad_exomes_Hom_OTH = convertToInteger(fields.get("hom_oth"));
            gnomad_exomes_Hom_raw = convertToInteger(fields.get("hom_raw"));
            gnomad_exomes_Hom = convertToInteger(fields.get("hom"));
        }


        ArrayList<Node> genomes = g.getConnectedNodes(n, "genomes");

        if (!genomes.isEmpty()) {
            Node gnomad_genome = genomes.get(0);
            HashMap<String, String> fields = gnomad_genome.properties;

            gnomad_genomes_AC_AFR = convertToInteger(fields.get("ac_afr"));
            gnomad_genomes_AC_AMR = convertToInteger(fields.get("ac_amr"));
            gnomad_genomes_AC_ASJ = convertToInteger(fields.get("ac_asj"));
            gnomad_genomes_AC_raw = convertToInteger(fields.get("ac_raw"));
            gnomad_genomes_AF_NFE = convertToDouble(fields.get("af_nfe"));
            gnomad_genomes_AF_OTH = convertToDouble(fields.get("af_oth"));
            gnomad_genomes_AF_raw = convertToDouble(fields.get("af_raw"));
            gnomad_genomes_AN_AFR = convertToInteger(fields.get("an_afr"));
            gnomad_genomes_AC_EAS = convertToInteger(fields.get("ac_eas"));
            gnomad_genomes_AC_Female = convertToInteger(fields.get("ac_female"));
            gnomad_genomes_AC_OTH = convertToInteger(fields.get("ac_oth"));
            gnomad_genomes_AC_NFE = convertToInteger(fields.get("ac_nfe"));
            gnomad_genomes_AC_Male = convertToInteger(fields.get("ac_male"));
            gnomad_genomes_AC_FIN = convertToInteger(fields.get("ac_fin"));
            gnomad_genomes_AF_AFR = convertToDouble(fields.get("af_afr"));
            gnomad_genomes_AF_AMR = convertToDouble(fields.get("af_amr"));
            gnomad_genomes_AF_ASJ = convertToDouble(fields.get("af_asj"));
            gnomad_genomes_AF_EAS = convertToDouble(fields.get("af_eas"));
            gnomad_genomes_AF_FIN = convertToDouble(fields.get("af_fin"));
            gnomad_genomes_AF_Female = convertToDouble(fields.get("af_female"));
            gnomad_genomes_AF_Male = convertToDouble(fields.get("af_male"));
            gnomad_genomes_AN_AMR = convertToInteger(fields.get("an_amr"));
            gnomad_genomes_AN_ASJ = convertToInteger(fields.get("an_asj"));
            gnomad_genomes_AN_EAS = convertToInteger(fields.get("an_eas"));
            gnomad_genomes_AN_FIN = convertToInteger(fields.get("an_fin"));
            gnomad_genomes_AN_Female = convertToInteger(fields.get("an_female"));
            gnomad_genomes_AN_Male = convertToInteger(fields.get("an_male"));
            gnomad_genomes_AN_NFE = convertToInteger(fields.get("an_nfe"));
            gnomad_genomes_AN_OTH = convertToInteger(fields.get("an_oth"));
            gnomad_genomes_AN_raw = convertToInteger(fields.get("an_raw"));
            gnomad_genomes_Hom_AFR = convertToInteger(fields.get("hom_afr"));
            gnomad_genomes_Hom_AMR = convertToInteger(fields.get("hom_amr"));
            gnomad_genomes_Hom_ASJ = convertToInteger(fields.get("hom_asj"));
            gnomad_genomes_Hom_EAS = convertToInteger(fields.get("hom_eas"));
            gnomad_genomes_Hom_FIN = convertToInteger(fields.get("hom_fin"));
            gnomad_genomes_Hom_Female = convertToInteger(fields.get("hom_female"));
            gnomad_genomes_Hom_Male = convertToInteger(fields.get("hom_male"));
            gnomad_genomes_Hom_NFE = convertToInteger(fields.get("hom_nfe"));
            gnomad_genomes_Hom_OTH = convertToInteger(fields.get("hom_oth"));
            gnomad_genomes_Hom_raw = convertToInteger(fields.get("hom_raw"));
            gnomad_genomes_Hom = convertToInteger(fields.get("hom"));
        }


        ArrayList<Node> kaviars = g.getConnectedNodes(n, "kaviars");

        if (!kaviars.isEmpty()) {

            Node kaviar = kaviars.get(0);
            HashMap<String, String> fields = kaviar.properties;

            kaviar_AN = Integer.valueOf(fields.get("an"));
            kaviar_AC = Integer.valueOf(fields.get("ac"));
            kaviar_AF = Double.valueOf(fields.get("af"));
        }

    }

    private static Integer convertToInteger(String string) {
        try {

            return Integer.valueOf(string);

        } catch (Exception e) {
            return null;
        }
    }

    private static Double convertToDouble(String string) {
        try {

            return Double.valueOf(string);

        } catch (Exception e) {
            return null;
        }
    }

}