package dev.midnightcoder.cafe.users;

import dev.midnightcoder.cafe.security.authtentication.PasswordMissMatchException;
import dev.midnightcoder.cafe.users.api.*;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-11
 */
@Service
@NullMarked
public class UserService implements UserLookup, UserRegistration {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserDto> findByIdentity(String identity) {
        return userRepository.findByIdentity(identity)
            .map(user -> new UserDto(
                user.getId(),
                user.getIdentity(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getRoles())
            );
    }

    @Override
    public Optional<UserDto> findByUserId(UUID userId) {
        return userRepository.findById(userId)
            .map(user -> new UserDto(
                user.getId(),
                user.getIdentity(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getRoles())
            );
    }

    @Override
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
            .map(user -> new UserDto(
                user.getId(),
                user.getIdentity(),
                user.getPasswordHash(),
                user.isEnabled(),
                user.getRoles())
            )
            .toList();
    }

    @Override
    public UserDto register(RegistrationRequest request) throws UserAlreadyExistsException, PasswordMissMatchException {
        if (userRepository.existsByIdentity(request.identity()))
            throw new UserAlreadyExistsException(request.identity());

        if (!request.password().equals(request.rePassword()))
            throw new PasswordMissMatchException();

        var passwordHash = passwordEncoder.encode(request.password());
        var user = new User(request.identity(), passwordHash, true);
        var savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), savedUser.getIdentity(), savedUser.getPasswordHash(), savedUser.isEnabled(), user.getRoles());
    }

    @Override
    public UserDto registerOAuthUser(String identity) throws UserAlreadyExistsException {
        if (userRepository.existsByIdentity(identity))
            throw new UserAlreadyExistsException(identity);

        var randomPassword = UUID.randomUUID().toString();
        var passwordHash = passwordEncoder.encode(randomPassword);
        var user = new User(identity, passwordHash, true);
        var savedUser = userRepository.save(user);

        return new UserDto(savedUser.getId(), savedUser.getIdentity(), savedUser.getPasswordHash(), savedUser.isEnabled(), savedUser.getRoles());
    }

    @Override
    public boolean existsByIdentity(String identity) {
        return userRepository.existsByIdentity(identity);
    }
}
