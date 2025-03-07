package com.example.security.mapper;

import com.example.security.dto.CustomerDto;
import com.example.security.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomMapper {
    CustomMapper dtoCustomMapper = Mappers.getMapper(CustomMapper.class);

    CustomerDto cutomerToCustomerDto(Customer customer);
}