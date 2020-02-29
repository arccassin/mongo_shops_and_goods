import java.io.InputStream;
import java.util.Scanner;

import static java.lang.String.format;

/**
 * Created by User on 18 Февр., 2020
 */
class CommandReader {

    private MongoStorage storage;
    private final String commandExit = "ВЫХОД";
    private final String commandAddStore = "ДОБАВИТЬ_МАГАЗИН";
    private final String commandAddGood = "ДОБАВИТЬ_ТОВАР";
    private final String commandPutGood2Store = "ВЫСТАВИТЬ_ТОВАР";
    private final String commandStatistic = "СТАТИСТИКА_ТОВАРОВ";
    private final String validCommandText = "Ввод успешен";
    private final String validAddGoodText = "Товар успешно добавлен";
    private final String failedAddGoodText = "Товар с таким именем уже существует";
    private final String validAddShopText = "Магазин успешно добавлен";
    private final String failedAddShopText = "Магазин с таким именем уже существует";
    private final String validAddGood2ShopText = "Товар успешно добавлен в магазин";
    private final String failedAddGood2ShopText = "Такого товара не существует и он не может быть добавлен в магазин";

    CommandReader(MongoStorage storage) {
        this.storage = storage;
    }

    void read(InputStream inputStream){
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
            if (words[0].equals(commandAddGood) && words.length == 3) {
                if (storage.addGood(words[1], Integer.valueOf(words[2]))){
                    System.out.println(validAddGoodText);
                } else {
                    System.out.println(failedAddGoodText);
                }
            } else if (words[0].equals(commandAddStore) && words.length == 2) {
                if (storage.addShop(words[1])) {
                    System.out.println(validAddShopText);
                } else {
                    System.out.println(failedAddShopText);
                }
            } else if (words[0].equals(commandPutGood2Store) && words.length == 3) {
                if (storage.addGood2Shop(words[1], words[2])) {
                    System.out.println(validAddGood2ShopText);
                } else {
                    System.out.println(failedAddGood2ShopText);
                }
            } else if (words[0].equals(commandStatistic) && words.length == 1) {
                storage.getStatistic();
            } else if (words[0].equals(commandExit)) {
                System.out.println("Bye bye!");
                break;
            } else {
                System.out.println("Неверная команда");
                continue;
            }

        } while (true);
//        storage.printAllShops();
    }

}
