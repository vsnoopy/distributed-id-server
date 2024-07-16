package p3.server.cmd;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import p3.common.api.RmiApiChannel;
import p3.common.util.XUtil;
import p3.server.storage.IdentityRecordProto;
import p3.server.storage.RedisDatabase;
import p3.server.storage.IdentityRecordHelpers;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Class the implements the Identity servers' commands.
 */
public class Commands
{
    private static final boolean debug_params = false;
    //private static final Logger LOGGER = Logger.getLogger(RedisDatabase.class.getName());
    private static final String UUID_TOPIC = "UUID_TOPIC";
    private static final String ID_TOPIC = "ID_TOPIC";

    /**
     * Implements command to create a new account.
     * @param db Redis database.
     * @param login_name Login name for identity account.
     * @param real_name Real name for the identity account.
     * @param password Hashed password for the identity account.
     * @param client_ip Ip address used to create identity account
     * @return String containing the result of the command.
     */
    protected static String Create(RedisDatabase db, KafkaProducer<String, String> kafkaProducer,
                                   String login_name, String real_name, String password, String client_ip)
    {
        final UUID uuid;

        if (debug_params) {
            DisplayParam("login_name", login_name);
            DisplayParam("real_name", real_name);
            DisplayParam("password", password);
            DisplayParam("client_ip", client_ip);
        }

        //user cannot be named delete since we use delete as a command in kafka messages to delete records
        if (Objects.equals(login_name, "delete")) {
            return XUtil.BuildErrorMessage("login name cannot be 'delete'", login_name);
        }

        String json0 = db.getRecord(login_name);
        if (json0 != null) {
            return XUtil.BuildErrorMessage("login name already exits", login_name);
        }

        uuid = UUID.randomUUID();

        //build object
        IdentityRecordProto.IdentityRecord record = IdentityRecordProto.IdentityRecord.newBuilder()
                .setUuid(uuid.toString())
                .setLoginName(login_name)
                .setRealName(real_name)
                .setPassword(password)
                .setClientIp(client_ip)
                .setCreateDate(new Date().getTime())
                .setLastChangeDate(new Date().getTime())
                .build();

        //convert to json
        String json = IdentityRecordHelpers.protoToJson(record);

        //send to kafka to update other servers
        ProducerRecord<String, String> pr = new ProducerRecord<>(ID_TOPIC, login_name, json);
        kafkaProducer.send(pr);
        ProducerRecord<String, String> pr2 = new ProducerRecord<>(UUID_TOPIC, uuid.toString(), login_name);
        kafkaProducer.send(pr2);

        return uuid.toString();
    }

    /**
     * Retrieves the identity account information for the specified account login name.
     * @param db Redis database.
     * @param login_name Login name for identity account.
     * @return String containing the result of the command.
     */
    protected static String Lookup(RedisDatabase db, String login_name)
    {
        if (debug_params) {
            DisplayParam("login_name", login_name);
        }

        String json = db.getRecord(login_name);
        if (json == null) {
            return XUtil.BuildErrorMessage("login name not found", login_name);
        }

        return IdentityRecordHelpers.protoToPrettyString(Objects.requireNonNull(IdentityRecordHelpers.jsonToProto(json)));
    }

    /**
     * Retrieves the identity account information for the specified account id.
     * @param db Redis database.
     * @param uuid User id.
     * @return String containing the result of the command.
     */
    protected static String ReverseLookup(RedisDatabase db, String uuid)
    {
        final String login_name;
        final String result;

        if (debug_params) {
            DisplayParam("uuid", uuid);
        }

        login_name = db.getLoginName(uuid);
        if (login_name == null) {
            return XUtil.BuildErrorMessage("UUID not found", uuid);
        }

        result = db.getRecord(login_name);
        if (result == null) {
            return XUtil.BuildErrorMessage("login name not found", login_name);
        }

        return IdentityRecordHelpers.protoToPrettyString(Objects.requireNonNull(IdentityRecordHelpers.jsonToProto(result)));
    }

