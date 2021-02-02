package software.amazon.servicecatalog.serviceactionassociation;

import static software.amazon.servicecatalog.serviceactionassociation.HandlerConstants.NUMBER_OF_STATE_POLL_RETRIES;
import static software.amazon.servicecatalog.serviceactionassociation.HandlerConstants.POLL_RETRY_DELAY_SECONDS;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;
import software.amazon.servicecatalog.serviceactionassociation.stabilizer.Stabilization;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        ActionAssociationController controller = ActionAssociationController
                .builder()
                .scClient(SCClientBuilder.getClient())
                .proxy(proxy)
                .logger(logger)
                .build();

        if (callbackContext == null) { // DELETE request
            logger.log("Invoking Delete handler for new resource.");
            return handleNewDeleteRequest(controller, request);
        } else { // Delete Stabilization Request
            logger.log(String.format("Invoking Delete handler for stabilizing resource %s", callbackContext.getServiceActionId()));
            return Stabilization.handleDeleteStabilizeRequest(controller, request, callbackContext, logger);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNewDeleteRequest(final ActionAssociationController controller, final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel desiredModel = request.getDesiredResourceState();
        try {
            controller.disassociateServiceAction(desiredModel.getProductId(), desiredModel.getProvisioningArtifactId(), desiredModel.getServiceActionId());
        } catch(SdkException e) {
            ExceptionTranslator.translateToCfnException(e);
        }
        return ProgressEvent.defaultInProgressHandler(CallbackContext.builder()
                        .productId(desiredModel.getProductId())
                        .provisioningArtifactId(desiredModel.getProvisioningArtifactId())
                        .serviceActionId(desiredModel.getServiceActionId())
                        .stabilizationRetriesRemaining(NUMBER_OF_STATE_POLL_RETRIES)
                        .build(),
                POLL_RETRY_DELAY_SECONDS,
                desiredModel);
    }
}
