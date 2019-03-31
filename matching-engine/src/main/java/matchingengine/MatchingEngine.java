package matchingengine;


import dispatcher.EnvelopeDispatcher;
import messages.*;
import communicators.*;
import java.io.*;
import java.net.*;
import java.util.Collections;
import java.util.Vector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MatchingEngine {
    String symbol;
    int bidPrice;
    int orignalBidPrice;
    short bidQuantity;
    int askPrice;
    int originalAskPrice;
    short askQuantity;
    short orderIdCounter;
    private final static int PORT = 2000;
    private static final Logger LOG = LogManager.getFormatterLogger(MatchingEngine.class.getName());
    boolean ackMessageReceived=false;
    public RestingBook restingBook=new RestingBook();
    public OrderHandler orderHandler;
    //TODO figure what we need the IP to be
    //private final static String IP = "127.0.0.1";
    public Socket socket;
    //made public for testing
    public TcpCommunicator tcpCommunicator;
    //sellers from cheapest to most expensive, buyers most expensive to cheapest

//todo probs axe this constructor
    public MatchingEngine(String symbol, int bidPrice,int askPrice){
        this.symbol=symbol;
        this.bidPrice=bidPrice;
        this.orignalBidPrice=bidPrice;
        this.askPrice=askPrice;
        this.originalAskPrice=askPrice;
        this.askQuantity=0;
        orderIdCounter=0;
        register();
        this.orderHandler=new OrderHandler(this.restingBook,symbol,tcpCommunicator);
        initMessageListener();
    }

    public MatchingEngine(String symbol){
        this.symbol=symbol;
        this.bidPrice=0;
        this.orignalBidPrice=0;
        this.askPrice=0;
        this.originalAskPrice=0;
        this.askQuantity=0;
        this.bidQuantity=0;
        orderIdCounter=0;
        register();
        this.orderHandler=new OrderHandler(this.restingBook,symbol,tcpCommunicator);
        initMessageListener();
    }


    public void register(){
        RegisterMatchingEngineMessage registerMatchingEngineMessage = new RegisterMatchingEngineMessage(this.symbol);
        try {
            InetAddress address=InetAddress.getLocalHost();
            //TODO change address to IP
            socket=new Socket(address,PORT);
            tcpCommunicator=new TcpCommunicator(socket);
            tcpCommunicator.send(registerMatchingEngineMessage.encode());
            LOG.info("%s sent registerMatchingEngineMessage to Gateway",this.symbol);
            //TODO wait for ack?
        }catch (IOException e) {
            LOG.error("Failure in %s constructor, couldn't send registerMatchingEngineMessage",
                    symbol, e.getMessage());
        }
    }


    public void initMessageListener(){
        EnvelopeDispatcher<byte[]> envelopeDispatcher = new EnvelopeDispatcher<>(tcpCommunicator, Message::decode);
        envelopeDispatcher.registerForDispatch(TopOfBookRequestMessage.class, this::sendTopOfBook);
        LOG.info("%s Initialized TopOfBookRequestMessage listener",this.symbol);
        envelopeDispatcher.registerForDispatch(ForwardOrderMessage.class, this::handleOrder);
        LOG.info("%s Initialized ForwardOrderMessage listener",this.symbol);
        envelopeDispatcher.registerForDispatch(ForwardCancelMessage.class, this::cancelOrder);
        LOG.info("%s Initialized ForwardCancelMessage listener",this.symbol);
        new Thread(envelopeDispatcher).start();
    }

    private void cancelOrder(Envelope<ForwardCancelMessage> envelope) {
        ForwardCancelMessage cancelOrderMessage = envelope.getMessage();
        short orderId=cancelOrderMessage.getOrderId();
        short playerId=cancelOrderMessage.getPlayerId();
        short cancelledQuantity=0;
        LOG.info("%s received ForwardCancelMessage from gateway for order %d from player %d ",
                this.symbol, orderId,playerId);
        boolean done=false;
        for(int i=0;i<restingBook.sellers.size()&&done==false;i++){
            if(restingBook.sellers.get(i).getPlayerId()==playerId&&restingBook.sellers.get(i).getOrderID()==orderId){
                cancelledQuantity=restingBook.sellers.get(i).getQuantity();
                restingBook.sellers.remove(i);
                done=true;
            }
        }
        for(int i=0;i<restingBook.buyers.size()&&done==false;i++){
            if(restingBook.buyers.get(i).getPlayerId()==playerId&&restingBook.buyers.get(i).getOrderID()==orderId){
                cancelledQuantity=restingBook.buyers.get(i).getQuantity();
                restingBook.buyers.remove(i);
                done=true;
            }
        }
        CancelConfirmationMessage cancelConfirmationMessage = new CancelConfirmationMessage(playerId,
                orderId,cancelledQuantity,this.symbol);
        try {
            tcpCommunicator.send(cancelConfirmationMessage.encode());
            LOG.info("%s sent cancelConfirmationMessage to Gateway for order %d",this.symbol, orderId);
        } catch (IOException e) {
            LOG.error("Failure in %s ME while trying to send cancelConfirmationMessage to Gateway",
                    symbol, e.getMessage());
        }
    }

    private void sendTopOfBook(Envelope<TopOfBookRequestMessage> envelope) {
        //update top of book
        if(restingBook.buyers.size()>0) {
            this.bidPrice = restingBook.buyers.get(0).getPrice();
        }
        else{
            this.bidPrice=this.orignalBidPrice;
        }
        if(restingBook.sellers.size()>0) {
            this.askPrice = restingBook.sellers.get(0).getPrice();
        }
        else{
            this.askPrice=originalAskPrice;
        }
        LOG.info("%s Received TopOfBookRequestMessage from Gateway",this.symbol);
        TopOfBookRequestMessage topOfBookRequestMessage = envelope.getMessage();
        TopOfBookResponseMessage topOfBookResponseMessage=new TopOfBookResponseMessage(this.symbol,
                this.bidPrice, this.bidQuantity,this.askPrice,this.askQuantity);
        try {
            tcpCommunicator.send(topOfBookResponseMessage.encode());
            LOG.info("% sent topOfBookResponseMessage to Gateway",this.symbol);
        } catch (IOException e) {
            LOG.error("Failure in %s while trying to send topOfBookResponseMessage to Gateway",
                    symbol, e.getMessage());
        }
    }

    private void handleOrder(Envelope<ForwardOrderMessage> envelope){
        LOG.info("Received ForwardOrderMessage from Gateway for player ID %d",
                envelope.getMessage().getPlayerId());
        orderHandler.handleOrder(envelope.getMessage());
    }

    //todo for testing, look into better way to do this one
    public void cancelOrderTester(ForwardCancelMessage cancelOrderMessage){
        short orderId=cancelOrderMessage.getOrderId();
        short playerId=cancelOrderMessage.getPlayerId();
        short cancelledQuantity=0;
        LOG.info("%s received ForwardCancelMessage from gateway for order %d from player %d ",
                this.symbol, orderId,playerId);
        boolean done=false;
        for(int i=0;i<restingBook.sellers.size()&&done==false;i++){
            if(restingBook.sellers.get(i).getPlayerId()==playerId&&restingBook.sellers.get(i).getOrderID()==orderId){
                cancelledQuantity=restingBook.sellers.get(i).getQuantity();
                restingBook.sellers.remove(i);
                done=true;
            }
        }
        for(int i=0;i<restingBook.buyers.size()&&done==false;i++){
            if(restingBook.buyers.get(i).getPlayerId()==playerId&&restingBook.buyers.get(i).getOrderID()==orderId){
                cancelledQuantity=restingBook.buyers.get(i).getQuantity();
                restingBook.buyers.remove(i);
                done=true;
            }
        }
        CancelConfirmationMessage cancelConfirmationMessage = new CancelConfirmationMessage(playerId,
                orderId,cancelledQuantity,this.symbol);
        try {
            tcpCommunicator.send(cancelConfirmationMessage.encode());
            LOG.info("%s sent cancelConfirmationMessage to Gateway for order %d",this.symbol, orderId);
        } catch (IOException e) {
            LOG.error("Failure in %s ME while trying to send cancelConfirmationMessage to Gateway",
                    symbol, e.getMessage());
        }

    }










    public static void main(String[] args) {
        MatchingEngine matchingEngine=new MatchingEngine("GOOG");
    }

}
