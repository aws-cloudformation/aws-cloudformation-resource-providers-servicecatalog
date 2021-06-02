package software.amazon.servicecatalog.serviceactionassociation;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

import java.util.ArrayList;
import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        ActionAssociationController controller = ActionAssociationController
                .builder()
                .logger(logger)
                .proxy(proxy)
                .scClient(SCClientBuilder.getClient())
                .build();

        final ResourceModel desiredModel = request.getDesiredResourceState();
        final String productId = desiredModel.getProductId();
        final String provisioningArtifactId = desiredModel.getProvisioningArtifactId();
        List<ResourceModel> models = new ArrayList<>();

        try {
            final List<String> serviceActionIds = controller.listServiceActionsForProvisioningArtifact(productId, provisioningArtifactId);
            models = controller.buildListResourceModel(serviceActionIds, productId, provisioningArtifactId);
        } catch (SdkException e) {
            ExceptionTranslator.translateToCfnException(e);
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
