package software.amazon.servicecatalog.serviceactionassociation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import software.amazon.awssdk.services.servicecatalog.model.DisassociateServiceActionFromProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.DisassociateServiceActionFromProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private static final String SERVICE_ACTION_ID = "act-1234asdf";
    private static final String PRODUCT_ID = "pro-fdger1241";
    private static final String PROVISIONING_ARTIFACT_ID = "pa-asdge1341";
    private static final String SDK_EXCEPTION = "sdk exception";

    private DisassociateServiceActionFromProvisioningArtifactRequest disassociateRequest;
    private DeleteHandler handler;
    private ResourceModel model;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new DeleteHandler();
        disassociateRequest = DisassociateServiceActionFromProvisioningArtifactRequest
                .builder()
                .productId(PRODUCT_ID)
                .provisioningArtifactId(PROVISIONING_ARTIFACT_ID)
                .serviceActionId(SERVICE_ACTION_ID)
                .build();
        model = ResourceModel
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
        final DisassociateServiceActionFromProvisioningArtifactResponse result = DisassociateServiceActionFromProvisioningArtifactResponse
                .builder()
                .build();
        doReturn(result).when(proxy).injectCredentialsAndInvokeV2(
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
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ThrowResourceNotFoundException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(ResourceNotFoundException.builder().message(SDK_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                eq(disassociateRequest),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowOtherException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        doThrow(SdkException.builder().message(SDK_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                eq(disassociateRequest),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnInternalFailureException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_StabilizeDeleteRequest_ServiceActionAssociatedToPA(){
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
    public void handleRequest_StabilizeDeleteRequest_ServiceActionNotAssociatedToPA(){
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
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_StabilizeDeleteRequest_ResourceNotFoundException(){
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

        doThrow(ResourceNotFoundException.builder().message(SDK_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, callbackContext, logger));
    }

    @Test
    public void handle_StabilizeDeleteRequest_NoRetriesRemaining() {
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
