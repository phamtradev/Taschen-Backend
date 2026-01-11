package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface AddressService {

    AddressResponse createAddress(Long userId, AddressRequest request) throws IdInvalidException;

    List<AddressResponse> getAddressesByUserId(Long userId) throws IdInvalidException;
    
    List<AddressResponse> getUserAddresses(Long userId) throws IdInvalidException;

    AddressResponse getAddressById(Long id) throws IdInvalidException;

    AddressResponse updateAddress(Long id, AddressRequest request) throws IdInvalidException;
    
    void deleteAddress(Long id) throws IdInvalidException;
    
    void setDefaultAddress(Long userId, Long addressId) throws IdInvalidException;
}


