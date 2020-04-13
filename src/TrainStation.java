/*
 * date : 2020-04-13
 * name : Manoj Lakshan Rathnapriya
 * IIT id : 2019274
 * uoW iD : w1761261
 */
import com.google.gson.Gson;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrainStation extends Application {
    private static final int QUEUE_CAPACITY = 21; //maximum capacity of a queue
    private static final int PASSENGER_CAPACITY = 42;//maximum capacity of passengers
    private static int maxLength1 = 0;//maximum length of queue 1
    private static int maxLength2 = 0;//maximum length of queue two
    private static int maxTime1 = 0;//maximum time of queue one
    private static int maxTime2 = 0;//maximum time of queue two
    private static int minTime1 = 18;//minimum time of queue one
    private static int minTime2 = 18;//minimum time of queue two
    //train queue & trin seats details
    private static Passenger[] Train = new Passenger[PASSENGER_CAPACITY];
    //waiting room
    private Passenger[] waitingRoom = new Passenger[PASSENGER_CAPACITY];
    //queue one
    private PassengerQueue trainQueue = new PassengerQueue();
    //queue two
    private PassengerQueue trainQueue2 = new PassengerQueue();
    //save today booked detail for this document
    private Document[] documents = new Document[PASSENGER_CAPACITY];
    //today booking count
    private static int bookedCount = 0;
    //waiting room passengers count
    private static int waitRoomCount = 0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        //disable the warnings and information
        /*
         *create connection to mongoDB
         * use two collection
         */
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.OFF); // e.g. or Log.WARNING, etc.
        com.mongodb.MongoClient mongo = new MongoClient("localhost", 27017);
        MongoClient client = new MongoClient();
        //use Mongo DB data base
        DB db = client.getDB("TrainStation");
        //these collections use to save and load objects
        DBCollection queue = db.getCollection("queue1");
        DBCollection queue2 = db.getCollection("queue2");
        DBCollection wait = db.getCollection("waitingRoom");
        DBCollection train = db.getCollection("train");
        //these collections use to load data
        MongoDatabase database = mongo.getDatabase("dumbaraManikeTrain");
        MongoCollection<Document> toBadulla = database.getCollection("badulla");
        MongoCollection<Document> toColombo = database.getCollection("Colombo");
        MongoCollection<Document> doc = database.getCollection("doc");
        //select the station basdull or colombo
        selectDestination(toBadulla, toColombo);

        //menu system
        System.out.println("\n-----------------------------------------------------------------------------\n");
        Scanner sc = new Scanner(System.in);
        menu:
        while (true) {
            System.out.println("------------------------------------------------");
            System.out.println("|\" W\"  add a passenger to the Waiting Room      |");
            System.out.println("|\" A\"  add a passenger to the trainQueue        |");
            System.out.println("|\" V\"  to view the trainQueue                   |");
            System.out.println("|\" D\"  Delete passenger from the trainQueue     |");
            System.out.println("|\" S\"  Store trainQueue data                    |");
            System.out.println("|\" L\"  Load data                                |");
            System.out.println("|\" R\"  Run the simulation and produce report    |");
            System.out.println("|\" Q\"  exit                                     |");
            System.out.println("------------------------------------------------");
            System.out.print("Enter your option = ");
            String option = sc.next().toLowerCase();
            System.out.println("---------------------------------------------------------------------------");

            switch (option) {
                case "a":
                    addPassenger();
                    displayQueue();
                    break;
                case "v":
                    viewQueue();
                    break;
                case "d":
                    delete();
                    break;
                case "s":
                    saveDetails(queue, queue2, wait, doc, train);
                    break;
                case "l":
                    loadData(queue, queue2, wait, doc, train);
                    break;
                case "r":
                    simulation();
                    break;
                case "w":
                    waiting();
                    displayTheWaitingRoom();
                    break;
                case "q":
                    break menu;
                default:
                    System.out.println("\n-------------------------------------------------------------------------");
                    System.out.println("*************************    INVALID INPUT    ***************************");

            }
            System.out.println("--------------------------------------------------------------------------\n");
        }

    }

    /*
     * display the waiting room
     */
    private void displayTheWaitingRoom() {
        Stage stage = new Stage();
        BorderPane borderPane = new BorderPane();
        Pane pane = new Pane();
        pane.setMinWidth(200);
        pane.setId("pane");
        borderPane.setLeft(pane);
        pane.setStyle("-fx-background-color: #333");
        Pane pane1 = new Pane();
        borderPane.setTop(pane1);
        ScrollPane scrollPane = new ScrollPane();
        borderPane.setCenter(scrollPane);
        GridPane gridPane = new GridPane();
        scrollPane.setContent(gridPane);
        Label label = new Label("QUEUE I AND QUEUE II");
        label.setId("lable1");
        pane1.getChildren().add(label);
        pane1.setStyle("-fx-background-color: #333");
        Button button = new Button("waiting room");
        Button button3 = new Button("close");
        clear(pane1, gridPane);
        setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, PASSENGER_CAPACITY);
        button3.setLayoutY(450);
        button3.setId("close");
        button3.setOnAction(e -> stage.close());
        pane.getChildren().addAll(button3, button);
        Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("button.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    //this method use to implement the queue
    private void setTheQueue(int x, Passenger[] array) {
        if (x != 0) {
            for (int i = 0; i < x; i++) {
                if (i == x - 1) {
                    array[i] = null;
                } else {
                    array[i] = array[i + 1];
                }
            }
        }
    }

    /*
     * generate a number  between 1 and 6
     * @return int
     * this helps to send passenger from waiting room to queue and get the delay time
     */
    private int getRandInt() {
        Random random = new Random();
        return random.nextInt(6) + 1;
    }

    /*
     * htis is gui add a passenger to the train queue
     * add passenger to the queue
     */
    private void addPassenger() {
        //show the alert when queue is full
        if (trainQueue.isFull() && trainQueue2.isFull()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("QUEUES ARE FULL!!");
            alert.showAndWait();
        } else {
            if (waitRoomCount != 0) {
                int num = getRandInt();
                //get random number check is it greater than waiting  room length
                //if waiting room count less than that number send all passengers in the wating room
                if (num > waitRoomCount) {
                    System.out.println("--------------------------------------------------");
                    System.out.println(waitRoomCount + " passenger/passengers added to train queues");
                    System.out.println("--------------------------------------------------");
                    addToueue(waitRoomCount);
                } else {
                    System.out.println("--------------------------------------------------");
                    System.out.println(num + " passenger/passengers added to train queues");
                    System.out.println("--------------------------------------------------");
                    addToueue(num);
                }
            } else {
                //when waiting room is empty show this alert
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("waiting room is empty\n cant add passengers to  queue");
                alert.showAndWait();
            }
        }
    }

    //this method used to add passenger to queue
    private void addToueue(int num) {
        for (int i = 0; i < num; i++) {
            if (waitingRoom[0] != null) {
                //select the shotest queue
                //if queue lengths are equeal send passenger to first queue
                if (trainQueue.getLast() > trainQueue2.getLast()) {
                    //add passenger to the queue
                    trainQueue2.add(waitingRoom[0]);
                    //dislay the add passenger's details
                    trainQueue2.display();
                    System.out.println("added to queue II");
                    //increase queue last value
                    trainQueue2.setLast(1);
                } else {
                    trainQueue.add(waitingRoom[0]);
                    trainQueue.display();
                    System.out.println("added to queue I");
                    trainQueue.setLast(1);
                }
                //set the queue
                //1 index -> 0 index
                setTheQueue(waitRoomCount, waitingRoom);
                waitRoomCount--;
            }
        }

    }

    private void displayQueue() {
        Stage stage = new Stage();
        BorderPane borderPane = new BorderPane();
        Pane pane = new Pane();
        pane.setMinWidth(200);
        pane.setId("pane");
        borderPane.setLeft(pane);
        Pane pane1 = new Pane();
        borderPane.setTop(pane1);
        ScrollPane scrollPane = new ScrollPane();
        borderPane.setCenter(scrollPane);
        GridPane gridPane = new GridPane();
        scrollPane.setContent(gridPane);
        Label label = new Label("QUEUE I AND QUEUE II");
        label.setId("lable1");
        pane1.getChildren().add(label);
        pane1.setStyle("-fx-background-color: #333");
        Button button = new Button("waiting room");
        Button button1 = new Button("train queue I");
        Button button3 = new Button("close");
        Button button4 = new Button("train queue II");
        clear(pane1, gridPane);
        setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, PASSENGER_CAPACITY);
        button.setLayoutY(20);
        button1.setLayoutY(80);
        button.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, PASSENGER_CAPACITY);
        });
        button1.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE I", trainQueue.getQueueArray(), QUEUE_CAPACITY);
        });
        button4.setLayoutY(140);
        button4.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE II", trainQueue2.getQueueArray(), QUEUE_CAPACITY);
        });
        button3.setLayoutY(450);
        button3.setId("close");
        button3.setOnAction(e -> stage.close());
        pane.getChildren().addAll(button1, button3, button4, button);
        Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("button.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }


    /*
     * this method use to view waiting room,queue, and train
     */
    private void viewQueue() {
        Stage stage = new Stage();
        BorderPane borderPane = new BorderPane();
        Pane pane = new Pane();
        pane.setMinWidth(200);
        pane.setId("pane");
        borderPane.setLeft(pane);
        Pane pane1 = new Pane();
        borderPane.setTop(pane1);
        ScrollPane scrollPane = new ScrollPane();
        borderPane.setCenter(scrollPane);
        GridPane gridPane = new GridPane();
        scrollPane.setContent(gridPane);
        pane1.setStyle("-fx-background-color: #333");
        Button button = new Button("waiting room");
        Button button1 = new Button("train queue I");
        Button button2 = new Button("Train ");
        Button button3 = new Button("close");
        Button button4 = new Button("train queue II");
        button.setLayoutY(20);
        Label label = new Label("");
        label.setId("lable1");
        clear(pane1, gridPane);
        setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, PASSENGER_CAPACITY);
        button.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, PASSENGER_CAPACITY);
        });
        button1.setLayoutY(80);
        button1.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE I", trainQueue.getQueueArray(), QUEUE_CAPACITY);
        });
        button2.setLayoutY(200);
        button2.setOnAction(e -> {
            clear(pane1, gridPane);
            setTrain(pane1, gridPane, "TRAIN SEATS", Train, PASSENGER_CAPACITY);
        });
        button4.setLayoutY(140);
        button4.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE II", trainQueue2.getQueueArray(), QUEUE_CAPACITY);
        });
        button3.setLayoutY(450);
        button3.setId("close");
        button3.setOnAction(e -> stage.close());
        pane.getChildren().addAll(button, button1, button2, button3, button4);
        Scene scene = new Scene(borderPane, 800, 600);
        scene.getStylesheets().add(getClass().getResource("button.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void setTrain(Pane pane, GridPane gridPane, String l, Passenger[] w, int x) {
        Label label = new Label(l);
        label.setLayoutX(300);
        label.setId("lable1");
        pane.getChildren().add(label);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 0, 15, 100));
        gridPane.getChildren().add(vBox);
        gridPane.setAlignment(Pos.CENTER);
        Label label1 = new Label("SEAT NUMBER" + "\t\t\t" + "NAME");
        label1.setStyle("-fx-padding: 6px;-fx-color: black;-fx-font-weight: 700");
        vBox.getChildren().addAll(label1, separator);
        for (int i = 0; i < x; i++) {
            if (w[i] != null) {
                String lab = "\t" + String.valueOf(i + 1) + "\t\t\t" + w[i].getName();
                Label button = new Label(lab);
                button.setStyle("-fx-padding: 5px;-fx-text-fill: #3742fa;-fx-font-weight: 700;-fx-font-size: 14px");
                vBox.getChildren().add(button);

            } else {
                String lab = "\t" + String.valueOf(i + 1) + "\t\t\t\t" + "empty";
                Label button = new Label(lab);
                button.setStyle("-fx-padding: 5px");
                vBox.getChildren().add(button);
            }
        }
    }

    //clear the pane
    private void clear(Pane pane, GridPane gridPane) {
        pane.getChildren().clear();
        gridPane.getChildren().clear();
    }

    //set pane and add scroll pane
    private void setPane(Pane pane, GridPane gridPane, String l, Passenger[] w, int x) {
        Label label = new Label(l);
        label.setLayoutX(300);
        label.setId("lable1");
        pane.getChildren().add(label);
        Separator separator = new Separator(Orientation.HORIZONTAL);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10, 0, 15, 100));
        gridPane.getChildren().add(vBox);
        gridPane.setAlignment(Pos.CENTER);
        Label label1 = new Label("NO" + "\t\t" + "SEAT NUMBER" + "\t\t\t" + "NAME");
        label1.setStyle("-fx-padding: 6px;-fx-color: black;-fx-font-weight: 700");
        vBox.getChildren().addAll(label1, separator);
        for (int i = 0; i < x; i++) {
            if (w[i] != null) {
                String lab = String.valueOf(i + 1) + "\t\t\t" + String.valueOf(w[i].getSeatNumber()) + "\t\t\t" + w[i].getName();
                Label button = new Label(lab);
                button.setStyle("-fx-padding: 5px;-fx-text-fill: #3742fa;-fx-font-size: 14px;-fx-font-weight: 700");
                vBox.getChildren().add(button);

            }
        }
    }

    /*
     * this method use to delete passenger from queue
     */
    private void delete() {
        System.out.println("------------------------------------------------------------------");
        System.out.println("******************    DELETE PASSENGER    ************************");
        System.out.println("------------------------------------------------------------------");
        Scanner sc = new Scanner(System.in);
        loop:
        while (true) {
            //select the queue
            System.out.println("\"1.\"  queue I");
            System.out.println("\"2.\"  queue II");
            System.out.println("\"Q/q.\"  exit");
            System.out.print("Enter your option = ");
            String number = sc.next();
            switch (number) {
                case "1":
                    if (deletePassenger(trainQueue.getQueueArray(), trainQueue.getLast())) {
                        trainQueue.setLast(-1);
                    }
                    break;
                case "2":
                    if (deletePassenger(trainQueue2.getQueueArray(), trainQueue2.getLast())) {
                        trainQueue2.setLast(-1);
                    }
                    break;
                case "q":
                    break loop;
                default:
                    System.out.println("wrong input");
            }
        }
    }

    //delete passenger & return boolean value
    private boolean deletePassenger(Passenger[] array, int x) {
        boolean find = false;
        if (x != 0) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter passenger name to delete = ");
            String name = sc.nextLine();
            System.out.println("-------------------------------------------------------");
            System.out.println("searching .....");
            Passenger deletePassenger = null;
            for (int i = 0; i < x; i++) {
                if (array[i] != null) {
                    //search passenger name
                    if (array[i].getName().equalsIgnoreCase(name)) {
                        deletePassenger = array[i];
                        for (int k = i; k < x; k++) {
                            //set the queue
                            //it start in delete index
                            if (k == x - 1) {
                                //if last index should be null when deleting a passenger
                                array[k] = null;
                            } else {
                                array[k] = array[k + 1];
                            }
                            find = true;
                        }
                        break;
                    }
                }
            }
            if (find) {
                System.out.println("------------------------------------------------");
                System.out.println("delete completed !!");
                System.out.println("|Name = " + deletePassenger.getName() + "        |");
                System.out.println("|seat = " + deletePassenger.getSeatNumber() + "  |");
                System.out.println("------------------------------------------------");

            } else {
                System.out.println(name + " hasn't added to the queue");
            }
        } else {
            System.out.println("--------------------------------------------");
            System.out.println(">>>  queue is empty");
            System.out.println("--------------------------------------------");
        }
        return find;
    }

    /*
     * this method used to show the simulate report
     * and add passenger to the train
     */
    private void simulation() throws InterruptedException {
        boolean find = false;
        //set the max lengths
        maxLength1 = trainQueue.getLast();
        maxLength2 = trainQueue2.getLast();
        //set the max length in all time
        if (trainQueue.getMaxLength() < maxLength1) {
            trainQueue.setMaxLength(maxLength1);
        }
        if (trainQueue2.getMaxLength() < maxLength2) {
            trainQueue2.setMaxLength(maxLength2);
        }
        //passenger borded to train
        /*
        *this x and y used to increas the fist element of queues
         */
        int x = 0;
        int y = 0;
        while (!trainQueue.isEmpty() || !trainQueue2.isEmpty()) {
            //get the time
            int timeDelay = getRandInt() + getRandInt() + getRandInt();
            int timeDelay2 = getRandInt() + getRandInt() + getRandInt();
            if (!trainQueue.isEmpty()) {
                //set time to passengers in the queue
                setTime(trainQueue.getQueueArray(), maxLength1, timeDelay, x);
                x++;
                //gather statistics
                dataSetting(trainQueue, trainQueue.getLast());
                //reduce 1 from last in the queue
                trainQueue.setLast(-1);
            }
            if (!trainQueue2.isEmpty()) {
                setTime(trainQueue2.getQueueArray(), maxLength2, timeDelay2, y);
                y++;
                dataSetting(trainQueue2, trainQueue2.getLast());
                trainQueue2.setLast(-1);
            } else {
                minTime2 = 0;
            }
            find = true;
        }
        if (find) {
            showReport();
            for (int i = maxLength1; i > 0; i--) {
                //boarded to train
                bordToTrain(Train, trainQueue, maxLength1);
            }
            for (int i = maxLength2; i > 0; i--) {
                bordToTrain(Train, trainQueue2, maxLength2);
            }
        } else {
            System.out.println("nothing to add train");
        }
    }

    //set waiting time to Passengers in the queue
    private void setTime(Passenger[] passenger, int x, int time, int y) {
        for (int i = y; i < x; i++) {
            passenger[i].setSecondsInQueue(time);
        }
    }

    private void dataSetting(PassengerQueue passenger, int last) {
        //gather statictics
        int tempTimeMax;
        int tempTimeMin;
        if (passenger.equals(trainQueue)) {
            tempTimeMax = maxTime1;
            tempTimeMin = minTime1;
        } else {
            tempTimeMax = maxTime2;
            tempTimeMin = minTime2;
        }
        for (int i = 0; i < last; i++) {
            if (passenger.getQueueArray()[i] != null) {
                int time = passenger.getQueueArray()[i].getSecondsInQueue();
                if (time > tempTimeMax) {
                    tempTimeMax = time;
                    if (passenger.getMaxStayInQueue() < time) {
                        passenger.setMaxStayInQueue(tempTimeMax);
                    }
                }

                if (time < tempTimeMin) {
                    tempTimeMin = time;
                    if (passenger.getMinStayInQueue() > tempTimeMin)
                        passenger.setMinStayInQueue(tempTimeMin);
                }
            }
        }
        if (passenger.equals(trainQueue)) {
            if (maxLength1 == 1) {
                maxTime1 = tempTimeMax;
                minTime1 = 0;
            } else {
                maxTime1 = tempTimeMax;
                minTime1 = tempTimeMin;
            }
        } else {
            if (maxLength2 == 1) {
                maxTime2 = tempTimeMax;
                minTime2 = 0;
            } else {
                maxTime2 = tempTimeMax;
                minTime2 = tempTimeMin;
            }
        }
    }

    //remove passenger from queue & add to train
    private void bordToTrain(Passenger[] train, PassengerQueue queue, int x) {
        train[queue.remove().getSeatNumber() - 1] = queue.remove();
        //set the queue
        setTheQueue(x, queue.getQueueArray());
    }

    /*
     * display the report
     * this shows gathered statictics
     */
    private void showReport() {
        Stage stage = new Stage();
        stage.setTitle("SIMULATION REPORT");
        BorderPane borderPane = new BorderPane();
        Pane pane = new Pane();
        Pane pane2 = new Pane();
        ScrollPane pane1 = new ScrollPane();
        borderPane.setTop(pane);
        borderPane.setCenter(pane1);
        borderPane.setBottom(pane2);
        Button close = new Button("save & exit");
        close.setLayoutX(350);
        close.setStyle("-fx-background-color: #eb4d4b");
        close.setOnAction(e -> {
            saveReport(trainQueue, maxLength1, "QUEUE I");
            saveReport(trainQueue2, maxLength2, "QUEUE II");
            stage.close();
        });
        pane2.getChildren().add(close);
        Label title = new Label("SIMULATION REPORT");
        title.setLayoutX(255);
        title.setLayoutY(10);
        title.setStyle("-fx-text-fill:#4b7bec;-fx-font-size: 23px;-fx-font-weight: 700;-fx-font-family: 'Bitstream Vera Sans Mono'");

        Label title1 = new Label("queue I details");
        title1.setLayoutX(30);
        title1.setLayoutY(30);
        title1.setStyle("-fx-text-fill:#16d91d;-fx-font-size: 18px;-fx-font-weight: 800;-fx-padding: 20px;");

        Label minimum = new Label("minimum time = " + String.valueOf(minTime1));
        minimum.setLayoutX(30);
        minimum.setLayoutY(90);
        minimum.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label maximum = new Label("maximum time = " + String.valueOf(maxTime1));
        maximum.setLayoutX(30);
        maximum.setLayoutY(110);
        maximum.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        double avg = 0;
        if (maxLength1 != 0) {
            avg = (double) (maxTime1 + minTime1) / maxLength1;
        }
        Label average = new Label("average time = " + String.valueOf(avg));
        average.setLayoutX(30);
        average.setLayoutY(130);
        average.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        Label length = new Label("maximum length = " + String.valueOf(maxLength1));
        length.setLayoutX(30);
        length.setLayoutY(150);
        length.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label length2 = new Label("All time maximum length = " + String.valueOf(trainQueue.getMaxLength()));
        length2.setLayoutX(30);
        length2.setLayoutY(170);
        length2.setStyle("-fx-font-size:15px;-fx-text-fill: #c4115a ");

        Label length5 = new Label("All time maximum time = " + String.valueOf(trainQueue.getMaxStayInQueue()));
        length5.setLayoutX(30);
        length5.setLayoutY(190);
        length5.setStyle("-fx-font-size:15px;-fx-text-fill: #c4115a ");

        Label title2 = new Label("queue II details");
        title2.setLayoutX(30);
        title2.setLayoutY(200);
        title2.setStyle("-fx-text-fill:#16d91d;-fx-font-size: 18px;-fx-font-weight: 800;-fx-padding: 20px;");
        Label minimum2 = new Label("minimum time = " + String.valueOf(minTime2));

        minimum2.setLayoutX(30);
        minimum2.setLayoutY(250);
        minimum2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        Label maximum2 = new Label("maximum time = " + String.valueOf(maxTime2));
        maximum2.setLayoutX(30);
        maximum2.setLayoutY(270);
        maximum2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        double avg2 = 0;
        if (maxLength2 != 0) {
            avg2 = (double) (maxTime2 + minTime2) / maxLength2;
        }
        Label average2 = new Label("average time = " + String.valueOf(avg2));
        average2.setLayoutX(30);
        average2.setLayoutY(290);
        average2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label length3 = new Label("maximum length = " + String.valueOf(maxLength2));
        length3.setLayoutX(30);
        length3.setLayoutY(310);
        length3.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label length6 = new Label("All time maximum length = " + String.valueOf(trainQueue2.getMaxLength()));
        length6.setLayoutX(30);
        length6.setLayoutY(330);
        length6.setStyle("-fx-font-size:15px;-fx-text-fill: #c4115a");

        Label length7 = new Label("All time maximum time = " + String.valueOf(trainQueue2.getMaxStayInQueue()));
        length7.setLayoutX(30);
        length7.setLayoutY(350);
        length7.setStyle("-fx-font-size:15px;-fx-text-fill: #c4115a ");

        pane.getChildren().addAll(title, title1, title2, minimum, maximum, average, length, minimum2, maximum2, average2, length2, length3, length5, length6, length7);
        HBox vBox = new HBox();
        pane1.setContent(vBox);
        vBox.setPadding(new Insets(10, 0, 15, 40));
        pane.setPadding(new Insets(10, 0, 20, 60));
        if (trainQueue.getMaxLength() != 0) {
            printQueues(vBox, "QUEUE ONE", trainQueue, maxLength1);
        }
        if (trainQueue2.getMaxLength() != 0) {
            printQueues(vBox, "QUEUE TWO", trainQueue2, maxLength2);
        }
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(borderPane, 800, 700);
        stage.setScene(scene);
        stage.showAndWait();
    }

    //print the queues
    private void printQueues(HBox v, String label, PassengerQueue queue, int x) {
        VBox vBox = new VBox();
        v.getChildren().add(vBox);
        vBox.setPadding(new Insets(10, 0, 15, 60));
        Label label1 = new Label(label);
        label1.setStyle("-fx-padding: 30px;-fx-font-weight: 800;-fx-text-fill: purple");
        Label menu = new Label("NO" + "\t" + "SEAT" + "\t" + "TIME" + "\t\t" + "NAME");
        menu.setStyle("-fx-text-fill: black;-fx-font-weight: 700");
        Separator separator = new Separator(Orientation.HORIZONTAL);
        vBox.getChildren().addAll(label1, menu, separator);
        for (int i = 0; i < x; i++) {
            Label data = new Label((i + 1) + "\t" + String.valueOf(queue.getQueueArray()[i].getSeatNumber()) + "\t\t  " + String.valueOf(queue.getQueueArray()[i].getSecondsInQueue()) + "\t\t" + queue.getQueueArray()[i].getName());
            vBox.getChildren().add(data);
            data.setStyle("-fx-text-fill: #010813;-fx-padding: 5px;-fx-font-size: 13px");
        }
    }

    //save report to text file
    private void saveReport(PassengerQueue queue, int x, String l) {
        try {
            int timeMin;
            int timeMax;
            if (queue.equals(trainQueue)) {
                timeMax = maxTime1;
                timeMin = minTime1;
            } else {
                timeMax = maxTime2;
                timeMin = minTime2;
            }
            if (x != 0) {
                double avg = (timeMin + timeMax) / x;
                String content = "\nminimum time = " + timeMin + "\n" +
                        "maximum time = " + timeMax + "\n" +
                        "average time = " + avg + "\n" +
                        "maximum length = " + x + "\n";
                File file = new File("report.txt");
                FileWriter fileWriter = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fileWriter);
                LocalDate now = LocalDate.now();
                bw.write(String.valueOf(now));
                bw.write("\n" + l);
                bw.write(content);
                bw.write("---------------------------------------\n");
                bw.write("***   PASSENGER NAMES & DETAILS   ***\n");
                for (int i = 0; i < x; i++) {
                    String content2 = queue.getQueueArray()[i].getName() + " " + queue.getQueueArray()[i].getSeatNumber() + " " + queue.getQueueArray()[i].getSecondsInQueue() + "\n";
                    bw.write(content2);
                }
                bw.write("------------------------------------------\n===========================================================================================\n");
                bw.close();
            }
        } catch (IOException ioe) {
            System.out.println("Some thing went wrong");
            ioe.printStackTrace();
        }
    }

    //add to waiting room gui
    private void waiting() {
        Stage stage = new Stage();
        BorderPane borderPane = new BorderPane();
        ScrollPane scrollPane = new ScrollPane();
        Pane pane = new Pane();
        Pane pane1 = new Pane();
        borderPane.setTop(pane);
        pane.setStyle("-fx-background-color: #333");
        borderPane.setCenter(scrollPane);
        borderPane.setBottom(pane1);
        Label label2 = new Label("ADD TO WAITING ROOM");
        label2.setStyle("-fx-text-fill: #ffffff;-fx-font-size: 30px;-fx-font-weight: 800");
        pane1.setStyle("-fx-background-color: #333");
        label2.setLayoutX(180);
        pane.getChildren().add(label2);
        Button close = new Button("close");
        close.setLayoutX(330);
        pane1.getChildren().add(close);
        close.setOnAction(e -> {
            stage.close();
        });
        close.setStyle("-fx-background-color: #eb3b5a;-fx-font-weight: 600;-fx-min-width: 150px;-fx-text-fill: #ffffff;-fx-min-height: 50px");
        HBox hBox = new HBox();
        VBox vBox = new VBox();
        VBox vBox1 = new VBox();
        vBox.setPadding(new Insets(10, 0, 15, 60));
        vBox1.setPadding(new Insets(50, 0, 15, 90));
        hBox.getChildren().addAll(vBox, vBox1);
        Label label = new Label("SEAT NUMBER" + "\t\t\t" + "NAME");
        label.setStyle("-fx-padding: 6px;-fx-text-fill: #fbc531;-fx-font-weight: 700");
        Separator separator = new Separator(Orientation.HORIZONTAL);
        vBox.getChildren().addAll(label, separator);
        vBox1.setSpacing(12);
        for (int i = 0; i < PASSENGER_CAPACITY; i++) {
            if (documents[i] != null) {
                String seat = (String) documents[i].get("seat");
                String name = (String) documents[i].get("name");
                String sname = (String) documents[i].get("sname");
                Label label1 = new Label("\t" + seat + "\t\t\t\t" + name + " " + sname);
                label1.setStyle("-fx-padding: 10px;-fx-text-fill:#130f40;-fx-font-weight: 700;-fx-font-size: 14px");
                vBox.getChildren().add(label1);
                Button button = new Button("add" + seat);
                button.setStyle("-fx-background-color: #22a6b3;-fx-text-fill: #ffffff;-fx-font-weight: 700;-fx-min-width: 120px");
                vBox1.getChildren().addAll(button);
                button.setOnAction(e -> {
                    addTo(seat);
                    button.setVisible(false);
                    button.setDisable(false);
                    label1.setStyle("-fx-padding: 10px;-fx-text-fill: #a5b1c2;-fx-font-weight: 700");
                });
            }
        }
        scrollPane.setContent(hBox);
        Scene scene = new Scene(borderPane, 800, 650);
        stage.setScene(scene);
        stage.showAndWait();
    }

    /*
     * from this method check user inputs
     * @param doc and add them to waiting room
     *add data to waiting room
     */
    private void addDataToWaitingRoom(MongoCollection<Document> doc) {
        FindIterable<Document> data = doc.find();
        LocalDate todayDate = LocalDate.now();
        //read dthe collecting
        for (Document record : data) {
            //get data from mongo collection
            LocalDate date = LocalDate.parse((String) record.get("date"));
            //set and add data for waitingRoom specific date(current date)
            if (Period.between(todayDate, date).getDays() == 1) {
                int seat = Integer.parseInt((String) record.get("seat"));
                documents[seat - 1] = record;
                bookedCount++;
            }
        }
    }

    /*
     * form this method add passenger to the waiting room
     * and set passenger's attributes
     * @param sNum (seat number)
     */
    private void addTo(String sNum) {
        try {
            //check passenger already in the queue
            //if find the set @run as false
            if (documents[Integer.parseInt(sNum) - 1] != null) {
                boolean run = true;
                //get the seat number from the document array
                String seat = (String) documents[Integer.parseInt(sNum) - 1].get("seat");
                for (Passenger data : waitingRoom) {
                    if (data != null) {
                        if (data.getSeatNumber() == Integer.parseInt(sNum)) {
                            run = false;
                            break;
                        }
                    }
                }

                if (run) {
                    if (sNum.equalsIgnoreCase(seat)) {
                        String name = (String) documents[Integer.parseInt(sNum) - 1].get("name");
                        String sname = (String) documents[Integer.parseInt(sNum) - 1].get("sname");
                        //create a new Passenger in waiting room array
                        waitingRoom[waitRoomCount] = new Passenger();
                        //set the name
                        waitingRoom[waitRoomCount].setName(name, sname);
                        //set the seat number
                        waitingRoom[waitRoomCount].setSeatNumber(Integer.parseInt(seat));
                        //display passenger details
                        waitingRoom[waitRoomCount].display();
                        //increase waitingRoom count
                        waitRoomCount++;
                        //remove passenger from booking details (documents)
                        documents[Integer.parseInt(sNum) - 1] = null;
                        bookedCount--;
                        //display passenger details
                        System.out.println("-------------------------------------------------------------------------");
                        System.out.println("\n>>>>" + name + " " + sname + " " + "added to waiting room ");
                        System.out.println("-------------------------------------------------------------------------");
                    }
                }
                if (!run) {
                    System.out.println("-------------------------------------------------------------------------");
                    System.out.println(">>> Already added to waiting room");
                    System.out.println("-------------------------------------------------------------------------");
                }
            } else {
                System.out.println("-------------------------------------------------------------------------");
                System.out.println(sNum + " han not book today");
                System.out.println("-------------------------------------------------------------------------");
            }
        } catch (Exception e) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(">>> Some thing went wrong!! you CAN NOT add passenger");
            System.out.println("-------------------------------------------------------------------------");
        }
    }

    //select what is the station
    private void selectDestination(MongoCollection<Document> toBadulla, MongoCollection<Document> toColombo) {
        try {
            System.out.println("\n-------------------------------------------------------------------------");
            System.out.println("********************     DENUWARA MANIKE TRAIN      *********************");
            System.out.println("-------------------------------------------------------------------------\n");
            Scanner sc = new Scanner(System.in);
            System.out.println("TODAY : " + LocalDate.now());
            /*
             *create connection to mongoDB
             * use two collection
             */

            loop:
            while (true) {
                System.out.println("\n---------------------------------------------------------------------------");
                System.out.println("************************    SELECT THE STATION   **************************\n");
                System.out.println("\"1\" Colombo");
                System.out.println("\"2\" Badulla");
                System.out.print("Enter your option : ");
                String option = sc.next();
                switch (option) {
                    case "1":
                        System.out.println("\nWELCOME TO COLOMBO RAILWAY STATION");
                        addDataToWaitingRoom(toBadulla);
                        break loop;
                    case "2":
                        System.out.println("\nWELCOME TO BADULLA RAILWAY STATION");
                        addDataToWaitingRoom(toColombo);
                        break loop;

                    default:
                        System.out.println("=======  Wrong Input ======");
                }
                System.out.println("----------------------------------------------------------------------------");
            }
        } catch (ExceptionInInitializerError ignored) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println("something went wrong");
            System.out.println("-------------------------------------------------------------------------");
        }
    }

    /*
     * save data to mongo collect
     *
     * @param queue  queue one save here
     * @param queue2 queue2 save here
     * @param wait   waitingRoom in this  wait collection
     * @param doc    booked details save in this doc collection
     * @param train  train details save in this trin collection
     */
    private void saveDetails(DBCollection queue, DBCollection queue2, DBCollection wait, MongoCollection<Document> doc, DBCollection train) {
        System.out.println("\n---------------------------------------------------------------------------------");
        System.out.println("-----------------------------      SAVING DATA    -------------------------------");
        System.out.println("---------------------------------------------------------------------------------\n");
        /*
          when calling to save method there have passed some null values
          coz i had use that method two
         */
        save(queue, QUEUE_CAPACITY, trainQueue.getQueueArray(), null, 1, null);
        System.out.println(">>> saved waiting room");
        save(queue2, QUEUE_CAPACITY, trainQueue2.getQueueArray(), null, 1, null);
        System.out.println(">>> saved queue  one");
        save(train, PASSENGER_CAPACITY, Train, null, 1, null);
        System.out.println(">>> saved queue two");
        save(wait, PASSENGER_CAPACITY, waitingRoom, null, 1, null);
        System.out.println(">>> saved train");
        save(null, PASSENGER_CAPACITY, null, documents, 2, doc);
        System.out.println(">>> saved document");
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("                                    SAVING FINISHED                              ");
        System.out.println("---------------------------------------------------------------------------------");
    }

    /*
     *saving data in to BD collections in mogoDB
     * @param collection mongo collection
     * @param x capacity of arrays
     * @param array the array what want to save
     * @param document  the document
     * @param y this used what have save
     *          if y = 1 array is about passengers
     *          if y = 2 array is a document array
     * @param doc this is a mongo collection
     *            and other collection are DBcollection
     */
    private void save(DBCollection collection, int x, Passenger[] array, Document[] document, int y, MongoCollection<Document> doc) {
        try {
            if (y == 2) {
                //clear previous data
                doc.drop();
                //saving data in document
                Document document1 = new Document();
                for (int i = 0; i < x; i++) {
                    if (document[i] != null) {
                        document1.append("data", document[i]);
                        doc.insertOne(document1);
                        document1.clear();
                    }
                }
            }
            if (y == 1) {
                //clear the collection before add data
                collection.drop();
                String now = LocalDate.now().toString();
                Gson gson = new Gson();
                for (int i = 0; i < x; i++) {
                    if (array[i] != null) {
                        //convert object to string using Gson(convert to json file)
                        String json = gson.toJson(array[i]);
                        //add a id and date fore it
                        BasicDBObject basicDBObject = new BasicDBObject("object", json).append("id", i).append("date", now);
                        //add data to collection
                        collection.insert(basicDBObject);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("some thing went wrong");
        }
    }

    /*
     * load data to data structures
     * @param queue  queue one details load from this queue collection
     * @param queue2 queue2 data load from this queue2 collection
     * @param wait   waitingRoom data load from this wait collection
     * @param doc    booked detail load from this doc collection
     * @param train  this is calling in load method
     */
    private void loadData(DBCollection queue, DBCollection queue2, DBCollection wait, MongoCollection<Document> doc, DBCollection train) {
        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.println("-----------------------------     LOADING DATA    -------------------------------");
        System.out.println("-------------------------------------------------------------------------------\n");
        System.out.println(">>> loaded waiting room");
        load(queue, trainQueue.getQueueArray(), null, 1, null);
        System.out.println(">>> loaded queue one");
        load(queue2, trainQueue2.getQueueArray(), null, 1, null);
        System.out.println(">>> loaded queue two");
        load(wait, waitingRoom, null, 1, null);
        System.out.println(">>> loaded train");
        load(train, Train, null, 1, null);
        System.out.println(">>> load booked details");
        load(null, null, documents, 2, doc);
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println("                                  LOADING FINISHED                            ");
        System.out.println("---------------------------------------------------------------------------------");
    }

    /*
     *loading data in to BD collections in mogoDB
     * @param collection mongo collection
     * @param array the array what want to save
     * @param document  the document
     * @param y this used what have save
     *          if y = 1 array is about passengers
     *          if y = 2 array is a document array
     * @param doc this is a mongo collection
     *            and other collection are DBcollection
     */
    private void load(DBCollection collection, Passenger[] array, Document[] document, int y, MongoCollection<Document> doc) {
        try {

            if (y == 2) {
                //remove loaded data to document (booked data)
                for (int j = 0; j < PASSENGER_CAPACITY; j++) {
                    document[j] = null;
                }
                FindIterable<Document> details = doc.find();
                for (Document record : details) {
                    Document document1 = (Document) record.get("data");
                    String seat = (String) document1.get("seat");
                    document[Integer.parseInt(seat) - 1] = document1;
                }
            }
            if (y == 1) {
                Gson gson = new Gson();
                String now = LocalDate.now().toString();
                DBCursor data = collection.find();
                for (DBObject obj : data) {
                    String date = (String) obj.get("date");
                    if (date.equals(now)) {
                        //get the saved object from the collection as a string
                        String seat = (String) obj.get("object");
                        //convert it to Passenger object using gson
                        Passenger object = gson.fromJson(seat, Passenger.class);
                        //add it according it's index
                        int index = (int) obj.get("id");
                        array[index] = object;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("some thing went wrong");
        }
    }
}