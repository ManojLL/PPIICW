public class PassengerQueue {
    private static final int QUEUE_CAPACITY = 21;
    private Passenger[] queueArray = new Passenger[QUEUE_CAPACITY];
    private static int first = 0;
    private int last = 0;
    private int maxStayInQueue = 0;
    private int maxLength = 0;
    private int minStayInQueue = 0;

    public void setMinStayInQueue(int minStayInQueue) {
        this.minStayInQueue = minStayInQueue;
    }

    public int getMinStayInQueue() {
        return minStayInQueue;
    }

    public int getMaxStayInQueue() {
        return maxStayInQueue;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getLast() {
        return last;
    }

    public void setLast(int i) {
        this.last = last + i;
    }

    public void setMaxStayInQueue(int maxStayInQueue) {
        this.maxStayInQueue = maxStayInQueue;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void add(Passenger next) {
        queueArray[last] = next;
    }

    public Passenger remove() {
        return queueArray[first];
    }

    public boolean isEmpty() {
        return last == first;
    }

    public boolean isFull() {
        return last == 21;
    }

    public void display() {
        System.out.println("-----------------------------------------");
        System.out.println("***       add passenger details       ***");
        System.out.println("-----------------------------------------");
        System.out.println("name = " + queueArray[last].getName());
        System.out.println("seat = " + queueArray[last].getSeatNumber());
    }

    public Passenger[] getQueueArray() {
        return queueArray;
    }
}