import java.time.LocalDateTime;

public class RawData {
    public int userId;
    public LocalDateTime time;
    public double lat;
    public double lon;

    public RawData(int userId, LocalDateTime time, double lat, double lon) {
        this.userId = userId;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("userId=").append(userId);
        sb.append(", time=").append(time);
        sb.append(", lat=").append(lat);
        sb.append(", lon=").append(lon);
        sb.append('}');
        return sb.toString();
    }
}
