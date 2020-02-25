import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by User on 18 Февр., 2020
 */
public class CommandReader {

    MongoStorage storage;
    final String commandExit = "ВЫХОД";
    final String commandAddStore = "ДОБАВИТЬ_МАГАЗИН";
    final String commandAddGood = "ДОБАВИТЬ_ТОВАР";
    final String commandPutGood2Store = "ВЫСТАВИТЬ_ТОВАР";
    final String commandStatistic = "СТАТИСТИКА_ТОВАРОВ";

    public CommandReader(MongoStorage storage) {
        this.storage = storage;
    }

    public void read(InputStream inputStream){
        Scanner scanner = new Scanner(inputStream);

        String nextLine;
        do {
            nextLine = scanner.nextLine();
            String[] words = nextLine.split("\\s");
            if (words.length == 0) {
                continue;
            }
            if (words[0].equals(commandAddGood) && words.length == 3) {
                storage.addGood(words[1], Integer.valueOf(words[2]));
            } else if (words[0].equals(commandAddStore) && words.length == 2) {
                storage.addShop(words[1]);
            } else if (words[0].equals(commandPutGood2Store) && words.length == 3) {
                storage.addGood2Shop(words[1], words[2]);
            } else if (words[0].equals(commandStatistic) && words.length == 1) {
                storage.getStatistic();
            } else if (words[0].equals(commandExit)) {
                break;
            } else {
                System.out.println("Неверная команда");
                continue;
            }


        } while (true);
        storage.printAllShops();
    }

}
