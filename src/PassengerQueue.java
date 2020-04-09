public class PassengerQueue {
    private Passenger[] queueArray = new Passenger[21];
    private static int first = 0;
    private int last = 0;
    private int maxStayInQueue;
    private int maxLength;

    public int getMaxStayInQueue() {
        return maxStayInQueue;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getLast() {
        return last;
    }

    public int getFirst() {
        return first;
    }

    public void setQueueArray(Passenger[] queueArray) {
        this.queueArray = queueArray;
    }

    public static void setFirst(int first) {
        PassengerQueue.first = first;
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
        last++;
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

    }

    public Passenger[] getQueueArray(){
        return queueArray;
    }
}