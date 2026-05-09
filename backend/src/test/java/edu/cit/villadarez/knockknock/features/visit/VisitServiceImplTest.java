package edu.cit.villadarez.knockknock.features.visit;

import edu.cit.villadarez.knockknock.features.condo.Condo;
import edu.cit.villadarez.knockknock.features.condo.CondoRepository;
import edu.cit.villadarez.knockknock.features.user.User;
import edu.cit.villadarez.knockknock.features.user.UserRepository;
import edu.cit.villadarez.knockknock.shared.email.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceImplTest {

    @Mock
    private VisitRepository visitRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CondoRepository condoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VisitServiceImpl visitService;

    @Test
    void createVisitSetsScheduledStatusReferenceNumberAndPublishesEvent() {
        UUID visitorId = UUID.randomUUID();
        UUID condoId = UUID.randomUUID();
        User visitor = new User(visitorId, "Visitor", "visitor@example.com", "pw", "VISITOR");
        Condo condo = new Condo();
        condo.setCondoId(condoId);
        condo.setCode("SUN");
        condo.setName("Sunrise Towers");

        when(userRepository.findById(visitorId)).thenReturn(Optional.of(visitor));
        when(condoRepository.findById(condoId)).thenReturn(Optional.of(condo));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visit saved = visitService.createVisit(visitorId, condoId, "12A", "2026-05-20", "Family visit");

        assertThat(saved.getVisitor()).isSameAs(visitor);
        assertThat(saved.getCondo()).isSameAs(condo);
        assertThat(saved.getUnitNumber()).isEqualTo("12A");
        assertThat(saved.getPurpose()).isEqualTo("Family visit");
        assertThat(saved.getVisitDate()).isEqualTo(LocalDate.of(2026, 5, 20));
        assertThat(saved.getStatus()).isEqualTo("SCHEDULED");
        assertThat(saved.getReferenceNumber()).startsWith("KK-SUN-");

        verify(eventPublisher).publishEvent(any(VisitStatusChangedEvent.class));
    }

    @Test
    void cancelVisitOnlyAllowsTheOwningVisitorToCancelScheduledVisit() {
        UUID visitId = UUID.randomUUID();
        UUID visitorId = UUID.randomUUID();
        User visitor = new User(visitorId, "Visitor", "visitor@example.com", "pw", "VISITOR");
        Visit visit = new Visit();
        visit.setVisitId(visitId);
        visit.setVisitor(visitor);
        visit.setStatus("SCHEDULED");

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visit cancelled = visitService.cancelVisit(visitId, visitorId);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        verify(eventPublisher).publishEvent(any(VisitStatusChangedEvent.class));
    }

    @Test
    void cancelVisitRejectsNonOwner() {
        UUID visitId = UUID.randomUUID();
        User owner = new User(UUID.randomUUID(), "Owner", "owner@example.com", "pw", "VISITOR");
        Visit visit = new Visit();
        visit.setVisitId(visitId);
        visit.setVisitor(owner);
        visit.setStatus("SCHEDULED");

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitService.cancelVisit(visitId, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }

    @Test
    void getVisitsByUserMarksPastUncheckedScheduledVisitsAsMissed() {
        UUID visitorId = UUID.randomUUID();
        User visitor = new User(visitorId, "Visitor", "visitor@example.com", "pw", "VISITOR");
        Visit pastVisit = new Visit();
        pastVisit.setVisitor(visitor);
        pastVisit.setStatus("SCHEDULED");
        pastVisit.setVisitDate(LocalDate.now().minusDays(1));

        when(userRepository.findById(visitorId)).thenReturn(Optional.of(visitor));
        when(visitRepository.findByVisitor(visitor)).thenReturn(List.of(pastVisit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Visit> visits = visitService.getVisitsByUser(visitorId);

        assertThat(visits).containsExactly(pastVisit);
        assertThat(pastVisit.getStatus()).isEqualTo("MISSED");
        verify(eventPublisher).publishEvent(any(VisitStatusChangedEvent.class));
    }

    @Test
    void generateQrForVisitStoresQuickchartUrlForScheduledVisit() {
        UUID visitId = UUID.randomUUID();
        Visit visit = new Visit();
        visit.setVisitId(visitId);
        visit.setStatus("SCHEDULED");
        visit.setReferenceNumber("KK-SUN-2026-000001");

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));
        when(visitRepository.save(any(Visit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visit updated = visitService.generateQrForVisit(visitId);

        assertThat(updated.getQrImageUrl())
                .startsWith("https://quickchart.io/qr?text=KK-SUN-2026-000001")
                .contains("size=300");
    }

    @Test
    void checkInVisitRequiresVisitToBelongToAdminCondo() {
        UUID visitId = UUID.randomUUID();
        UUID adminCondoId = UUID.randomUUID();
        Condo anotherCondo = new Condo();
        anotherCondo.setCondoId(UUID.randomUUID());
        Visit visit = new Visit();
        visit.setVisitId(visitId);
        visit.setStatus("SCHEDULED");
        visit.setCondo(anotherCondo);

        when(visitRepository.findById(visitId)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitService.checkInVisit(visitId, adminCondoId, "Admin", "CONDOMINIUM_ADMIN"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }
}
