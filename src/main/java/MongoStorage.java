import com.mongodb.MongoClient;
import com.mongodb.MongoConfigurationException;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.bson.json.JsonWriterSettings;

import static com.mongodb.client.model.Accumulators.*;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static java.util.Arrays.asList;

/**
 * Created by User on 17 Февр., 2020
 */
public class MongoStorage implements Closeable {

    // Объект для работы с Mongo
    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    // Объект для работы с коллекцией магазинов
    private MongoCollection<Document> shopsCollection;

    // Объект для работы с коллекцией товаров
    private MongoCollection<Document> goodsCollection;

    private static Consumer<Document> printDocuments() {
        return doc -> System.out.println(doc.toJson(JsonWriterSettings.builder().indent(true).build()));
    }

    void init() {
        ServerAddress serverAddress = new ServerAddress("127.0.0.1", 27017);
        try {
            mongoClient = new MongoClient(serverAddress);
            mongoDatabase = mongoClient.getDatabase("local");
        } catch (MongoConfigurationException Exc) {
            System.out.println("Не удалось подключиться к Mongo");
            System.out.println(Exc.getMessage());
        }

        shopsCollection = mongoDatabase.getCollection("shops");
        goodsCollection = mongoDatabase.getCollection("goods");

        // Удалим
        shopsCollection.drop();
        goodsCollection.drop();

    }

    public void addShop(String name) {
        Bson filter = eq("name", name);
        Document shop = shopsCollection.find(filter).first();
        if (shop != null) {
            return;
        }
        shop = new Document()
                .append("name", name)
                .append("goods_in_store", asList());

        shopsCollection.insertOne(shop);
    }

    public void addGood(String name, int price) {
        Bson filter = eq("name", name);
        Document good = shopsCollection.find(filter).first();
        if (good != null) {
            return;
        }
        good = new Document()
                .append("name", name)
                .append("price", price);
        goodsCollection.insertOne(good);
    }

    public void addGood2Shop(String goodName, String shopName) {
        Bson filter = eq("name", shopName);
        Bson updateGoods = Updates.addToSet("goods_in_store", goodName);
        Document shop = shopsCollection.findOneAndUpdate(filter, updateGoods);
    }

    public void printAllShops() {
        FindIterable<Document> iterable = shopsCollection.find();
        MongoCursor<Document> cursor = iterable.iterator();
        while (cursor.hasNext()) {
            System.out.println(cursor.next().toJson());
        }
    }

    //все по магазинам
    private void getAllStaticFacet() {
        Bson lookup = lookup("goods", "goods_in_store", "name", "fromGood");
        Bson unwind = unwind("$fromGood");
        Bson sort = sort(descending("fromGood.price"));
        Bson group = group("$name",
                sum("count", 1L),
                avg("averagePrice", "$fromGood.price"),
                last("minPriceGood", "$fromGood"),
                first("maxPriceGood", "$fromGood"));

        Bson match = match(lt("fromGood.price", 100L));
        Bson matchGroup = group("$name", sum("matchCount", 1L));
        Bson faset1 = facet(
                new Facet("shopInfo", lookup, unwind, sort, group),
                new Facet("under100", lookup, unwind, match, matchGroup)
        );
        List<Document> allResults = shopsCollection.aggregate(Arrays.asList(faset1)).into(new ArrayList<>());

        Document res = allResults.get(0);
        ArrayList<Document> shopInfoDoc = (ArrayList<Document>) res.get("shopInfo");
        ArrayList<Document> under100Doc = (ArrayList<Document>) res.get("under100");

        StringBuilder builder = new StringBuilder();
        shopInfoDoc.forEach(doc -> {
            String shopName = (String) doc.get("_id");
            builder.append("\n\nМагазин \"");
            builder.append(shopName);
            builder.append("\"");
            builder.append("\n— Общее количество товаров: ");
            builder.append(doc.get("count"));
            builder.append("\n— Средяя цена товара: ");
            builder.append(doc.get("averagePrice"));

            Document good = (Document) doc.get("minPriceGood");
            if (good != null) {
                builder.append("\n— Самый дешевый товар: ");
                builder.append(good.get("name"));
                builder.append(" - ");
                builder.append(good.get("price"));
            }

            good = (Document) doc.get("maxPriceGood");
            if (good != null) {
                builder.append("\n— Самый дорогой товар: ");
                builder.append(good.get("name"));
                builder.append(" - ");
                builder.append(good.get("price"));
            }
            for (int i = under100Doc.size() - 1; i >= 0; i--) {
                Document shopDoc = under100Doc.get(i);
                String currentShopName = (String) shopDoc.get("_id");
                if (shopName.equals(currentShopName)) {
                    builder.append("\n— Количество товаров, дешевле 100 рублей: ");
                    builder.append(shopDoc.get("matchCount"));
                    under100Doc.remove(i);
                    break;
                }
            }
        });
        System.out.println(builder.toString());
    }

