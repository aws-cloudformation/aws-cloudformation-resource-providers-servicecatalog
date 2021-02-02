package software.amazon.servicecatalog.serviceactionassociation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.AssociateServiceActionWithProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.AssociateServiceActionWithProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.DuplicateResourceException;
import software.amazon.awssdk.services.servicecatalog.model.LimitExceededException;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    final static private String PRODUCT_ID = "prod-sdfg1234";
    final static private String SERVICE_ACTION_ID = "act-12413asd";
    final static private String PROVISIONING_ARTIFACT_ID = "pa-dfergbr1232r4";
    final static private String Exception = "Service Action Association Exception";

    private CreateHandler handler;
    private ResourceModel model;
    private AssociateServiceActionWithProvisioningArtifactRequest associationRequest;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        model = ResourceModel
                .builder()
                .productId(PRODUCT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .build();
        associationRequest = AssociateServiceActionWithProvisioningArtifactRequest
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final AssociateServiceActionWithProvisioningArtifactResponse associationResponse = AssociateServiceActionWithProvisioningArtifactResponse
                .builder()
                .build();

        doReturn(associationResponse).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getCallbackContext().getProvisioningArtifactId()).isEqualTo(PROVISIONING_ARTIFACT_ID);
        assertThat(response.getCallbackContext().getServiceActionId()).isEqualTo(SERVICE_ACTION_ID);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ThrowResourceNotFoundException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        doThrow(ResourceNotFoundException.builder().message(Exception).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowSdkException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        doThrow(SdkException.builder().message(Exception).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnInternalFailureException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowDuplicateResourceException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        doThrow(DuplicateResourceException.builder().message(Exception).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnAlreadyExistsException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowLimitExceededException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        doThrow(LimitExceededException.builder().message(Exception).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnServiceLimitExceededException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_StabilizeCreateRequest_ServiceActionAssociatedToPA(){
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ListServiceActionsForProvisioningArtifactResponse listResponse = ListServiceActionsForProvisioningArtifactResponse
                .builder()
                .serviceActionSummaries(ImmutableList.of(
                        ServiceActionSummary.builder().id(SERVICE_ACTION_ID).build()
                ))
                .nextPageToken(null)
                .build();
        final CallbackContext callbackContext = CallbackContext
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .stabilizationRetriesRemaining(5)
                .build();

        doReturn(listResponse).when(proxy).injectCredentialsAndInvokeV2(any(ListServiceActionsForProvisioningArtifactRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, callbackContext, logger);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizeCreateRequest_ServiceActionNotAssociatedToPA(){
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ListServiceActionsForProvisioningArtifactResponse listResponse = ListServiceActionsForProvisioningArtifactResponse
                .builder()
                .serviceActionSummaries(ImmutableList.of(
                        ServiceActionSummary.builder().id("act-fake1").build()
                ))
                .nextPageToken(null)
                .build();
        final CallbackContext callbackContext = CallbackContext
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .stabilizationRetriesRemaining(5)
                .build();

        doReturn(listResponse).when(proxy).injectCredentialsAndInvokeV2(any(ListServiceActionsForProvisioningArtifactRequest.class), any());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, callbackContext, logger);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackContext().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getCallbackContext().getProvisioningArtifactId()).isEqualTo(PROVISIONING_ARTIFACT_ID);
        assertThat(response.getCallbackContext().getServiceActionId()).isEqualTo(SERVICE_ACTION_ID);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizeCreateRequest_ResourceNotFoundException(){
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final CallbackContext callbackContext = CallbackContext
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .stabilizationRetriesRemaining(5)
                .build();

        doThrow(ResourceNotFoundException.builder().message(Exception).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, callbackContext, logger));
    }

    @Test
    public void handle_StabilizeCreateRequest_NoRetriesRemaining() {
        // Given
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder().build();

        final CallbackContext callbackContext = CallbackContext
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .stabilizationRetriesRemaining(0)
                .build();

        // When
        assertThrows(CfnNotStabilizedException.class, () -> handler.handleRequest(proxy, request, callbackContext, logger));
    }
}
