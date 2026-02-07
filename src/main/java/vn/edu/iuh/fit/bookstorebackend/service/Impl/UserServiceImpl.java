package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.SetUserRoleCodesRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.mapper.UserMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.RefreshTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        validateEmailNotExists(request.getEmail());
        
        User user = createUserFromRequest(request);
        setUserRoles(user, request.getRoleCodes());
        
        User savedUser = userRepository.save(user);
        return userMapper.toUserResponse(savedUser);
    }
    
    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }
    }
    
    private User createUserFromRequest(CreateUserRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.getGender());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(request.getActive() != null ? request.getActive() : true);
        return user;
    }
    
    private void setUserRoles(User user, Set<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        
        Set<Role> roles = roleCodes.stream()
                .map(this::findRoleByCode)
                .collect(Collectors.toSet());
        user.setRoles(roles);
    }
    
    private void setUserRolesFromList(User user, List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        
        Set<Role> roles = roleCodes.stream()
                .map(this::findRoleByCode)
                .collect(Collectors.toSet());
        user.setRoles(roles);
    }
    
    private Role findRoleByCode(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Role not found with code: " + code));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return mapToUserResponseList(users);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findUserById(id);
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = findUserByEmail(email);
        return userMapper.toUserResponse(user);
    }
    
    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserById(id);
        updateUserFields(user, request);
        updateUserRoles(user, request.getRoleCodes());
        
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }
    
    private void updateUserFields(User user, UpdateUserRequest request) {
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }
    }
    
    private void updateUserRoles(User user, List<String> roleCodes) {
        setUserRolesFromList(user, roleCodes);
    }

    @Override
    public UserResponse setActive(Long id, boolean active) {
        User user = findUserById(id);
        user.setActive(active);
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse setRolesByCodes(Long userId, SetUserRoleCodesRequest request) {
        User user = findUserById(userId);
        setUserRoles(user, request.getRoleCodes());
        
        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = findUserById(id);
        refreshTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    private List<UserResponse> mapToUserResponseList(List<User> users) {
        return users.stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }
}
