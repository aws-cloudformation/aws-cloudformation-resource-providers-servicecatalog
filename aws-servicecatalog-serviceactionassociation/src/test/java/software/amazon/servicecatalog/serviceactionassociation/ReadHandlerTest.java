package software.amazon.servicecatalog.serviceactionassociation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private static final String productId = "pro-asdfgh";
    private static final String provisioningArtifactId = "pa-asdffg";
    private static final String serviceActionId = "act-dfadgasd";
    private static final String SDK_EXCEPTION = "sdk exception";

    private ReadHandler handler;
    private ResourceModel resourceModel;
    private ListServiceActionsForProvisioningArtifactRequest listRequest;
    private ListServiceActionsForProvisioningArtifactResponse response ;
    private ListServiceActionsForProvisioningArtifactResponse emptyResponse;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        resourceModel = ResourceModel
                .builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .serviceActionId(serviceActionId)
                .build();
        listRequest = ListServiceActionsForProvisioningArtifactRequest
                .builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .pageToken(null)
                .build();

        response = ListServiceActionsForProvisioningArtifactResponse
                .builder()
                .serviceActionSummaries(ImmutableList.of(
                        ServiceActionSummary.builder().id("act-fake1").build(),
                        ServiceActionSummary.builder().id("act-fake2").build(),
                        ServiceActionSummary.builder().id("act-fake3").build(),
                        ServiceActionSummary.builder().id("act-fake4").build(),
                        ServiceActionSummary.builder().id(serviceActionId).build()
                ))
                .nextPageToken(null)
                .build();

        emptyResponse = ListServiceActionsForProvisioningArtifactResponse
                .builder()
                .serviceActionSummaries(ImmutableList.of(
                        ServiceActionSummary.builder().id("act-fake1").build(),
                        ServiceActionSummary.builder().id("act-fake2").build(),
                        ServiceActionSummary.builder().id("act-fake3").build(),
                        ServiceActionSummary.builder().id("act-fake4").build()
                ))
                .nextPageToken(null)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        doReturn(response).when(proxy).injectCredentialsAndInvokeV2(
                eq(listRequest),
                ArgumentMatchers.any()
        );


        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ThrowResourceNotFoundException() {
        doReturn(emptyResponse).when(proxy).injectCredentialsAndInvokeV2(
                eq(listRequest),
                ArgumentMatchers.any()
        );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowInvalidParametersException() {
        doThrow(InvalidParametersException.builder().build()).when(proxy).injectCredentialsAndInvokeV2(
                eq(listRequest),
                ArgumentMatchers.any()
        );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // When
        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ThrowSdkException() {
        doThrow(SdkException.builder().message(SDK_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                eq(listRequest),
                ArgumentMatchers.any()
        );

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(resourceModel)
                .build();

        // When
        assertThrows(CfnInternalFailureException.class, () -> handler.handleRequest(proxy, request, null, logger));
    }
}
