import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Launcher {
    static String URL = "https://www.gazprombank.ru/rest/hackathon/atm/?page=";
    public static void main(String[] args) throws ClassNotFoundException {
//        LocalTime start = LocalTime.now();
//        Class.forName("org.postgresql.Driver");
//        new Launcher().testing(1000000, 100);
//
//        System.out.println(Duration.between(start, LocalTime.now()));
        Type bankType = new TypeToken<List<CashMachine>>(){}.getType();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<CashMachine> cashMachines = new ArrayList<>();
        for(int i = 1;i < 58;i++)
        cashMachines.addAll(gson.fromJson(CashMachine.getReader(URL + i), bankType));
        cashMachines.forEach(System.out::println);
        System.err.println(cashMachines.size());
    }

    public void testing(int countQueries, final int countUsers) {
        Stream<RawData> stream = Stream.generate(new Supplier<RawData>() {
            private final int maxId = countUsers;
            HashMap<Integer, Data> map = new HashMap<>(maxId);


            @Override
            public RawData get() {
                int id = (int) (Math.random() * maxId);
                Data cur;
                if (map.containsKey(id)) {
                    cur = map.get(id);
                    if (cur.curSeconds++ == 60) {
                        cur.curSeconds = 0;
                        cur.time = cur.time.plusMinutes(1);
                    }
                    double dLon = Math.random() > 0.5 ? -0.001 : 0.001;
                    double dLat = Math.random() > 0.5 ? -0.001 : 0.001;
                    cur.lat += dLat;
                    cur.lon += dLon;
                } else {
                    cur = new Data();
                    cur.time = LocalDateTime.now().withSecond(0).withNano(0);
                    cur.curSeconds = 0;
                    cur.lon = (Math.random() * 360) - 180;
                    cur.lat = (Math.random() * 180) - 90;
                }
                map.put(id, cur);
                return new RawData(id, cur.time, cur.lat, cur.lon);
            }

            class Data {
                public int curSeconds;
                public LocalDateTime time;
                public double lon;
                public double lat;
            }
        });
        PrepareData func = new PrepareData("jdbc:postgresql://localhost:5432/gazzbangs",
                "postgres", "postgres");
        stream.limit(countQueries).forEach(func::addData);
    }


}
