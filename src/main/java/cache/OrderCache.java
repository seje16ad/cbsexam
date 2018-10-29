package cache;

import controllers.OrderController;
import java.util.ArrayList;
import model.Order;
import utils.Config;

//TODO: Build this cache and use it.
public class OrderCache {

    private ArrayList<Order> orders;

    private long ttl;

    private long created;
//getOrderTtl er lavet i Config-klassen
    public OrderCache() {this.ttl = Config.getOrderTtl(); }

    public ArrayList<Order> getOrders(Boolean forceUpdate) {
        if (forceUpdate
        || ((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
        ||  this.orders.isEmpty()) {
            ArrayList<Order> orders = OrderController.getOrders();

            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.orders;
    }
}
