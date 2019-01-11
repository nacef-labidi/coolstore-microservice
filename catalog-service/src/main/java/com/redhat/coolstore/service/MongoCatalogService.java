package com.redhat.coolstore.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.redhat.coolstore.model.Product;
import org.bson.Document;


@ApplicationScoped
public class MongoCatalogService implements CatalogService {

    @Inject
    private MongoClient mc;

    @Inject
    Logger log;

    private MongoCollection<Document> productCollection;

	public MongoCatalogService() {
	}

	public List<Product> getProducts() {
        return StreamSupport.stream(productCollection.find().spliterator(), false)
                .map(d -> toProduct(d))
                .collect(Collectors.toList());

    }


    public void add(Product product) {
        productCollection.insertOne(toDocument(product));
    }

    public void addAll(List<Product> products) {
        List<Document> documents = products.stream().map(p -> toDocument(p)).collect(Collectors.toList());
        productCollection.insertMany(documents);
    }

    @PostConstruct
    protected void init() {
        log.info("@PostConstruct is called...");

        String dbName = System.getenv("DB_NAME");
        if(dbName==null || dbName.isEmpty()) {
            log.info("Could not get environment variable DB_NAME using the default value of 'CatalogDB'");
            dbName = "CatalogDB";
        }

        MongoDatabase db = mc.getDatabase(dbName);


        productCollection = db.getCollection("products");

        // Drop the collection if it exists and then add default content
        productCollection.drop();
        addAll(DEFAULT_PRODUCT_LIST);

    }

    @PreDestroy
    protected void destroy() {
        log.info("Closing MongoClient connection");
        if(mc!=null) {
            mc.close();
        }
    }

    /**
     * This method converts Product POJOs to MongoDB Documents, normally we would place this in a DAO
     * @param product
     * @return
     */
    private Document toDocument(Product product) {
        return new Document()
                .append("itemId",product.getItemId())
                .append("name",product.getName())
                .append("desc",product.getDesc())
                .append("price",product.getPrice());
    }

    /**
     * This method converts MongoDB Documents to Product POJOs, normally we would place this in a DAO
     * @param document
     * @return
     */
    private Product toProduct(Document document) {
        Product product =  new Product();
        product.setItemId(document.getString("itemId"));
        product.setName(document.getString("name"));
        product.setDesc(document.getString("desc"));
        product.setPrice(document.getDouble("price"));
        return product;
    }



    private static List<Product> DEFAULT_PRODUCT_LIST = new ArrayList<>();
    static {
        DEFAULT_PRODUCT_LIST.add(new Product("329299", "Perceuse a percussion Redstone", "Perceuse a percussion Redstone", 129.00));
        DEFAULT_PRODUCT_LIST.add(new Product("329199", "Barbecue au charbon de bois NATERIAL", "Barbecue au charbon de bois NATERIAL", 79.00));
        DEFAULT_PRODUCT_LIST.add(new Product("165613", "Store vénitien aluminium", "Le système de fixation par clips du store vénitien, permet une pose rapide sans colle ni perçage (sur fenêtre PVC). Il permet d'obtenir une vision sur l'extérieur, tout en préservant votre intimité. L'orientation des lamelles permet de régler de façon naturelle la luminosité de votre pièce et de créer des ambiances différentes tout au long de la journée. Cette gamme se décline en plusieurs coloris et dimensions.", 17.80));
        DEFAULT_PRODUCT_LIST.add(new Product("165614", "Suspension e27 design Tarbes", "Suspension e27 design Tarbes", 19.90));
        DEFAULT_PRODUCT_LIST.add(new Product("165954", "Set de 30 accessoires multifonction", "Set de 30 accessoires multifonction", 24.95));
        DEFAULT_PRODUCT_LIST.add(new Product("444434", "Tondeuse thermique ALPINA BL510SH", "Tondeuse moteur Honda GCV140, 160cc largeur de coupe 51cm, centralisé grandes roues arrieres, bac 60 litres + mulching.", 199.00));
        DEFAULT_PRODUCT_LIST.add(new Product("444435", "Bloc-porte vernis chêne Ceruse ARTENS", "Bloc-porte vernis chêne Ceruse ARTENS", 118.44));
        DEFAULT_PRODUCT_LIST.add(new Product("444436", "Borne extérieure Melbourne LED", "Borne extérieure Melbourne LED", 35.69));

    }

}
