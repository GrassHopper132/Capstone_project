package org.peopleshores.capstone_project.service;

import org.peopleshores.capstone_project.dto.AuthResponse;
import org.peopleshores.capstone_project.dto.LoginRequest;
import org.peopleshores.capstone_project.dto.RegisterRequest;
import org.peopleshores.capstone_project.entity.Role;
import org.peopleshores.capstone_project.entity.User;
import org.peopleshores.capstone_project.exception.DuplicateResourceException;
import org.peopleshores.capstone_project.exception.InvalidCredentialsException;
import org.peopleshores.capstone_project.exception.ResourceNotFoundException;
import org.peopleshores.capstone_project.repository.RoleRepository;
import org.peopleshores.capstone_project.repository.UserRepository;
import org.peopleshores.capstone_project.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    /**
     * Public self-registration always creates this role. A caller cannot choose
     * their own privilege level; promotion is an administrative action.
     */
    private static final String DEFAULT_ROLE = "VISITOR";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new account. The role field on the incoming request is read but
     * deliberately ignored: accepting it would let any anonymous caller mint an
     * ADMIN account, since this endpoint is unauthenticated by design.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("An account already exists for " + request.email());
        }

        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> ResourceNotFoundException.of("Role", DEFAULT_ROLE));

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.fullName(),
                role);

        userRepository.save(user);
        return toResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        String role = user.getRole().getName();
        return new AuthResponse(
                jwtService.generate(user.getEmail(), role),
                jwtService.getExpirySeconds(),
                user.getEmail(),
                user.getFullName(),
                role);
    }
}