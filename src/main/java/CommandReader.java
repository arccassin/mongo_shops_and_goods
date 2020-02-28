import java.io.InputStream;
import java.util.Scanner;

import static java.lang.String.format;

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
    final String validСommandText = "Ввод успешен";

    public CommandReader(MongoStorage storage) {
        this.storage = storage;
    }

    public void read(InputStream inputStream){
        Scanner scanner = new Scanner(inputStream);

        String helloText = ("Вам доступны для ввода команды: \n" +
                " — Команда добавления магазина: ДОБАВИТЬ_МАГАЗИН <имя>\n" +
                " — Команда добавления товара: ДОБАВИТЬ_ТОВАР <имя> <количество>\n" +
                " — Команда добавления товара в магазин: ВЫСТАВИТЬ_ТОВАР <имя_товара> <имя_магазина>\n" +
                " — Команда получения информации о товарах во всех магазинах: СТАТИСТИКА_ТОВАРОВ\n" +
                " - Команда окончания работы: ВЫХОД\n\n");
        System.out.println(helloText);
        String nextLine;
        do {


            nextLine = scanner.nextLine();
            String[] words = nextLine.split("\\s");
            if (words.length == 0) {
                continue;
            }
            boolean validCommand = true;
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
                validCommand = false;
                continue;
            }
            if (validCommand){
                System.out.println(validСommandText);
            }

        } while (true);
//        storage.printAllShops();
    }

}
