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

public class CreateHandler extends BaseHandler<CallbackContext> {

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

        if (callbackContext == null) { // CREATE request
            logger.log("Invoking create handler for new resource.");
            return handleNewCreateRequest(controller, request);
        } else { // CREATE Stabilization Request
            logger.log(String.format("Invoking create handler for stabilizing resource %s", callbackContext.getServiceActionId()));
            return Stabilization.handleCreateStabilizeRequest(controller, request, callbackContext, logger);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleNewCreateRequest(final ActionAssociationController controller, final ResourceHandlerRequest<ResourceModel> request) {
        final ResourceModel desiredModel = request.getDesiredResourceState();
        try {
            controller.associateServiceAction(desiredModel.getProductId(), desiredModel.getProvisioningArtifactId(), desiredModel.getServiceActionId());
        } catch (SdkException e) {
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
