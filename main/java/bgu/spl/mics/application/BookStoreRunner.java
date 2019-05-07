package bgu.spl.mics.application;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.Pair;
import bgu.spl.mics.application.passiveObjects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CountDownLatch;


/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) {

        Gson gson = new Gson();
        File jsonFile = Paths.get(args[0]).toFile();
        try {
            LinkedList<Thread> threadsList = new LinkedList<>();
            //Read initialInventory
            JsonObject jsonObject = gson.fromJson(new FileReader(jsonFile), JsonObject.class);
            JsonArray inv = jsonObject.getAsJsonArray("initialInventory");
            //Read initialResources
            BookInventoryInfo[] books = gson.fromJson(inv, BookInventoryInfo[].class);
            JsonArray resources = jsonObject.getAsJsonArray("initialResources");
            DeliveryVehicle[] vehicles = gson.fromJson(((JsonObject) resources.get(0)).get("vehicles"), DeliveryVehicle[].class);
            //Read SERVICES
            JsonObject services = jsonObject.getAsJsonObject("services");
            //timer
            JsonObject time = services.getAsJsonObject("time");
            int speed = time.get("speed").getAsInt();
            int duration = time.get("duration").getAsInt();
            //count down
            CountDownLatch countdown = new CountDownLatch(services.get("selling").getAsInt() + services.get("inventoryService").getAsInt() + services.get("logistics").getAsInt()
                    + services.get("resourcesService").getAsInt() + services.getAsJsonArray("customers").size());
            //selling services
            for (int i = 0; i < services.get("selling").getAsInt(); i++) {
                threadsList.add(new Thread(new SellingService(countdown)));
            }
            //inventory services
            for (int i = 0; i < services.get("inventoryService").getAsInt(); i++) {
                threadsList.add(new Thread((new InventoryService(countdown))));
            }
            //logistic services
            for (int i = 0; i < services.get("logistics").getAsInt(); i++) {
                threadsList.add(new Thread(new LogisticsService(countdown)));
            }
            //resources services
            for (int i = 0; i < services.get("resourcesService").getAsInt(); i++) {
                threadsList.add(new Thread((new ResourceService(countdown))));
            }
            //API services
            JsonArray customers = services.getAsJsonArray("customers");
            //hashMap for customers
            HashMap<Integer, Customer> customersHashMap = new HashMap<>();
            for (JsonElement e : customers) {
                JsonObject element = (JsonObject) e;
                int id = element.get("id").getAsInt();
                String name = element.get("name").getAsString();
                String address = element.get("address").getAsString();
                int distance = element.get("distance").getAsInt();
                int creditNumber = ((JsonObject) element.get("creditCard")).get("number").getAsInt();
                int creditAmount = ((JsonObject) element.get("creditCard")).get("amount").getAsInt();
                Customer c = new Customer(id, name, address, distance, creditNumber, creditAmount);
                JsonArray orders = element.getAsJsonArray("orderSchedule");
                ArrayList<Pair<String, Integer>> orderSchedule = new ArrayList<>();
                for (JsonElement order : orders) {
                    String bookTitle = ((JsonObject) order).get("bookTitle").getAsString();
                    int tick = ((JsonObject) order).get("tick").getAsInt();
                    Pair<String, Integer> p = new Pair(bookTitle, tick);
                    orderSchedule.add(p);
                }
                orderSchedule.sort(Pair::compareTo);
                threadsList.add(new Thread(new APIService(orderSchedule, c, countdown)));
                customersHashMap.put(id, c);
            }

            Inventory inventory = Inventory.getInstance();
            inventory.load(books);
            ResourcesHolder resourcesHolder = ResourcesHolder.getInstance();
            resourcesHolder.load(vehicles);

            for (Thread t : threadsList) {
                t.start();
            }
            try {
                countdown.await();
            } catch (InterruptedException e) {
                System.out.println("not all services have registered");
            }
            threadsList.add(new Thread(new TimeService(speed, duration)));
            threadsList.getLast().start();
            for (Thread t : threadsList) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    System.out.println(t.getName() + "didnt terminated");
                }
            }


            // printing serlized objects
            printToFile(args[1], customersHashMap);
            inventory.printInventoryToFile(args[2]);
            MoneyRegister.getInstance().printOrderReceipts(args[3]);
            printToFile(args[4], MoneyRegister.getInstance());


        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        return;
    }

    public static void printToFile(String filename, Serializable s) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}




