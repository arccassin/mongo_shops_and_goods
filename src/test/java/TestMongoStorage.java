import java.io.*;

/**
 * Created by User on 18 Февр., 2020
 */
public class TestMongoStorage {
    public static void main(String[] args) {
        try (MongoStorage mongoStorage = new MongoStorage()) {
            mongoStorage.init();

            String inputString = "";

            FileInputStream inputStream =
                    null;
            try {
                inputStream = new FileInputStream("src\\test\\resoursces\\TestCommand2.txt");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            CommandReader reader = new CommandReader(mongoStorage);
            reader.read(inputStream);

        }
    }
}
