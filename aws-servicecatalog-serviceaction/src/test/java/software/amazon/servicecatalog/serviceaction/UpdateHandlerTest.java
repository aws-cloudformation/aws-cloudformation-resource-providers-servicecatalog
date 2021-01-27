package software.amazon.servicecatalog.serviceaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.List;

import com.google.common.collect.ImmutableList;
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionDetail;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.awssdk.services.servicecatalog.model.UpdateServiceActionResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    final static private String INVALID_PARAMETERS_EXCEPTION = "invalid parameters exception";
    final static private String RESOURCE_NOT_FOUND_EXCEPTION = "Resource not found exception";
    final static private String PREVIOUS_DOCUMENT = "AWS-StartEC2Instance";
    final static private String DESIRED_DOCUMENT = "AWS-StartEC2Instance";

    private UpdateHandler handler;
    private ResourceModel previousModel;
    private ResourceModel desiredModel;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        previousModel = buildResourceModel("StartEC2Instance", PREVIOUS_DOCUMENT);
        desiredModel = buildResourceModel("StopEC2Instance", DESIRED_DOCUMENT);
    }

    private List<DefinitionParameter> buildDefinitionParameters(final String documentName) {
        return ImmutableList.of(
                DefinitionParameter.builder().key("Name").value(documentName).build(),
                DefinitionParameter.builder().key("Version").value("1").build(),
                DefinitionParameter.builder().key("AssumeRole").value("arn:aws:iam::123456789012:role/role").build(),
                DefinitionParameter.builder().key("Parameters").value("[{\\\"Name\\\":\\\"InstanceId\\\",\\\"Type\\\":\\\"TARGET\\\"}]").build()
        );
    }

    private ResourceModel buildResourceModel(final String resourceName, final String documentName) {
        return ResourceModel.builder()
                .id("act-1993jive")
                .name(resourceName)
                .definitionType("SSM_AUTOMATION")
                .definition(buildDefinitionParameters(documentName))
                .description(resourceName)
                .build();
    }

    private ServiceActionSummary buildServiceActionSummary() {
        return ServiceActionSummary.builder()
                .definitionType("SSM_AUTOMATION")
                .description("StopEC2Instance")
                .id("act-1993jive")
                .name("StopEC2Instance")
                .build();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ServiceActionSummary serviceActionSummary = buildServiceActionSummary();
        final ServiceActionDetail serviceActionDetail = ServiceActionDetail
                .builder()
                .serviceActionSummary(serviceActionSummary)
                .build();
        final UpdateServiceActionResponse result = UpdateServiceActionResponse
                .builder()
                .serviceActionDetail(serviceActionDetail)
                .build();
        doReturn(result).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .previousResourceState(previousModel)
                .desiredResourceState(desiredModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, resourceHandlerRequest, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isZero();
        assertThat(response.getResourceModel().getId()).isEqualTo(desiredModel.getId());
        assertThat(response.getResourceModel().getName()).isEqualTo(desiredModel.getName());
        assertThat(response.getResourceModel().getDefinitionType()).isEqualTo(desiredModel.getDefinitionType());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_WhenInvalidParameters_Exception() {
        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        doThrow(InvalidParametersException.builder().message(INVALID_PARAMETERS_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, resourceHandlerRequest, null, logger));
    }

    @Test
    public void handleRequest_WhenNotFound_Exception() {
        final ResourceHandlerRequest<ResourceModel> resourceHandlerRequest = ResourceHandlerRequest
                .<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        doThrow(ResourceNotFoundException.builder().message(RESOURCE_NOT_FOUND_EXCEPTION).build()).when(proxy).injectCredentialsAndInvokeV2(
                ArgumentMatchers.any(),
                ArgumentMatchers.any());

        // When
        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, resourceHandlerRequest, null, logger));
    }
}
