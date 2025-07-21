package learn.tripsplit.domain;

import java.util.ArrayList;
import java.util.List;

public class Result<T> {
    private final ArrayList<String> messages = new ArrayList<>();
    private ResultType type = ResultType.SUCCESS;
    private T payload;

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    public ResultType getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    public List<String> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addMessage(String message, ResultType type) {
        messages.add(message);
        if (type == ResultType.INVALID && this.type != ResultType.NOT_FOUND) {
            this.type = type;
        } else if (type == ResultType.NOT_FOUND) {
            this.type = type;
        }
    }
}