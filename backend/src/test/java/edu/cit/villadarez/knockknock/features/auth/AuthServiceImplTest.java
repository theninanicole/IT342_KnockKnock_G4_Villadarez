package edu.cit.villadarez.knockknock.features.auth;

import edu.cit.villadarez.knockknock.features.condo.Condo;
import edu.cit.villadarez.knockknock.features.condo.CondoRepository;
import edu.cit.villadarez.knockknock.features.user.User;
import edu.cit.villadarez.knockknock.features.user.UserRepository;
import edu.cit.villadarez.knockknock.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CondoRepository condoRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerVisitorCreatesVisitorAndReturnsToken() {
        RegisterVisitorRequest request = new RegisterVisitorRequest();
        request.setFullName("Nina Visitor");
        request.setEmail("visitor@example.com");
        request.setContactNumber("9171234567");
        request.setPassword("Password1");
        request.setConfirmPassword("Password1");

        when(userRepository.existsByEmail("visitor@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(jwtUtils.generateJwtToken("visitor@example.com")).thenReturn("jwt-token");

        Object result = authService.registerVisitor(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getFullName()).isEqualTo("Nina Visitor");
        assertThat(savedUser.getEmail()).isEqualTo("visitor@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
        assertThat(savedUser.getRole()).isEqualTo("VISITOR");
        assertThat(savedUser.getAuthProvider()).isEqualTo("email");
        assertThat(savedUser.getContactNumber()).isEqualTo(9171234567L);

        assertThat(result).isInstanceOf(Map.class);
        assertThat(((Map<?, ?>) result).get("token")).isEqualTo("jwt-token");
    }

    @Test
    void registerVisitorRejectsDuplicateEmail() {
        RegisterVisitorRequest request = new RegisterVisitorRequest();
        request.setEmail("visitor@example.com");
        request.setPassword("Password1");
        request.setConfirmPassword("Password1");

        when(userRepository.existsByEmail("visitor@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerVisitor(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void registerCondoAdminCreatesCondoAndAdminUser() {
        RegisterCondoAdminRequest request = new RegisterCondoAdminRequest();
        request.setFullName("Admin User");
        request.setEmail("admin@example.com");
        request.setContactNumber("9177654321");
        request.setPassword("Password1");
        request.setConfirmPassword("Password1");
        request.setCondoName("Sunrise Towers");
        request.setCondoAddress("Cebu City");

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(condoRepository.existsByCode("SUN")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("encoded-password");
        when(jwtUtils.generateJwtToken("admin@example.com")).thenReturn("admin-token");

        Object result = authService.registerCondoAdmin(request);

        ArgumentCaptor<Condo> condoCaptor = ArgumentCaptor.forClass(Condo.class);
        verify(condoRepository).save(condoCaptor.capture());
        Condo savedCondo = condoCaptor.getValue();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedCondo.getName()).isEqualTo("Sunrise Towers");
        assertThat(savedCondo.getAddress()).isEqualTo("Cebu City");
        assertThat(savedCondo.getStatus()).isEqualTo("ACTIVE");
        assertThat(savedCondo.getCode()).isEqualTo("SUN");

        assertThat(savedUser.getRole()).isEqualTo("CONDOMINIUM_ADMIN");
        assertThat(savedUser.getCondo()).isSameAs(savedCondo);
        assertThat(((Map<?, ?>) result).get("token")).isEqualTo("admin-token");
    }
}
