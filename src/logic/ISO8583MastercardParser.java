package logic;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.packager.GenericPackager;
import org.jpos.util.Logger;
import org.jpos.util.SimpleLogListener;

public class ISO8583MastercardParser {
    public String iso8583MastercardMessageTest(String iso8583String) {
        try {
            Logger logger = new Logger();
            logger.addListener(new SimpleLogListener(System.out));

            GenericPackager packager = new GenericPackager("./src/res/mastercard.xml");

            ISOMsg isoMsg = new ISOMsg();

            String isoMessageBuffer = iso8583String;
            isoMsg.setPackager(packager);

            isoMsg.unpack(ISOUtil.hex2byte(isoMessageBuffer));

            StringBuilder result = new StringBuilder();
            result.append("Field " + 1 + ": " + isoMsg.getString(0) + "\n");

            for (int i = 1; i <= 128; i++) {
                if (isoMsg.hasField(i)) {
                    result.append("Field " + i + ": " + isoMsg.getString(i) + "\n");
                }
            }

            return result.toString();
        } catch (ISOException e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }
}
