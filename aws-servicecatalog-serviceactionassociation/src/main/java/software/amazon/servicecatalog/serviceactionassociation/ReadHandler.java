package software.amazon.servicecatalog.serviceactionassociation;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

public class ReadHandler extends BaseHandler<CallbackContext> {

    private static final String RESOURCE_NOT_FOUND_EXCEPTION = "ServiceAction %s with product id %s and provisioning artifact id %s not found";

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

        ResourceModel resourceModel = null;
        try {
            final ResourceModel desiredModel = request.getDesiredResourceState();
            final String serviceActionId = desiredModel.getServiceActionId();
            final String productId = desiredModel.getProductId();
            final String provisioningArtifactId = desiredModel.getProvisioningArtifactId();
            boolean isMatch = controller.isServiceActionAssociatedToPA(productId, provisioningArtifactId, serviceActionId);
            if(isMatch){
                resourceModel = ResourceModel
                        .builder()
                        .serviceActionId(serviceActionId)
                        .provisioningArtifactId(provisioningArtifactId)
                        .productId(productId)
                        .build();
            } else {
                logger.log(String.format(RESOURCE_NOT_FOUND_EXCEPTION, serviceActionId, productId, provisioningArtifactId));
                throw ResourceNotFoundException.builder().message(String.format(RESOURCE_NOT_FOUND_EXCEPTION, serviceActionId, productId, provisioningArtifactId)).build();
            }

        } catch (SdkException e) {
            ExceptionTranslator.translateToCfnException(e);
        }
        return ProgressEvent.defaultSuccessHandler(resourceModel);
    }
}
