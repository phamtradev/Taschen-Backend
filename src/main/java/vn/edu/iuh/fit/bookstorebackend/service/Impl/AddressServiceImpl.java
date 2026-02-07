package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Address;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.mapper.AddressMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.AddressRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.AddressService;

import java.util.List;
import java.util.stream.Collectors;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) throws IdInvalidException {
        User user = findUserById(userId);
        
        if (isSetAsDefault(request)) {
            unsetOtherDefaultAddresses(userId);
        }
        
        Address address = createAddressFromRequest(request, user);
        Address savedAddress = addressRepository.save(address);
        
        return addressMapper.toAddressResponse(savedAddress);
    }
    
    private User findUserById(Long userId) throws IdInvalidException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IdInvalidException("User not found: " + userId));
    }
    
    private boolean isSetAsDefault(AddressRequest request) {
        return request.getIsDefault() != null && request.getIsDefault();
    }
    
    private void unsetOtherDefaultAddresses(Long userId) {
        List<Address> otherAddresses = addressRepository.findByUserId(userId);
        otherAddresses.stream()
                .filter(address -> Boolean.TRUE.equals(address.getIsDefault()))
                .forEach(address -> address.setIsDefault(false));
        addressRepository.saveAll(otherAddresses);
    }
    
    private Address createAddressFromRequest(AddressRequest request, User user) {
        Address address = addressMapper.toAddress(request);
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        address.setUser(user);
        return address;
    }

    @Override
    public List<AddressResponse> getAddressesByUserId(Long userId) throws IdInvalidException {
        validateUserExists(userId);
        List<Address> addresses = addressRepository.findByUserId(userId);
        return mapToAddressResponseList(addresses);
    }

    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        try {
            return getAddressesByUserId(userId);
        } catch (IdInvalidException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void validateUserExists(Long userId) throws IdInvalidException {
        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("User not found: " + userId);
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) throws IdInvalidException {
        Address address = findAddressById(addressId);
        validateAddressBelongsToUser(address, userId);
        
        unsetOtherDefaultAddresses(userId);
        setAddressAsDefault(address);
    }
    
    private Address findAddressById(Long addressId) throws IdInvalidException {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new IdInvalidException("Address not found: " + addressId));
    }
    
    private void validateAddressBelongsToUser(Address address, Long userId) throws IdInvalidException {
        if (address.getUser() == null || !address.getUser().getId().equals(userId)) {
            throw new IdInvalidException("Address does not belong to user: " + userId);
        }
    }
    
    private void setAddressAsDefault(Address address) {
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    public AddressResponse getAddressById(Long id) throws IdInvalidException {
        Address address = findAddressById(id);
        return addressMapper.toAddressResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) throws IdInvalidException {
        Address address = findAddressById(id);
        
        updateAddressFields(address, request);
        handleDefaultAddressUpdate(address, request);
        
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressResponse(updatedAddress);
    }
    
    private void updateAddressFields(Address address, AddressRequest request) {
        if (request.getAddressType() != null) {
            address.setAddressType(request.getAddressType());
        }
        if (request.getStreet() != null) {
            address.setStreet(request.getStreet());
        }
        if (request.getDistrict() != null) {
            address.setDistrict(request.getDistrict());
        }
        if (request.getWard() != null) {
            address.setWard(request.getWard());
        }
        if (request.getCity() != null) {
            address.setCity(request.getCity());
        }
        if (request.getRecipientName() != null) {
            address.setRecipientName(request.getRecipientName());
        }
        if (request.getPhoneNumber() != null) {
            address.setPhoneNumber(request.getPhoneNumber());
        }
    }
    
    private void handleDefaultAddressUpdate(Address address, AddressRequest request) {
        if (request.getIsDefault() == null) {
            return;
        }
        
        if (request.getIsDefault()) {
            unsetOtherDefaultAddresses(address.getUser().getId(), address.getId());
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }
    }
    
    private void unsetOtherDefaultAddresses(Long userId, Long excludeAddressId) {
        List<Address> otherAddresses = addressRepository.findByUserId(userId);
        otherAddresses.stream()
                .filter(address -> !address.getId().equals(excludeAddressId))
                .filter(address -> Boolean.TRUE.equals(address.getIsDefault()))
                .forEach(address -> address.setIsDefault(false));
        addressRepository.saveAll(otherAddresses);
    }

    @Override
    public void deleteAddress(Long id) throws IdInvalidException {
        validateAddressExists(id);
        addressRepository.deleteById(id);
    }
    
    private void validateAddressExists(Long id) throws IdInvalidException {
        if (!addressRepository.existsById(id)) {
            throw new IdInvalidException("Address not found: " + id);
        }
    }

    private List<AddressResponse> mapToAddressResponseList(List<Address> addresses) {
        return addresses.stream()
                .map(addressMapper::toAddressResponse)
                .collect(Collectors.toList());
    }
}   


