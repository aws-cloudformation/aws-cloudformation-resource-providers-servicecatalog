package software.amazon.servicecatalog.serviceactionassociation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.DisassociateServiceActionFromProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.DisassociateServiceActionFromProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
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
}