    /**
     * Modifies the specified identity account.
     * @param db Redis database.
     * @param old_login_name Current login name of identity account.
     * @param new_login_name New login name for identity account.
     * @param password Hashed password of the identity account.
     * @return String containing the result of the command.
     */
    protected static String Modify(RedisDatabase db, KafkaProducer<String, String> kafkaProducer,
                                   String old_login_name, String new_login_name, String password)
    {
        if (debug_params) {
            DisplayParam("old_login_name", old_login_name);
            DisplayParam("new_login_name", new_login_name);
            DisplayParam("password", password);
        }

        if (Objects.equals(new_login_name, "delete") || Objects.equals(old_login_name, "delete")) {
            return XUtil.BuildErrorMessage("login name cannot be 'delete'", new_login_name);
        }

        String old_json = db.getRecord(old_login_name);
        if (old_json == null) {
            return XUtil.BuildErrorMessage("login name not found", old_login_name);
        }

        IdentityRecordProto.IdentityRecord old_record = IdentityRecordHelpers.jsonToProto(old_json);
        assert old_record != null;
        if (!old_record.getPassword().equals(password)) {
            return XUtil.BuildErrorMessage("Incorrect password", password);
        }

        String new_json0 = db.getRecord(new_login_name);
        if (new_json0 != null) {
            return XUtil.BuildErrorMessage("login name already exists", new_login_name);
        }

        //send to kafka to delete old record
        ProducerRecord<String, String> pr0 = new ProducerRecord<>(ID_TOPIC, old_login_name, "delete");
        kafkaProducer.send(pr0);
        ProducerRecord<String, String> pr1 = new ProducerRecord<>(UUID_TOPIC, old_record.getUuid(), "delete");
        kafkaProducer.send(pr1);

        //build new record with new login name
        IdentityRecordProto.IdentityRecord new_record = IdentityRecordProto.IdentityRecord.newBuilder()
                .setUuid(old_record.getUuid())
                .setLoginName(new_login_name)
                .setRealName(old_record.getRealName())
                .setPassword(password)
                .setClientIp(old_record.getClientIp())
                .setCreateDate(old_record.getCreateDate())
                .setLastChangeDate(new Date().getTime())
                .build();

        String new_json = IdentityRecordHelpers.protoToJson(new_record);

        //send to kafka to update all servers
        ProducerRecord<String, String> pr = new ProducerRecord<>(ID_TOPIC, new_login_name, new_json);
        kafkaProducer.send(pr);
        ProducerRecord<String, String> pr2 = new ProducerRecord<>(UUID_TOPIC, old_record.getUuid(), new_login_name);
        kafkaProducer.send(pr2);

        return IdentityRecordHelpers.protoToPrettyString(new_record);
    }

    /**
     * Deletes the specified identity from the database.
     * @param db Redis database.
     * @param login_name Login name of identity account.
     * @param password Hashed password of the identity account.
     * @return String containing the result of the command.
     */
    protected static String Delete(RedisDatabase db, KafkaProducer<String, String> kafkaProducer,
                                   String login_name, String password)
    {
        if (debug_params) {
            DisplayParam("login_name", login_name);
            DisplayParam("password", password);
        }

        String json = db.getRecord(login_name);
        if (json == null) {
            return XUtil.BuildErrorMessage("login name not found", login_name);
        }

        IdentityRecordProto.IdentityRecord record = IdentityRecordHelpers.jsonToProto(json);
        assert record != null;
        if (!record.getPassword().equals(password)) {
            return XUtil.BuildErrorMessage("Incorrect password", password);
        }

        //send to kafka to update other servers
        ProducerRecord<String, String> pr = new ProducerRecord<>(ID_TOPIC, login_name, "delete");
        kafkaProducer.send(pr);
        ProducerRecord<String, String> pr2 = new ProducerRecord<>(UUID_TOPIC, record.getUuid(), "delete");
        kafkaProducer.send(pr2);

        return IdentityRecordHelpers.protoToPrettyString(record);
    }

