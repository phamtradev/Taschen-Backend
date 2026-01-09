package vn.edu.iuh.fit.bookstorebackend.service;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.SetUserRolesRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;

import java.util.List;

@Service
public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse setActive(Long id, boolean active);

    UserResponse setRoles(Long userId, SetUserRolesRequest request);

    void deleteUser(Long id);
}
