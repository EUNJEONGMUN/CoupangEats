package com.example.demo.src.store;

import com.example.demo.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final StoreDao storeDao;
    private final StoreProvider storeProvider;
    private final JwtService jwtService;
    private final int FAIL = 0;

    @Autowired
    public StoreService(StoreDao storeDao, StoreProvider storeProvider, JwtService jwtService){
        this.storeDao = storeDao;
        this.jwtService = jwtService;
        this.storeProvider = storeProvider;

    }

}
