package vn.edu.iuh.fit.bookstorebackend.user.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.CreateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.SetUserRoleCodesRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.UserResponse;

import java.util.List;

@Service
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse setActive(Long id, boolean active);
    
    UserResponse setRolesByCodes(Long userId, SetUserRoleCodesRequest request);

    void deleteUser(Long id);
}
