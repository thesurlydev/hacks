package com.digitalsanctum.rir;

import com.digitalsanctum.rir.model.Asn;
import com.digitalsanctum.rir.model.Poc;

import java.util.ArrayList;
import java.util.List;

public class AsnRecordParser {


    public static void main(String[] args) {

//        String test = " 0                       IANA-RSVD-0                                        IANA-IP-ARIN (Abuse), IANA-ARIN (Admin), IANA-IP-ARIN (Tech)";
        String test = " 23                      AS23                                               NASAA-ARIN (Abuse), CST57-ARIN (Admin), NISN-ARIN (NOC), IPAMO-ARIN (Tech), RFBJ-ARIN (Tech)";

        String[] parts = test.split("\\s\\s+");

        String number = parts[0].trim();
        String name = parts[1];
        String pocStr = parts[2];

        List<Poc> pocs = parsePocs(pocStr);
        Asn asn = new Asn(number, name, pocs);

        System.out.println(asn);
    }

    public static List<Poc> parsePocs(String pocStr) {
        String[] pocParts = pocStr.split(", ");
        List<Poc> pocs = new ArrayList<>();
        for (String pocPart : pocParts) {
            String[] pp = pocPart.replaceAll("\\(", "").replaceAll("\\)", "").split("\\s");
            Poc poc = new Poc(pp[0], pp[1]);
            pocs.add(poc);
        }
        return pocs;
    }

}
