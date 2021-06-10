package software.amazon.servicecatalog.serviceaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsResponse;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.awssdk.services.servicecatalog.paginators.ListServiceActionsIterable;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    final static private String INVALID_PARAMETERS_EXCEPTION = "invalid parameters exception";

    private ListHandler handler;
    private ResourceModel model;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @Mock
    private ListServiceActionsIterable iterable;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        model = ResourceModel.builder()
                .build();
    }

    private List<ServiceActionSummary> buildServiceActionSummaries() {
        List<ServiceActionSummary> serviceActionSummaries = new ArrayList<>();
        ServiceActionSummary summary = ServiceActionSummary.builder()
                .definitionType("SSM_AUTOMATION")
                .description("Start EC2 Instances")
                .id("act-1993jive")
                .name("StartEC2Instance")
                .build();
        serviceActionSummaries.add(summary);
        return serviceActionSummaries;
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ListServiceActionsResponse result = ListServiceActionsResponse.builder()
                .serviceActionSummaries(buildServiceActionSummaries())
                .build();

        Stream<ListServiceActionsResponse> stream = Stream.<ListServiceActionsResponse>builder().add(result).build();
        doReturn(iterable).when(proxy).injectCredentialsAndInvokeIterableV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());
        doReturn(stream).when(iterable).stream();

        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .desiredResourceState(model)
                .clientRequestToken("token")
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, resourceHandlerRequest, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_whenInvalidParameters_Exception() {
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
}
