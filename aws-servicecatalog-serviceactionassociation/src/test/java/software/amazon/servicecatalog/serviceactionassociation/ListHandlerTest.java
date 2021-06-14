package software.amazon.servicecatalog.serviceactionassociation;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.awssdk.services.servicecatalog.paginators.ListServiceActionsForProvisioningArtifactIterable;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {
    final static private String INVALID_PARAMETERS_EXCEPTION = "invalid parameters exception";
    private static final String productId = "pro-asdfgh";
    private static final String provisioningArtifactId = "pa-asdffg";

    private ListHandler handler;
    private ResourceModel model;
    private ListServiceActionsForProvisioningArtifactRequest listRequest;
    private ListServiceActionsForProvisioningArtifactResponse response ;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ListServiceActionsForProvisioningArtifactIterable iterable;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        model = ResourceModel
                .builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
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
                        ServiceActionSummary.builder().id("act-fake4").build()
                ))
                .nextPageToken(null)
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        Stream<ListServiceActionsForProvisioningArtifactResponse> stream = Stream.<ListServiceActionsForProvisioningArtifactResponse>builder()
                .add(response)
                .build();
        doReturn(iterable).when(proxy).injectCredentialsAndInvokeIterableV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());
        doReturn(stream).when(iterable).stream();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_WhenInvalidParameters_Exception() {
        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken("token")
                .build();

        doThrow(InvalidParametersException.builder().message(INVALID_PARAMETERS_EXCEPTION).build())
                .when(proxy).injectCredentialsAndInvokeIterableV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, resourceHandlerRequest, null, logger));
    }

    @Test
    public void handleRequest_whenResourceNotFound_Exception() {
        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken("token")
                .build();

        doThrow(ResourceNotFoundException.builder().message(INVALID_PARAMETERS_EXCEPTION).build())
                .when(proxy).injectCredentialsAndInvokeIterableV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, resourceHandlerRequest, null, logger));
    }
}
