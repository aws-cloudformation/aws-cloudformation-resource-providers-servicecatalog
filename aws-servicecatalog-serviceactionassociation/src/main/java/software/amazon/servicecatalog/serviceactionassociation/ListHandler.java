package software.amazon.servicecatalog.serviceactionassociation;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ActionAssociationController controller = ActionAssociationController
                .builder()
                .logger(logger)
                .proxy(proxy)
                .scClient(SCClientBuilder.getClient())
                .build();

        final ResourceModel desiredModel = request.getDesiredResourceState();
        final String productId = desiredModel.getProductId();
        final String provisioningArtifactId = desiredModel.getProvisioningArtifactId();

        try {
            final List<String> serviceActionIds = controller.listAllServiceActionIdsForProvisioningArtifact(productId, provisioningArtifactId);
            final List<ResourceModel> models = buildListResourceModel(serviceActionIds, productId, provisioningArtifactId);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (SdkException e) {
            throw ExceptionTranslator.translateToCfnException(e);
        }
    }

    private List<ResourceModel> buildListResourceModel(List<String> serviceActionIds, final String productId, final String provisioningArtifactId) {
        return serviceActionIds.stream().map(actionId -> ResourceModel.builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .serviceActionId(actionId)
                .build()).collect(Collectors.toList());
    }
}
