package co.uk.vavr.trial.service;

import lombok.Getter;

@Getter
public class EventPublisher {
    Result result;

    public void sendToTopic(Result result) {
        throw new RuntimeException("not implemented");
    }

}
