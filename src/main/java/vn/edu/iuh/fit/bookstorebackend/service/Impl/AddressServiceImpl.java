package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Address;
import vn.edu.iuh.fit.bookstorebackend.model.User;
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

    @Override
    @Transactional
    public AddressResponse createAddress(Long userId, AddressRequest request) throws IdInvalidException {
        User user = userRepository.findById(userId).orElseThrow(() -> new IdInvalidException("User not found: " + userId));

        // if new address is default, unset other defaults
        if (request.getIsDefault() != null && request.getIsDefault()) {
            List<Address> others = addressRepository.findByUserId(userId);
            for (Address o : others) {
                if (Boolean.TRUE.equals(o.getIsDefault())) {
                    o.setIsDefault(false);
                }
            }
            addressRepository.saveAll(others);
        }

        Address address = new Address();
        address.setAddressType(request.getAddressType());
        address.setStreet(request.getStreet());
        address.setDistrict(request.getDistrict());
        address.setWard(request.getWard());
        address.setCity(request.getCity());
        address.setRecipientName(request.getRecipientName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        address.setUser(user);

        Address saved = addressRepository.save(address);
        return toResponse(saved);
    }

    @Override
    public List<AddressResponse> getAddressesByUserId(Long userId) throws IdInvalidException {
        if (!userRepository.existsById(userId)) {
            throw new IdInvalidException("User not found: " + userId);
        }
        return addressRepository.findByUserId(userId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AddressResponse> getUserAddresses(Long userId) {
        try {
            return getAddressesByUserId(userId);
        } catch (IdInvalidException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long addressId) throws IdInvalidException {
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new IdInvalidException("Address not found: " + addressId));
        if (address.getUser() == null || !address.getUser().getId().equals(userId)) {
            throw new IdInvalidException("Address does not belong to user: " + userId);
        }

        List<Address> others = addressRepository.findByUserId(userId);
        for (Address o : others) {
            if (!o.getId().equals(addressId) && Boolean.TRUE.equals(o.getIsDefault())) {
                o.setIsDefault(false);
            }
        }
        addressRepository.saveAll(others);

        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    public AddressResponse getAddressById(Long id) throws IdInvalidException {
        Address address = addressRepository.findById(id).orElseThrow(() -> new IdInvalidException("Address not found: " + id));
        return toResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long id, AddressRequest request) throws IdInvalidException {
        Address address = addressRepository.findById(id).orElseThrow(() -> new IdInvalidException("Address not found: " + id));

        if (request.getAddressType() != null) address.setAddressType(request.getAddressType());
        if (request.getStreet() != null) address.setStreet(request.getStreet());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict());
        if (request.getWard() != null) address.setWard(request.getWard());
        if (request.getCity() != null) address.setCity(request.getCity());
        if (request.getRecipientName() != null) address.setRecipientName(request.getRecipientName());
        if (request.getPhoneNumber() != null) address.setPhoneNumber(request.getPhoneNumber());

        if (request.getIsDefault() != null) {
            if (request.getIsDefault()) {
                List<Address> others = addressRepository.findByUserId(address.getUser().getId());
                for (Address o : others) {
                    if (!o.getId().equals(address.getId()) && Boolean.TRUE.equals(o.getIsDefault())) {
                        o.setIsDefault(false);
                    }
                }
                addressRepository.saveAll(others);
                address.setIsDefault(true);
            } else {
                address.setIsDefault(false);
            }
        }

        Address updated = addressRepository.save(address);
        return toResponse(updated);
    }

    @Override
    public void deleteAddress(Long id) throws IdInvalidException {
        if (!addressRepository.existsById(id)) throw new IdInvalidException("Address not found: " + id);
        addressRepository.deleteById(id);
    }

    private AddressResponse toResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setAddressType(address.getAddressType());
        response.setStreet(address.getStreet());
        response.setDistrict(address.getDistrict());
        response.setWard(address.getWard());
        response.setCity(address.getCity());
        response.setRecipientName(address.getRecipientName());
        response.setPhoneNumber(address.getPhoneNumber());
        response.setIsDefault(address.getIsDefault());
        return response;
    }
}   


