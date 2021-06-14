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

        final ActionAssociationController controller = ActionAssociationController
                .builder()
                .logger(logger)
                .proxy(proxy)
                .scClient(SCClientBuilder.getClient())
                .build();

        try {
            final ResourceModel desiredModel = request.getDesiredResourceState();
            final String serviceActionId = desiredModel.getServiceActionId();
            final String productId = desiredModel.getProductId();
            final String provisioningArtifactId = desiredModel.getProvisioningArtifactId();
            final boolean isMatch = controller.isServiceActionAssociatedToPA(productId, provisioningArtifactId, serviceActionId);
            if(isMatch){
                ResourceModel resourceModel = ResourceModel
                        .builder()
                        .serviceActionId(serviceActionId)
                        .provisioningArtifactId(provisioningArtifactId)
                        .productId(productId)
                        .build();
                return ProgressEvent.defaultSuccessHandler(resourceModel);
            } else {
                throw ResourceNotFoundException.builder().message(String.format(RESOURCE_NOT_FOUND_EXCEPTION, serviceActionId, productId, provisioningArtifactId)).build();
            }
        } catch (SdkException e) {
            throw ExceptionTranslator.translateToCfnException(e);
        }
    }
}
