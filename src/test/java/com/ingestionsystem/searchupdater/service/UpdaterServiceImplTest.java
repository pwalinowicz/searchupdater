package com.ingestionsystem.searchupdater.service;

import com.ingestionsystem.searchupdater.model.Offer;
import com.ingestionsystem.searchupdater.model.Product;
import com.ingestionsystem.searchupdater.operation.*;
import com.ingestionsystem.searchupdater.repository.OfferRepository;
import com.ingestionsystem.searchupdater.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
public class UpdaterServiceImplTest {

    @MockitoSpyBean
    private ProductRepository productRepository;

    @MockitoSpyBean
    private OfferRepository offerRepository;

    @InjectMocks
    private UpdaterServiceImpl service;

    @BeforeEach
    public void init() {
        service = new UpdaterServiceImpl(productRepository, offerRepository);
    }

    @AfterEach
    public void clean() {
        offerRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void shouldDeleteExistingProductWithoutOffers() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var request = new IngestionRequest(
                RequestOperationType.DELETE_PRODUCT,
                null,
                null,
                productId,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(productRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldDeleteExistingProductWithOffer() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        offerRepository.save(new Offer("offerA", "offerA", product));

        var request = new IngestionRequest(
                RequestOperationType.DELETE_PRODUCT,
                null,
                null,
                productId,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).delete(any());
        assertThat(productRepository.findAll().size()).isEqualTo(0);
        var savedOfferOptional = offerRepository.findById("offerA");
        assertThat(savedOfferOptional.isPresent()).isTrue();
        savedOfferOptional.ifPresent(offer -> assertThat(offer.getProduct()).isNull());
    }

    @Test
    void shouldDoNothingIfProductIfNotExistsForDeleteOperation() {
        // given
        var request = new IngestionRequest(
                RequestOperationType.DELETE_PRODUCT,
                null,
                null,
                "productA",
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        verify(productRepository, times(0)).delete(any());

    }

    @Test
    void shouldNotUpsertSearchableProductForNewProductWithoutOffers() {
        // given
        var productId = "productA";
        var productName = "productAName";
        var request = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpsertSearchableProductForNewProductWithOffers() {
        // given
        var productId = "productA";
        var productName = "productAName";
        var offerRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                "offerA",
                "offerA",
                null,
                "productA",
                null
        );

        var productRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        //when
        service.getBaseSearchEngineOperations(offerRequest);
        var responseOperations = service.getBaseSearchEngineOperations(productRequest);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (UpsertOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        assertThat(operation.getProductName()).isEqualTo(productName);
        assertThat(operation.getOfferNames().size()).isEqualTo(1);
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateProductNameAndNotUpdateOperationForProductWithoutOffers() {
        // given
        var productId = "productA";
        var productName = "productAName";
        var newProductName = "newProductName";

        var productRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        var updateProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                newProductName
        );

        //when
        service.getBaseSearchEngineOperations(productRequest);
        var responseOperations = service.getBaseSearchEngineOperations(updateProductRequest);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(offerRepository.findAll().size()).isEqualTo(0);
        var products = productRepository.findAll();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.getFirst().getName()).isEqualTo(newProductName);
    }

    @Test
    void shouldUpdateProductNameAndUpdateOperationForProductWithExistingOffers() {
        // given
        var productId = "productA";
        var productName = "productAName";
        var newProductName = "newProductName";
        var offerRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                "offerA",
                "offerA",
                null,
                "productA",
                null
        );

        var productRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        var updateProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                newProductName
        );

        //when
        service.getBaseSearchEngineOperations(offerRequest);
        service.getBaseSearchEngineOperations(productRequest);
        var responseOperations = service.getBaseSearchEngineOperations(updateProductRequest);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (UpsertOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        assertThat(operation.getProductName()).isEqualTo(newProductName);
        assertThat(operation.getOfferNames().size()).isEqualTo(1);
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldNotUpdateExistingProductIfNoChanges() {
        // given
        var productId = "productA";
        var productName = "productAName";
        var product = new Product(productId, "productAName");
        productRepository.save(product);

        var updateProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        //when
        service.getBaseSearchEngineOperations(updateProductRequest);
        var responseOperations = service.getBaseSearchEngineOperations(updateProductRequest);

        //then
        verify(productRepository, times(0)).updateOrInsert(any());
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(offerRepository.findAll().size()).isEqualTo(0);
        var products = productRepository.findAll();
        assertThat(products.size()).isEqualTo(1);
        assertThat(products.getFirst().getName()).isEqualTo(productName);
    }

    @Test
    void shouldUpdateExistingOfferForProductWithOneOfferWithNullProduct() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var offerId = "offerA";
        var offerName = "offerA";
        offerRepository.save(new Offer(offerId, offerName, product));

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (DeleteOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        offerRepository.findById(offerId).ifPresent(offer -> assertThat(offer.getProduct()).isNull());

        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingOfferForProductWithOneOfferWithNewProduct() {
        // given
        var productId1 = "productA";
        var productName1 = "productAName";
        var productId2 = "productB";
        var productName2 = "productBName";
        var product1 = new Product(productId1, productName1);
        productRepository.save(product1);
        productRepository.save(new Product(productId2, productName2));
        var offerId = "offerA";
        var offerName = "offerA";
        offerRepository.save(new Offer(offerId, offerName, product1));

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                productId2,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(2);
        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT)
                .count()
        ).isEqualTo(1);
        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT)
                .count()
        ).isEqualTo(1);

        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT)
                .map(BaseSearchEngineOperation::getProductId)
                .findFirst().get()
        ).isEqualTo(productId1);
        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT)
                .map(BaseSearchEngineOperation::getProductId)
                .findFirst().get()
        ).isEqualTo(productId2);

        assertThat(offerRepository.findById(offerId).isPresent()).isTrue();
        offerRepository.findById(offerId).ifPresent(offer -> assertThat(offer.getProduct().getId()).isEqualTo(productId2));

        assertThat(productRepository.findById(productId1).isPresent()).isTrue();
        assertThat(productRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void shouldUpdateExistingOfferForProductWithMoreThanOneOfferWithNullProduct() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var offerId1 = "offerA";
        var offerName1 = "offerA";
        var offerId2 = "offerB";
        var offerName2 = "offerB";
        offerRepository.save(new Offer(offerId1, offerName1, product));
        offerRepository.save(new Offer(offerId2, offerName2, product));

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId1,
                offerName1,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (UpsertOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);

        assertThat(offerRepository.findById(offerId1).isPresent()).isTrue();
        assertThat(offerRepository.findById(offerId2).isPresent()).isTrue();

        offerRepository.findById(offerId1).ifPresent(offer -> assertThat(offer.getProduct()).isNull());
        offerRepository.findById(offerId2).ifPresent(offer -> assertThat(offer.getProduct().getId()).isEqualTo(productId));

        assertThat(productRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateExistingOfferForProductWithMoreThanOneOfferWithNewProduct() {
        // given
        var productId1 = "productA";
        var productId2 = "productB";
        var product1 = new Product(productId1, "productAName");
        var product2 = new Product(productId2, "productAName");
        productRepository.save(product1);
        productRepository.save(product2);
        var offerId1 = "offerA";
        var offerName1 = "offerA";
        var offerId2 = "offerB";
        var offerName2 = "offerB";
        var offerId3 = "offerC";
        var offerName3 = "offerC";
        offerRepository.save(new Offer(offerId1, offerName1, product1));
        offerRepository.save(new Offer(offerId2, offerName2, product1));
        offerRepository.save(new Offer(offerId3, offerName3, product2));

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId1,
                offerName1,
                null,
                productId2,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(2);
        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT)
                .count()
        ).isEqualTo(2);

        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT)
                .map(BaseSearchEngineOperation::getProductId)
                .filter(id -> id.equals(productId1))
                .count()
        ).isEqualTo(1);
        assertThat(responseOperations.stream()
                .filter(o -> o.getOperationType() == SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT)
                .map(BaseSearchEngineOperation::getProductId)
                .filter(id -> id.equals(productId2))
                .count()
        ).isEqualTo(1);

        assertThat(offerRepository.findById(offerId1).isPresent()).isTrue();
        assertThat(offerRepository.findById(offerId2).isPresent()).isTrue();
        assertThat(offerRepository.findById(offerId3).isPresent()).isTrue();

        offerRepository.findById(offerId1).ifPresent(offer -> assertThat(offer.getProduct().getId()).isEqualTo(productId2));
        offerRepository.findById(offerId2).ifPresent(offer -> assertThat(offer.getProduct().getId()).isEqualTo(productId1));
        offerRepository.findById(offerId3).ifPresent(offer -> assertThat(offer.getProduct().getId()).isEqualTo(productId2));

        assertThat(productRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void shouldNotUpdateExistingOfferIfNoChanges() {
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var offerId1 = "offerA";
        var offerName1 = "offerA";

        offerRepository.save(new Offer(offerId1, offerName1, product));

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId1,
                offerName1,
                null,
                productId,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
    }

    @Test
    void shouldDeleteExistingOfferForOfferWithProductWithOneOffer() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var offerId1 = "offerA";
        var offerName1 = "offerA";
        offerRepository.save(new Offer(offerId1, offerName1, product));

        var request = new IngestionRequest(
                RequestOperationType.DELETE_OFFER,
                offerId1,
                null,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (DeleteOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        verify(offerRepository, times(1)).deleteById(any());
        assertThat(offerRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldDeleteExistingOfferForOfferWithProductWithMoreThanOneOffer() {
        // given
        var productId = "productA";
        var product = new Product(productId, "productAName");
        productRepository.save(product);
        var offerId1 = "offerA";
        var offerName1 = "offerA";
        offerRepository.save(new Offer(offerId1, offerName1, product));
        var offerId2 = "offerB";
        var offerName2 = "offerB";
        offerRepository.save(new Offer(offerId2, offerName2, product));

        var request = new IngestionRequest(
                RequestOperationType.DELETE_OFFER,
                offerId1,
                null,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(1);
        var operation = (UpsertOperation) responseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        verify(offerRepository, times(1)).deleteById(any());
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldDeleteExistingOfferWithoutProduct() {
        // given
        var offerId1 = "offerA";
        var offerName1 = "offerA";
        offerRepository.save(new Offer(offerId1, offerName1));
        var request = new IngestionRequest(
                RequestOperationType.DELETE_OFFER,
                offerId1,
                null,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        verify(offerRepository, times(1)).deleteById(any());
        assertThat(offerRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldDoNothingForDeleteIfOfferNotExists() {
        // given
        var offerId1 = "offerA";
        var request = new IngestionRequest(
                RequestOperationType.DELETE_OFFER,
                offerId1,
                null,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        verify(offerRepository, times(0)).deleteById(any());
        assertThat(offerRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldUpdateOfferForNullProductWithNullProduct() {
        // given
        var offerId = "offerA";
        var offerName = "offerA";

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                null,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);
        var secondResponseOperations = service.getBaseSearchEngineOperations(request);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(secondResponseOperations.size()).isEqualTo(0);
        assertThat(productRepository.findAll().size()).isEqualTo(0);
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateOfferWithNullProductWithNewProductWithoutOffers() {
        // given
        var offerId = "offerA";
        var offerName = "offerA";
        var relatedProductId = "productA";

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                null,
                null
        );
        var secondRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                relatedProductId,
                null
        );

        //when
        var responseOperations = service.getBaseSearchEngineOperations(request);
        var secondResponseOperations = service.getBaseSearchEngineOperations(secondRequest);

        //then
        assertThat(responseOperations.size()).isEqualTo(0);
        assertThat(secondResponseOperations.size()).isEqualTo(0);
        assertThat(productRepository.findAll().size()).isEqualTo(1);
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldUpdateOfferWithNullProductWithNewProductWithOffers() {
        // given
        var offerId = "offerA";
        var offerName = "offerA";
        var relatedProductId = "productA";
        var productName = "productAName";

        var request = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                null,
                null
        );
        var secondRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                relatedProductId,
                null
        );
        var thirdRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                relatedProductId,
                null,
                productName
        );

        //when
        service.getBaseSearchEngineOperations(request);
        service.getBaseSearchEngineOperations(secondRequest);
        var thirdResponseOperations = service.getBaseSearchEngineOperations(thirdRequest);

        //then
        assertThat(thirdResponseOperations.size()).isEqualTo(1);
        var operation = (UpsertOperation) thirdResponseOperations.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(relatedProductId);
        assertThat(productRepository.findAll().size()).isEqualTo(1);
        assertThat(offerRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    void shouldRemoveProductCorrectlyAfterDeletingOffer() {
        // given
        var offerId = "offerA";
        var offerName = "offerAName";
        var productId = "productA";
        var productName = "productAName";

        var upsertProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        var upsertOfferRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                productId,
                null
        );

        var deleteOfferRequest = new IngestionRequest(
                RequestOperationType.DELETE_OFFER,
                offerId,
                null,
                null,
                null,
                null
        );

        var deleteProductRequest = new IngestionRequest(
                RequestOperationType.DELETE_PRODUCT,
                null,
                null,
                productId,
                null,
                null
        );

        //when
        var upsertProductResponse = service.getBaseSearchEngineOperations(upsertProductRequest);
        var upsertOfferResponse = service.getBaseSearchEngineOperations(upsertOfferRequest);
        var deleteOfferResponse = service.getBaseSearchEngineOperations(deleteOfferRequest);
        var deleteProductResponse = service.getBaseSearchEngineOperations(deleteProductRequest);

        //then
        assertThat(upsertProductResponse.size()).isEqualTo(0);
        assertThat(upsertOfferResponse.size()).isEqualTo(1);
        assertThat(deleteOfferResponse.size()).isEqualTo(1);
        assertThat(deleteProductResponse.size()).isEqualTo(0); //product should have been already deleted in previous step

        var upsertOperation = (UpsertOperation) upsertOfferResponse.getFirst();
        assertThat(upsertOperation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(upsertOperation.getProductId()).isEqualTo(productId);
        assertThat(upsertOperation.getOfferNames().getFirst()).isEqualTo(offerName);

        var operation = deleteOfferResponse.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.DELETE_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        verify(productRepository, times(1)).delete(any());
        assertThat(productRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    void shouldUpdateOfferNameCorrectly() {
        // given
        var offerId = "offerA";
        var offerName = "offerAName";
        var newOfferName = "newOfferAName";
        var productId = "productA";
        var productName = "productAName";

        var upsertProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        var upsertOfferRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                productId,
                null
        );

        var newOfferNameRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                newOfferName,
                null,
                productId,
                null
        );

        //when
        var upsertProductResponse = service.getBaseSearchEngineOperations(upsertProductRequest);
        var upsertOfferResponse = service.getBaseSearchEngineOperations(upsertOfferRequest);
        var newOfferNameResponse = service.getBaseSearchEngineOperations(newOfferNameRequest);

        //then
        assertThat(upsertProductResponse.size()).isEqualTo(0);
        assertThat(upsertOfferResponse.size()).isEqualTo(1);
        assertThat(newOfferNameResponse.size()).isEqualTo(1);

        var upsertOperation = (UpsertOperation) upsertOfferResponse.getFirst();
        assertThat(upsertOperation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(upsertOperation.getProductId()).isEqualTo(productId);
        assertThat(upsertOperation.getOfferNames().getFirst()).isEqualTo(offerName);

        var operation = (UpsertOperation) newOfferNameResponse.getFirst();
        assertThat(operation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(operation.getProductId()).isEqualTo(productId);
        assertThat(operation.getOfferNames().getFirst()).isEqualTo(newOfferName);
    }

    @Test
    void shouldUpdateOfferNameCorrectlyForProductWithMoreThanOneOffer() {
        // given
        var offerId = "offerA";
        var offerName = "offerAName";
        var newOfferName = "newOfferAName";
        var secondOfferId = "offerB";
        var secondOfferName = "offerBName";
        var productId = "productA";
        var productName = "productAName";

        var upsertProductRequest = new IngestionRequest(
                RequestOperationType.UPSERT_PRODUCT,
                null,
                null,
                productId,
                null,
                productName
        );

        var upsertOfferRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                offerName,
                null,
                productId,
                null
        );

        var secondUpsertOfferRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                secondOfferId,
                secondOfferName,
                null,
                productId,
                null
        );

        var newOfferNameRequest = new IngestionRequest(
                RequestOperationType.UPSERT_OFFER,
                offerId,
                newOfferName,
                null,
                productId,
                null
        );

        //when
        var upsertProductResponse = service.getBaseSearchEngineOperations(upsertProductRequest);
        var upsertOfferResponse = service.getBaseSearchEngineOperations(upsertOfferRequest);
        var upsertSecondOfferResponse = service.getBaseSearchEngineOperations(secondUpsertOfferRequest);
        var newOfferNameResponse = service.getBaseSearchEngineOperations(newOfferNameRequest);

        //then
        assertThat(upsertProductResponse.size()).isEqualTo(0);
        assertThat(upsertOfferResponse.size()).isEqualTo(1);
        assertThat(upsertSecondOfferResponse.size()).isEqualTo(1);
        assertThat(newOfferNameResponse.size()).isEqualTo(1);

        var upsertOperation = (UpsertOperation) upsertOfferResponse.getFirst();
        assertThat(upsertOperation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(upsertOperation.getProductId()).isEqualTo(productId);
        assertThat(upsertOperation.getOfferNames().getFirst()).isEqualTo(offerName);

        var secondUpsertOperation = (UpsertOperation) upsertSecondOfferResponse.getFirst();
        assertThat(secondUpsertOperation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(secondUpsertOperation.getProductId()).isEqualTo(productId);
        assertThat(secondUpsertOperation.getOfferNames().size()).isEqualTo(2);
        assertThat(secondUpsertOperation.getOfferNames()).contains(offerName, secondOfferName);

        var newOfferNameOperation = (UpsertOperation) newOfferNameResponse.getFirst();
        assertThat(newOfferNameOperation.getOperationType()).isEqualTo(SearchEngineOperationType.UPSERT_SEARCHABLE_PRODUCT);
        assertThat(newOfferNameOperation.getProductId()).isEqualTo(productId);
        assertThat(newOfferNameOperation.getOfferNames().size()).isEqualTo(2);
        assertThat(newOfferNameOperation.getOfferNames()).contains(newOfferName, secondOfferName);
    }
}
