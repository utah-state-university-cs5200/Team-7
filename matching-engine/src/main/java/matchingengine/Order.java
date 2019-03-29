package matchingengine;

import messages.SubmitOrderMessage;

public class Order implements Comparable<Order> {
    private short orderID;
    private short playerId;
    private SubmitOrderMessage.OrderType orderType;
    private short quantity;
    private int price;
    private String symbol;
    public Order(short playerId, SubmitOrderMessage.OrderType orderType, short quantity, int price,
                      String symbol,short orderID){
        this.playerId=playerId;
        this.orderType=orderType;
        this.quantity=quantity;
        this.price=price;
        this.symbol=symbol;
        this.orderID=orderID;
    }
    public int compareTo(Order order2){
        //TODO get this working right
        int diff = this.price-order2.price;
        return diff;
    }
    public short getOrderID() {
        return orderID;
    }

    public void setOrderID(short orderID) {
        this.orderID = orderID;
    }

    public short getPlayerId() {
        return playerId;
    }

    public void setPlayerId(short playerId) {
        this.playerId = playerId;
    }

    public SubmitOrderMessage.OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(SubmitOrderMessage.OrderType orderType) {
        this.orderType = orderType;
    }

    public short getQuantity() {
        return quantity;
    }

    public void setQuantity(short quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(short price) {
        this.price = price;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}