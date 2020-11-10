import co.uk.vavr.trial.service.NameService;
import co.uk.vavr.trial.service.DatabaseRepository;
import co.uk.vavr.trial.service.Result;
import co.uk.vavr.trial.service.PersonService;
import co.uk.vavr.trial.service.Service;
import co.uk.vavr.trial.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static java.math.BigDecimal.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServiceTest {

    public static final BigDecimal WEIGHT = valueOf(200.99);

    @Captor
    ArgumentCaptor<Result> argumentCaptor;

    @Mock
    EventPublisher eventPublisher;

    @Mock
    DatabaseRepository databaseRepository;

    @Mock
    PersonService personService;

    @Mock
    NameService nameService;

    @InjectMocks
    Service service;

    @Test
    void testServiceWeightEnrichment() {
        when(personService.getWeight()).thenReturn(WEIGHT);

        service.runEnrichment();

        verify(eventPublisher).sendToTopic(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getWeight()).isEqualTo(WEIGHT);
    }

    @Test
    void testErrorHandlingWithWeightEnrichment() {
        doThrow(RuntimeException.class).when(personService).getWeight();

        service.runEnrichment();

        verifyNoInteractions(eventPublisher);
        verify(databaseRepository).save("Weight not found");
    }

    @Test
    void testServiceNameEnrichment() {
        when(nameService.getName()).thenReturn("good");

        service.runEnrichment();

        verify(eventPublisher).sendToTopic(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getName()).isEqualTo("good");
        verifyNoInteractions(databaseRepository);
    }

    @Test
    void testServiceNameAndPricingEnrichment() {
        when(personService.getWeight()).thenReturn(WEIGHT);
        when(nameService.getName()).thenReturn("good");

        service.runEnrichment();

        verify(eventPublisher).sendToTopic(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getName()).isEqualTo("good");
        assertThat(argumentCaptor.getValue().getWeight()).isEqualTo(WEIGHT);
        verifyNoInteractions(databaseRepository);
    }

    @Test
    void testErrorHandlingWithNameEnrichment() {
        doThrow(RuntimeException.class).when(nameService).getName();

        service.runEnrichment();

        verifyNoInteractions(eventPublisher);
        verify(databaseRepository).save("Name not found");
    }

    @Test
    void testEitherCannotHandleBothWeightAndNameEnrichmentFailures() {
        doThrow(RuntimeException.class).when(personService).getWeight();
        doThrow(RuntimeException.class).when(nameService).getName();

        service.runEnrichment();

        verifyNoInteractions(eventPublisher);
        verify(databaseRepository).save("Weight not found");
        verify(databaseRepository, times(0)).save("Name not found");
    }
}
