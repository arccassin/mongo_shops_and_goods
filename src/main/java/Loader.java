/**
 * Created by User on 17 Февр., 2020
 */
public class Loader {
    public static void main(String[] args) {
        try (MongoStorage mongoStorage = new MongoStorage()) {
            mongoStorage.init();

            CommandReader reader = new CommandReader(mongoStorage);
            reader.read(System.in);

        }
    }
}