    // Общее количество товаров по каждому магазину
    private void getGoodCountOfStore() {
        Bson unwind = unwind("$goods_in_store");
        Bson group = group("$name", sum("count", 1L));
        Bson project = project(fields(excludeId(), include("name", "count"), computed("name", "$_id")));
        List<Document> results = shopsCollection.aggregate(Arrays.asList(unwind, group, project)).into(new ArrayList<>());
        results.forEach(doc -> System.out.printf("Магазин: \"%s\". Количество товаров: %s\n", doc.get("name"), doc.get("count")));
    }

    //Средняя цена товара по каждому магазину
    private void getGoodAvgOfStore() {
        Bson lookup = lookup("goods", "goods_in_store", "name", "fromGood");
        Bson unwind = unwind("$fromGood");
        Bson group = group("$name", avg("averagePrice", "$fromGood.price"));
        List<Document> results = shopsCollection.aggregate(Arrays.asList(lookup, unwind, group)).into(new ArrayList<>());
        results.forEach(doc -> System.out.printf("Магазин: \"%s\". Средняя цена: %s\n", doc.get("_id"), doc.get("averagePrice")));
    }

    //Самый дешевый и дорогой товар в каждом магазине
    private void getGoodMaxOfStore() {
        Bson lookup = lookup("goods", "goods_in_store", "name", "fromGood");
        Bson unwind = unwind("$fromGood");
        Bson sort = sort(descending("fromGood.price"));
        Bson group1 = group("$name", last("min", "$fromGood"));
        Bson group2 = group("$name", first("max", "$fromGood"));
        List<Document> results = shopsCollection.aggregate(Arrays.asList(lookup, unwind, sort, group1)).into(new ArrayList<>());
        results.forEach(doc -> {
            Document last = (Document) doc.get("min");
            if (last != null) {
                String goodName = (String) last.get("name");
                System.out.printf("Магазин: \"%s\". Самый дешевый товар: %s\n", doc.get("_id"), goodName);
            }
        });

        results = shopsCollection.aggregate(Arrays.asList(lookup, unwind, sort, group2)).into(new ArrayList<>());
        results.forEach(doc -> {
            Document last = (Document) doc.get("max");
            if (last != null) {
                String goodName = (String) last.get("name");
                System.out.printf("Магазин: \"%s\". Самый дорогой товар: %s\n", doc.get("_id"), goodName);
            }
        });

    }

    //Количество товаров дешевле 100 рублей
    private void getGoodsLess100OfStore() {
        Bson lookup = lookup("goods", "goods_in_store", "name", "fromGood");
        Bson unwind = unwind("$fromGood");
        Bson match = match(lt("fromGood.price", 100L));
        Bson group = group("$name", sum("matchCount", 1L));
        List<Document> results = shopsCollection.aggregate(Arrays.asList(lookup, unwind, match, group)).into(new ArrayList<>());
        results.forEach(doc -> System.out.printf("Магазин: \"%s\". Количество товаров дешевле 100 рублей: %s\n", doc.get("_id"), doc.get("matchCount")));
    }

    public void getStatistic() {
        getAllStaticFacet();
    }

    public void close() {
        mongoClient.close();
    }

}
