import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bson.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TrainStation extends Application {
    private static Passenger[] Train = new Passenger[42];
    private Passenger[] waitingRoom = new Passenger[42];
    private PassengerQueue trainQueue = new PassengerQueue();
    private PassengerQueue trainQueue2 = new PassengerQueue();

    private Document[] documents = new Document[42];

    private static int bookedCount = 0;
    private int waitRoomCount = 0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        //disable the warnings and information
        /*
         *create connection to mongoDB
         * use two collection
         */
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
        com.mongodb.MongoClient mongo = new MongoClient("localhost", 27017);
        DB db = mongo.getDB("TrainStation");

        //save and load objects
        DBCollection queue = db.getCollection("queue1");
        DBCollection queue2 = db.getCollection("queue2");
        DBCollection wait = db.getCollection("waitingRoom");
        DBCollection train = db.getCollection("train");

        //load data
        MongoDatabase database = mongo.getDatabase("dumbaraManikeTrain");
        MongoCollection<Document> toBadulla = database.getCollection("badulla");
        MongoCollection<Document> toColombo = database.getCollection("Colombo");
        //select the station bas=dull or colombo
        //menu system
        selectDestination(toBadulla, toColombo);
        System.out.println("\n===========================================================================\n");
        Scanner sc = new Scanner(System.in);
        menu:
        while (true) {
            System.out.println("\" W\"  add a passenger to the Waiting Room");
            System.out.println("\" A\"  add a passenger to the trainQueue");
            System.out.println("\" V\"  to view the trainQueue");
            System.out.println("\" D\"  Delete passenger from the trainQueue");
            System.out.println("\" S\"  Store trainQueue data");
            System.out.println("\" L\"  Load data");
            System.out.println("\" R\"  Run the simulation and produce report ");
            System.out.println("\" Q\"  exit ");
            System.out.print("\nEnter your option = ");
            String option = sc.next().toLowerCase();
            System.out.println("---------------------------------------------------------------------------");

            switch (option) {
                case "a":
                    addPassenger();
                    break;
                case "v":
                    viewQueue();
                    break;
                case "d":
                    delete();
                    break;
                case "s":

                    break;
                case "l":

                    break;
                case "r":
                    simulation();
                    break;
                case "w":
                    adding();
                    break;
                case "q":
                    break menu;
                default:
                    System.out.println("\n===========================================================================");
                    System.out.println("*************************    INVALID INPUT    *****************************");

            }
            System.out.println("===========================================================================\n");
        }

    }


    //this method use to implement the queue
    private void setTheQueue(int x, Passenger[] array, int y) {
        if (x != 0) {
            for (int i = 0; i < x; i++) {
                if (x == y) {
                    array[x] = null;
                    break;
                } else {
                    if (i == y) {
                        array[i] = null;
                    } else {
                        array[i] = array[i + 1];
                    }
                }
            }
        }
    }

    /**
     * generate a number  between 1 and 6
     *
     * @return int
     * this helps to send passenger from waiting room to queue and get the delay time
     */
    private int getRandInt() {
        Random random = new Random();
        return random.nextInt(6) + 1;

    }


    /***
     * htis is gui add a passenger to the train queue
     * add passenger to the queue
     */
    public void addPassenger() {
        //show the alert when queue is full
        if (trainQueue.isFull() && trainQueue2.isFull()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("QUEUES ARE FULL!!");
            alert.showAndWait();
        } else {
            if (waitRoomCount != 0) {
                int num = getRandInt();
                //get random number check is it greater than waiting  room length
                if (num > waitRoomCount) {
                    addToueue(waitRoomCount);
                } else {
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

    private void addToueue(int num) {
        for (int i = 0; i < num; i++) {
            if (waitingRoom[0] != null) {

                if (trainQueue.getLast() > trainQueue2.getLast()) {
                    trainQueue2.add(waitingRoom[0]);
                } else {
                    trainQueue.add(waitingRoom[0]);
                }
                setTheQueue(waitRoomCount, waitingRoom, 42);
                waitRoomCount--;
            }
        }

    }

    //sort the queue according to seat number
    private void sortTheQueue(Passenger[] array, int x) {
        Passenger temp;
        if (x >= 2) {
            for (int i = 0; i < x; i++) {
                for (int j = 1; j < x; j++) {
                    if (array[i].getSeatNumber() > array[j].getSeatNumber()) {
                        temp = array[i];
                        array[i] = array[j];
                        array[j] = temp;
                    }
                }
            }
        }
    }

    /**
     * this method use to view waiting room,queue, and train
     */
    public void viewQueue() {
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
        pane1.setStyle("-fx-background-color: #a4b0be");
        Button button = new Button("waiting room");
        Button button1 = new Button("train queue I");
        Button button2 = new Button("Train ");
        Button button3 = new Button("close");
        Button button4 = new Button("train queue II");
        button.setLayoutY(20);
        Label label = new Label("VIEW WAITING ROOM , QUEUE AND TRAIN");
        label.setId("lable1");
        pane1.getChildren().add(label);
        label.setLayoutX(150);
        button.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "WAITING ROOM", waitingRoom, 42);
        });
        button1.setLayoutY(80);
        button1.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE I", trainQueue.getQueueArray(), 21);
        });
        button2.setLayoutY(200);
        button2.setOnAction(e -> {
            clear(pane1, gridPane);
            setTrain(pane1, gridPane, "TRAIN SEATS", Train, 42);
        });
        button4.setLayoutY(140);
        button4.setOnAction(e -> {
            clear(pane1, gridPane);
            setPane(pane1, gridPane, "TRAIN QUEUE II", trainQueue2.getQueueArray(), 21);
        });
        button3.setLayoutY(450);
        button3.setId("close");
        button3.setOnAction(e -> {
            stage.close();
        });
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

    /**
     * this method use to delete passenger from queue
     */
    private void delete() {
        System.out.println("===================================================================");
        System.out.println("******************    DELETE PASSENGER    ************************");
        System.out.println("===================================================================");

        Scanner sc = new Scanner(System.in);
        loop:
        while (true) {
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

    private boolean deletePassenger(Passenger[] array, int x) {
        boolean find = false;
        if (x != 0) {
            Scanner sc = new Scanner(System.in);
            System.out.print("Enter passenger name to delete = ");
            String name = sc.nextLine();
            System.out.println("-------------------------------------------------------");
            System.out.println("searching .....");
            Passenger deletePassenger;
            loop:
            for (int i = 0; i < x; i++) {
                if (array[i] != null) {
                    if (array[i].getName().equalsIgnoreCase(name)) {
                        deletePassenger = array[i];
                        for (int k = i; k < x; k++) {
                            //set the queue
                            if (k == x - 1) {
                                array[k] = null;
                            } else {
                                array[k] = array[k + 1];
                            }
                            find = true;
                            System.out.println(x);
                        }
                        break;
                    }
                }
            }

            if (find) {
                System.out.println("------------------------------------------------");
                System.out.println("delete completed !!");
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

    /**
     * this method used to show the simulate report
     * and add passenger to the train
     */
    public void simulation() {
        boolean find = false;
        ArrayList<Integer> time = new ArrayList<>();
        ArrayList<Integer> time2 = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> names2 = new ArrayList<>();
        ArrayList<Integer> seat = new ArrayList<>();
        ArrayList<Integer> seat2 = new ArrayList<>();
        while (!trainQueue.isEmpty() || !trainQueue2.isEmpty()) {
            int timeDelay = getRandInt() + getRandInt() + getRandInt();
            int timeDelay2 = getRandInt() + getRandInt() + getRandInt();
            if (!trainQueue.isEmpty()) {
                setTime(trainQueue.getQueueArray(), trainQueue.getLast(), timeDelay);
                bordToTrain(Train, trainQueue, trainQueue.getLast(), time, names, seat);
                trainQueue.setLast(-1);
            }
            if (!trainQueue2.isEmpty()) {
                setTime(trainQueue2.getQueueArray(), trainQueue2.getLast(), timeDelay2);
                bordToTrain(Train, trainQueue2, trainQueue2.getLast(), time2, names2, seat2);
                trainQueue2.setLast(-1);
            }
            find = true;
        }
        if (find) {
            dataSetting(time, names, seat, time2, names2, seat2);
        } else {
            System.out.println("nothing to add train");
        }
    }

    private void dataSetting(ArrayList<Integer> time, ArrayList<String> names, ArrayList<Integer> seat, ArrayList<Integer> time2, ArrayList<String> names2, ArrayList<Integer> seat2) {
        int len = time.size();
        int len2 = time2.size();
        ArrayList<Integer> tempTime = time;
        ArrayList<Integer> tempTime2 = time2;
        int min = 0;
        int max = 0;
        double avg = 0;
        int min2 = 0;
        int max2 = 0;
        double avg2 = 0;
        if (len > 0) {
            sort(tempTime);
            min = tempTime.get(0);
            max = tempTime.get(len - 1);
            if (len == 1) {
                avg = min;
            } else {
                avg = (max + min) / 2;
            }
        }
        if (len2 > 0) {
            sort(tempTime2);
            min2 = tempTime.get(0);
            max2 = tempTime.get(len - 1);
            if (len == 1) {
                avg2 = min2;
            } else {
                avg = (max2 + min2) / 2;
            }
        }
        showReport(max, min, avg, max2, min2, avg2, time, names, seat, time2, names2, seat2, len, len2);

    }

    private void bordToTrain(Passenger[] train, PassengerQueue queue, int x, ArrayList<Integer> time, ArrayList<String> names, ArrayList<Integer> seat) {
        train[queue.remove().getSeatNumber() - 1] = queue.remove();
        int waitTime = queue.remove().getSecondsInQueue();
        int seatNum = queue.remove().getSeatNumber();
        String pName = queue.remove().getName();
        time.add(waitTime);
        names.add(pName);
        seat.add(seatNum);
        setTheQueue(x, queue.getQueueArray(), 21);
    }

    private void setTime(Passenger[] passenger, int x, int time) {
        for (int i = 0; i < x; i++) {
            passenger[i].setSecondsInQueue(time);
        }
    }

    //sort the time arrayList this help to get max and= min times
    private void sort(ArrayList<Integer> time) {
        int tempTime;

        int lengthOfArrayList = time.size();
        for (int i = 0; i < lengthOfArrayList; i++) {
            for (int j = i + 1; j < lengthOfArrayList; j++) {
                if (time.get(i) > time.get(j)) {
                    tempTime = time.get(i);
                    time.set(i, time.get(j));
                    time.set(j, tempTime);
                }
            }
        }
    }

    //show the report
    private void showReport(int max, int min, double avg, int max2, int min2, double avg2, ArrayList<Integer> time, ArrayList<String> name, ArrayList<Integer> seat, ArrayList<Integer> time2, ArrayList<String> name2, ArrayList<Integer> seat2, int x, int y) {
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
        close.setLayoutX(275);
        close.setStyle("-fx-background-color: #eb4d4b");
        close.setOnAction(e -> {
            saveReport(max, min, avg, time, name, seat, x, "QUEUE I");
            saveReport(max2, min2, avg2, time2, name2, seat2, y, "QUEUE II");
            stage.close();
        });
        pane2.getChildren().add(close);
        Label title = new Label("SIMULATION REPORT");
        title.setLayoutX(175);
        title.setLayoutY(10);
        title.setStyle("-fx-text-fill:#4b7bec;-fx-font-size: 23px;-fx-font-weight: 700;-fx-font-family: 'Bitstream Vera Sans Mono'");

        Label title1 = new Label("queue I details");
        title1.setLayoutX(30);
        title1.setLayoutY(30);
        title1.setStyle("-fx-text-fill:#16d91d;-fx-font-size: 18px;-fx-font-weight: 800;-fx-padding: 20px;");

        Label minimum = new Label("minimum time = " + String.valueOf(min));
        minimum.setLayoutX(30);
        minimum.setLayoutY(90);
        minimum.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label maximum = new Label("maximum time = " + String.valueOf(max));
        maximum.setLayoutX(30);
        maximum.setLayoutY(110);
        maximum.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label average = new Label("average time = " + String.valueOf(avg));
        average.setLayoutX(30);
        average.setLayoutY(130);
        average.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        Label length = new Label("maximum length = " + String.valueOf(x));
        length.setLayoutX(30);
        length.setLayoutY(150);
        length.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label title2 = new Label("queue II details");
        title2.setLayoutX(30);
        title2.setLayoutY(170);
        title2.setStyle("-fx-text-fill:#16d91d;-fx-font-size: 18px;-fx-font-weight: 800;-fx-padding: 20px;");
        Label minimum2 = new Label("minimum time = " + String.valueOf(min2));

        minimum2.setLayoutX(30);
        minimum2.setLayoutY(220);
        minimum2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");
        Label maximum2 = new Label("maximum time = " + String.valueOf(max2));
        maximum2.setLayoutX(30);
        maximum2.setLayoutY(240);
        maximum2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label average2 = new Label("average time = " + String.valueOf(avg2));
        average2.setLayoutX(30);
        average2.setLayoutY(260);
        average2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        Label length2 = new Label("maximum length = " + String.valueOf(y));
        length2.setLayoutX(30);
        length2.setLayoutY(280);
        length2.setStyle("-fx-font-size:15px;-fx-text-fill: #130f40 ");

        pane.getChildren().addAll(title, title1, title2, minimum, maximum, average, length, minimum2, maximum2, average2, length2);
        VBox vBox = new VBox();
        pane1.setContent(vBox);
        vBox.setPadding(new Insets(10, 0, 15, 60));
        pane.setPadding(new Insets(10, 0, 20, 60));
        printQueues(vBox, "QUEUE ONE", x, time, name, seat);
        printQueues(vBox, "QUEUE TWO", y, time2, name2, seat2);
        stage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(borderPane, 600, 700);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void printQueues(VBox vBox, String label, int x, ArrayList<Integer> time, ArrayList<String> name, ArrayList<Integer> seat) {
        Label label1 = new Label(label);
        label1.setStyle("-fx-padding: 30px;-fx-font-weight: 800;-fx-text-fill: purple");
        Label menu = new Label("NO" + "\t" + "SEAT" + "\t" + "TIME" + "\t\t" + "NAME");
        menu.setStyle("-fx-text-fill: black;-fx-font-weight: 700");
        Separator separator = new Separator(Orientation.HORIZONTAL);
        vBox.getChildren().addAll(label1, menu, separator);
        for (int i = 0; i < x; i++) {
            Label data = new Label(String.valueOf(i + 1) + "\t" + seat.get(i) + "\t\t" + String.valueOf(time.get(i)) + "\t\t" + name.get(i));
            vBox.getChildren().add(data);
            data.setStyle("-fx-text-fill: #010813;-fx-padding: 5px;-fx-font-size: 13px");
        }
    }

    private void saveReport(int max, int min, double avg, ArrayList<Integer> time, ArrayList<String> name, ArrayList<Integer> seat, int x, String l) {
        try {

            String content = "\nminimum time = " + min + "\n" +
                    "maximum time = " + max + "\n" +
                    "average time = " + avg + "\n" +
                    "maximum length = " + x + "\n";
            File file = new File("report.txt");
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            LocalDate now = LocalDate.now();
            bw.write(String.valueOf(now));
            bw.write(l);
            bw.write(content);
            bw.write("---------------------------------------\n");
            bw.write("***   PASSENGER NAMES & DETAILS   ***\n");
            for (int i = 0; i < x; i++) {
                String content2 = name.get(i) + " " + seat.get(i) + " " + time.get(i) + "\n";
                bw.write(content2);
            }
            bw.write("------------------------------------------\n===========================================================================================\n");
            bw.close();
        } catch (IOException ioe) {
            System.out.println("Some thing went wrong");
            ioe.printStackTrace();
        }

    }

    //add data to waiting room

    /**
     * from this method check user inputs
     *
     * @param doc and add them to waiting room
     */
    private void addDataToWaitingRoom(MongoCollection<Document> doc) {
        FindIterable<Document> data = doc.find();
        LocalDate todayDate = LocalDate.now();
        loop:
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


    public void adding() {
        Scanner sc = new Scanner(System.in);
        String run = "";
        while (!run.equalsIgnoreCase("q")) {
            displayWaitingRoom();
            if (bookedCount > 0) {
                System.out.println("\n\"Q\"  to exit");
                System.out.print("\nEnter your seat number : ");
                String sNum = sc.next().toLowerCase();
                if (sNum.equalsIgnoreCase("q")) {
                    break;
                } else {
                    System.out.println("-------------------------------------------------------------------------");
                    if (sNum.matches("[0-9]+")) {
                        if (Integer.parseInt(sNum) > 0 && Integer.parseInt(sNum) <= 42) {
                            addTo(sNum);
                        } else {
                            System.out.println("-------------------------------------------------------------------------");
                            System.out.println(">>> wrong input");
                            System.out.println("-------------------------------------------------------------------------");
                        }
                    } else {
                        System.out.println("-------------------------------------------------------------------------");
                        System.out.println(">>> wrong input");
                        System.out.println("-------------------------------------------------------------------------");
                    }
                    System.out.println("\"q/Q\"  to exit");
                    System.out.println("\"ANY KEY\"  to add more");
                    System.out.print("\nEnter your option : ");
                    run = sc.next();
                }
            } else {
                break;
            }
        }
    }

    //form this method add passenger to the waiting room
    //and set passenger's attributes
    private void addTo(String sNum) {
        try {
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
                        String id = (String) documents[Integer.parseInt(sNum) - 1].get("id");
                        waitingRoom[waitRoomCount] = new Passenger();
                        waitingRoom[waitRoomCount].setName(name, sname);
                        waitingRoom[waitRoomCount].setSeatNumber(Integer.parseInt(seat));
                        waitRoomCount++;
                        documents[Integer.parseInt(sNum) - 1] = null;
                        bookedCount--;
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


    //display waiting room
    private void displayWaitingRoom() {
        System.out.println("\n===========================================================================");
        System.out.println("**************************     TODAY BOOKINGS     ***************************\n");
        boolean find = false;
        try {
            for (Document data : documents) {
                if (data != null) {
                    String seat = (String) data.get("seat");
                    String name = (String) data.get("name");
                    String sname = (String) data.get("sname");
                    System.out.println(seat + " " + name + " " + sname);
                    find = true;
                }
            }
            if (!find) {
                System.out.println("No one booked seat today");
            }
        } catch (Exception e) {
            System.out.println("-------------------------------------------------------------------------");
            System.out.println(">>> Some thing went wrong !!!");
            System.out.println("-------------------------------------------------------------------------");
        }
    }

    //select what is the station
    private void selectDestination(MongoCollection<Document> toBadulla, MongoCollection<Document> toColombo) {
        try {
            System.out.println("\n===========================================================================");
            System.out.println("*********************     DENUWARA MANIKE TRAIN      **********************");
            System.out.println("===========================================================================\n");
            Scanner sc = new Scanner(System.in);
            System.out.println("TODAY : " + LocalDate.now());
            /*
             *create connection to mongoDB
             * use two collection
             */

            loop:
            while (true) {
                System.out.println("\n===========================================================================");
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
}