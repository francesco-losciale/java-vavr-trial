package co.uk.vavr.trial.service;

import io.vavr.collection.Stream;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

@AllArgsConstructor
public class Service {

    private PersonService personService;
    private NameService nameService;
    private DatabaseRepository repository;
    private EventPublisher eventPublisher;

    public void runEnrichment() {
        createEitherFailureOrEnrichmentResult()
            .flatMap(this::addWeight)
            .flatMap(this::addName)
            .map(this::sendToTopic)
            .mapLeft(this::persistFailure);
    }

    private static Either<Stream<String>, Result> createEitherFailureOrEnrichmentResult() {
        return right(new Result());
    }

    private Either<Stream<String>, Result> addWeight(Result result) {
        return Try.of(() -> personService.getWeight())
            .toEither()
            .mapLeft((throwable) -> Stream.of("Weight not found"))
            .map((weight) -> {
                result.setWeight(weight);
                return result;
            });
    }

    private Either<Stream<String>, Result> addName(Result result) {
        return Try.of(() -> nameService.getName())
            .toEither()
            .mapLeft((throwable) -> Stream.of("Name not found"))
            .map((weight) -> {
                result.setName("good");
                return result;
            });
    }

    private Either<Stream<String>, Result> persistFailure(Stream<String> errors) {
        errors.forEach(repository::save);
        return left(errors);
    }

    private Either<Stream<String>, Result> sendToTopic(Result result) {
        eventPublisher.sendToTopic(result);
        return right(result);
    }
}
