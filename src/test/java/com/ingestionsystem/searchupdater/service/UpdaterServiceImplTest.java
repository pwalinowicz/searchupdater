package com.ingestionsystem.searchupdater.service;

import com.ingestionsystem.searchupdater.operation.RequestOperationType;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class UpdaterServiceImplTest {

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private OfferRepository offerRepository;

    @InjectMocks
    private UpdaterService service;

//    @BeforeEach
//    public void init() {
//        service = new UpdaterServiceImpl(productRepository, offerRepository);
//    }

    @Test
    void shouldDeleteProduct() {
        // given
//        var request = new OperationRequest();
//        request.setOperation(RequestOperationType.DELETE_PRODUCT);
//        request.setOfferId("123");
//        doReturn(Optional.empty()).when(productRepository).findById(any());
//
//        //when
//        service.getBaseSearchEngineOperations(request);
//        assertNull(service);
    }
    @Test
    void shouldInsertNewProduct() {

    }

    @Test
    void shouldUpdateExistingProduct() {

    }

    @Test
    void shouldNotUpdateExistingProductIfNoChanges() {

    }

    @Test
    void shouldUpdateExistingOffer() {

    }

    @Test
    void shouldNotUpdateExistingOfferIfNoChanges() {

    }
}
