package com.annex.backend.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class PropertiesService {
    @Value("${custom.backend.address}")
    public String backendAddress;
}
