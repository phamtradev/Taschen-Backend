package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateUserRequest;
// removed SetUserRolesRequest - using role codes API instead
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.SetUserRoleCodesRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.UserService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(request.getGender());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(request.getActive() != null ? request.getActive() : true);

        // Set roles if provided
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            Set<Role> roles = request.getRoleCodes().stream()
                    .map(code -> roleRepository.findByCode(code)
                            .orElseThrow(() -> new RuntimeException("Role not found with code: " + code)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        return convertToUserResponse(user);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

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

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public UserResponse setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setActive(active);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public UserResponse setRolesByCodes(Long userId, SetUserRoleCodesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Set<Role> roles = request.getRoleCodes().stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new RuntimeException("Role not found with code: " + code)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setGender(user.getGender());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setActive(user.isActive());

        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(Role::getCode)
                    .collect(Collectors.toList()));
        }

        if (user.getAddresses() != null) {
            List<AddressResponse> addrResp = user.getAddresses().stream().map(a -> {
                AddressResponse ar = new AddressResponse();
                ar.setId(a.getId());
                ar.setAddressType(a.getAddressType());
                ar.setStreet(a.getStreet());
                ar.setDistrict(a.getDistrict());
                ar.setWard(a.getWard());
                ar.setCity(a.getCity());
                ar.setRecipientName(a.getRecipientName());
                ar.setPhoneNumber(a.getPhoneNumber());
                ar.setIsDefault(a.getIsDefault());
                return ar;
            }).collect(Collectors.toList());
            response.setAddresses(addrResp);
        }

        return response;
    }
}
