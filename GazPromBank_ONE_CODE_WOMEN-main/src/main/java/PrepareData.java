import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class PrepareData {

    private Map<Integer, Map<LocalDateTime, List<RawData>>> usersData = new HashMap<>();
    private Map<Integer, LocalDateTime> lastTime = new HashMap<>();

    private static final String INSERT_PATH = "insert into paths(id, timeInterval, lat, lon) VALUES (?, ?, ?, ?);";
    private static final String INSERT_USER = "insert into users (id) values (?);";
    private static final String SELECT_USER = "select * from users where id = ?";

    private Connection connection;

    public PrepareData(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add user and his data.
     * @param data
     */
    public void addData(RawData data) {
        if (usersData.containsKey(data.userId)) {
            Map<LocalDateTime, List<RawData>> timeMap = usersData.get(data.userId);
            if (timeMap.containsKey(data.time)) {
                List<RawData> list = timeMap.get(data.time);
                list.add(data);
            } else {
                List<RawData> list = new LinkedList<>();
                list.add(data);
                timeMap.put(data.time, list);

                //compact and clear map
                clearMap(data.userId);
            }
        } else {
            Map<LocalDateTime, List<RawData>> timeMap = new HashMap<>();
            List<RawData> list = new LinkedList<>();

            list.add(data);
            timeMap.put(data.time, list);

            usersData.put(data.userId, timeMap);
            lastTime.put(data.userId, data.time);

            loadToDB(data.userId);
        }
    }

    /**
     * Remove old data.
     * @param id
     */
    public void clearMap(int id, int maxSize) {
        Map<LocalDateTime, List<RawData>> timeMap = usersData.get(id);
        if (timeMap.size() > maxSize) {
            LocalDateTime lastKey = lastTime.get(id);
            ClearedData clearedData = new ClearedData(id, lastKey, timeMap.remove(lastKey));
            lastTime.put(id, timeMap.entrySet().iterator().next().getKey());
            loadToDB(clearedData);
        }
    }

    /**
     * Remove old data.
     * @param id
     */
    public void clearMap(int id) {
        clearMap(id, 1);
    }

    /**
     * Load user into DB table users.
     * @param id
     */
    public void loadToDB(int id) {
        try (PreparedStatement statementSelect = connection.prepareStatement(SELECT_USER)) {
            statementSelect.setInt(1, id);
            if (!statementSelect.executeQuery().next()) {
                try (PreparedStatement statementInsert = connection.prepareStatement(INSERT_USER)) {
                    statementInsert.setInt(1, id);
                    statementInsert.executeUpdate();
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    /**
     * Load compressed user data to DB paths.
     * @param data
     */
    public void loadToDB(ClearedData data) {
        try (PreparedStatement statementInsert = connection.prepareStatement(INSERT_PATH)) {
            statementInsert.setInt(1, data.userId);
            statementInsert.setTimestamp(2, Timestamp.from(data.time.toInstant(ZoneOffset.UTC)));
            statementInsert.setDouble(3, data.lat);
            statementInsert.setDouble(4, data.lon);
            statementInsert.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private class ClearedData extends RawData {

        public ClearedData(int userId, LocalDateTime time, List<RawData> list) {
            super(userId, time,
                    list.stream().mapToDouble(t -> t.lat).average().getAsDouble(),
                    list.stream().mapToDouble(t -> t.lon).average().getAsDouble());
        }
    }
    public boolean addAllBanks(List<CashMachine> cashMachines){
        String insertBanks = "insert into banks(bankid, lat, lon, region, regiontype, setlementtype, " +
                "setlement, fulladdress, location, location) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement statementInsertBanks = connection.prepareStatement(insertBanks)) {

        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }
}