    /**
     * Debug/test method for deleting al identities stored in
     * the identities database.
     * @param db Redis database.
     * @return String containing the result of the command.
     */
    protected static String DeleteAll(RedisDatabase db, KafkaProducer<String, String> kafkaProducer)
    {
        Map<String, String> records = db.getAllRecords();
        String login_name = null;
        String uuid = null;
        String results = "";

        for (String name : records.keySet()) {
            IdentityRecordProto.IdentityRecord record = IdentityRecordHelpers.jsonToProto(records.get(name));
            assert record != null;
            login_name = record.getLoginName();
            uuid = record.getUuid();

            //send to kafka to update other servers
            ProducerRecord<String, String> pr = new ProducerRecord<>(ID_TOPIC, login_name, "delete");
            kafkaProducer.send(pr);
            ProducerRecord<String, String> pr2 = new ProducerRecord<>(UUID_TOPIC, uuid, "delete");
            kafkaProducer.send(pr2);
            System.out.println("name = " + name);

            results += login_name + " : " + uuid + "\n";
        }

        results = results.replaceAll("\n$", "");

        System.out.println("result = " + results);

        return results;
    }

    /**
     * Gets the specific information type requested.
     * @param db Redis database.
     * @param type The type of information to be returned: users, usids, all.
     * @return String containing the result of the command.
     */
    protected static String Get(RedisDatabase db, String type)
    {
        final String results;

        RmiApiChannel.GET_TYPE get_type;

        DisplayParam("type", type);

        get_type = RmiApiChannel.GET_TYPE.valueOf(type);
        switch (get_type) {
            case USERS:
                results = BuildUserList(db);
                break;
            case UUIDS:
                results = BuildUUIDList(db);
                break;
            case ALL:
                results = BuildAllList(db);
                break;
            default:
                results = "unknown Get type";
                break;
        }

        return results;
    }

    /**
     * Debug/test method that returns identity database information
     * to the client.
     * @param db Redis database.
     * @return String containing the result of the command.
     */
    protected static String DebugStats(RedisDatabase db)
    {
        final String result;
        final long name_table_count = db.getRecordCount();
        final long uuid_table_count = db.getUUIDCount();

        synchronized (IdServer.console_critical_section) {
            result = "name_table_count = " + name_table_count + " § " + "uuid_table_count = " + uuid_table_count;

            XUtil.DumpPrintTitle("name_to_record [" + name_table_count + "]:");
            db.getAllRecords().forEach((k, v) -> {
                IdentityRecordProto.IdentityRecord record = IdentityRecordHelpers.jsonToProto(v);
                assert record != null;
                IdentityRecordHelpers.protoToPrettyDump(record);
            });

            XUtil.DumpPrintTitle("uuid_to_name [" + uuid_table_count + "]:");
            db.getAllUUIDs().forEach((k, v) -> {
                XUtil.DumpPrint("\t" + k + " -> " + v);
            });
        }

        return result;
    }

    /**
     * Builds a list of identity accounts' names stored in database.
     * @param db Redis database.
     * @return String containing the result of the command.
     */
    private static String BuildUserList(RedisDatabase db)
    {
        String results = "";

        Map<String, String> records = db.getAllRecords();
        for (String name : records.keySet()) {
            IdentityRecordProto.IdentityRecord record = IdentityRecordHelpers.jsonToProto(records.get(name));
            results += record.getLoginName() + "\n";
        }

        results = results.replaceAll("\n$", "");

        return results;
    }

    /**
     * Builds a list of identity accounts' uuids stored in database.
     * @param db Redis database.
     * @return String containing the result of the command.
     */
    private static String BuildUUIDList(RedisDatabase db)
    {
        String results = "";

        Map<String, String> records = db.getAllUUIDs();
        for (String uuid : records.keySet()) {
            results += uuid + "\n";
        }

        results = results.replaceAll("\n$", "");

        return results;
    }

    /**
     * Builds a list of all identity accounts information stored in database.
     * @param db Redis database.
     * @return String containing the result of the command.
     */
    private static String BuildAllList(RedisDatabase db)
    {
        String results = "";

        Map<String, String> records = db.getAllRecords();
        for (String name : records.keySet()) {
            IdentityRecordProto.IdentityRecord record = IdentityRecordHelpers.jsonToProto(records.get(name));
            results += IdentityRecordHelpers.protoToPrettyString(record) + "\n";
        }

        results = results.replaceAll("\n$", "");

        return results;
    }

    /**
     * Debug method to display are parameter name and its value.
     * @param param_name Parameter name.
     * @param value Parameter value.
     */
    private static void DisplayParam(String param_name, String value)
    {
        System.out.println("\t" + param_name + " = " + value);
    }

}
