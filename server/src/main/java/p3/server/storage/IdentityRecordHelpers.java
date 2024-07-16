package p3.server.storage;

import p3.common.util.XColor;
import p3.common.util.XUtil;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import p3.server.cmd.IdServer;

import java.util.Date;
import java.util.UUID;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for converting IdentityRecord objects to and from JSON and ProtoBuf.
 * Also provides a pretty print method.
 */
public class IdentityRecordHelpers {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(IdentityRecordHelpers.class);

    /**
     * Converts a JSON string to a ProtoBuf IdentityRecord object.
     * @param json JSON string
     * @return ProtoBuf IdentityRecord object
     */
    public static IdentityRecordProto.IdentityRecord jsonToProto(String json) {
        IdentityRecordProto.IdentityRecord.Builder builder = IdentityRecordProto.IdentityRecord.newBuilder();
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Error parsing JSON", e);
            return null;
        }
        return builder.build();
    }

    /**
     * Converts a ProtoBuf IdentityRecord object to a JSON string.
     * @param record ProtoBuf IdentityRecord object
     * @return JSON string
     */
    public static String protoToJson(IdentityRecordProto.IdentityRecord record) {
        String json = null;
        try {
            json = JsonFormat.printer().print(record);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Error converting ProtoBuf to JSON", e);
            return null;
        }
        return json;
    }

    /**
     * Pretty prints a ProtoBuf IdentityRecord object.
     * @param record ProtoBuf IdentityRecord object
     */
    public static void protoToPrettyDump(IdentityRecordProto.IdentityRecord record) {

        synchronized (IdServer.console_critical_section) {
            XUtil.DumpPrintTitle("Identity Record:");

            String tabs = "\t\t";
            XUtil.DumpPrint(tabs + "uuid = " + UUID.fromString(record.getUuid()));
            XUtil.DumpPrint(tabs + "login_name = " + record.getLoginName());
            XUtil.DumpPrint(tabs + "real_name = " + record.getRealName());
            XUtil.DumpPrint(tabs + "client_ip = " + record.getClientIp());
            XUtil.DumpPrint(tabs + "create_date = " + new Date(record.getCreateDate()));
            XUtil.DumpPrint(tabs + "last_change_date = " + new Date(record.getLastChangeDate()));
        }
    }

    /**
     * Converts a ProtoBuf IdentityRecord object to a pretty string.
     * @param record ProtoBuf IdentityRecord object
     * @return Pretty string
     */
    public static String protoToPrettyString(IdentityRecordProto.IdentityRecord record) {
        String sep = XColor.BLUE_BOLD_BRIGHT + " : " + XColor.RESET;
        final String string;

        string = UUID.fromString(record.getUuid()) + sep + record.getLoginName() + sep + record.getRealName() +
                sep + record.getClientIp() + sep + new Date(record.getCreateDate()) +
                sep + new Date(record.getLastChangeDate());

        return string;
    }
}
