package software.amazon.servicecatalog.serviceactionassociation.stabilizer;

import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.serviceactionassociation.ActionAssociationController;
import software.amazon.servicecatalog.serviceactionassociation.CallbackContext;
import software.amazon.servicecatalog.serviceactionassociation.ResourceModel;

import static software.amazon.servicecatalog.serviceactionassociation.HandlerConstants.POLL_RETRY_DELAY_SECONDS;

/**
 * Stabilization Class for Create and Delete Handlers.
 */
public class Stabilization {
    private static final String CREATE_RETRIES = "Create service action retries remaining: %s";
    private static final String DELETE_RETRIES = "Delete service action retries remaining: %s";

    public static ProgressEvent<ResourceModel, CallbackContext> handleCreateStabilizeRequest(final ActionAssociationController actionController, final ResourceHandlerRequest<ResourceModel> request,
                                                                                             final CallbackContext callbackContext,
                                                                                             final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        if (callbackContext.getStabilizationRetriesRemaining() == 0) {
            logger.log("Create retries remaining zero");
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, callbackContext.getServiceActionId());
        }
        try {
            final boolean isMatch = actionController.isServiceActionAssociatedToPA(callbackContext.getProductId(), callbackContext.getProvisioningArtifactId(), callbackContext.getServiceActionId());
            logger.log(String.format(CREATE_RETRIES, callbackContext.getStabilizationRetriesRemaining()));
            if(isMatch){
                return ProgressEvent.defaultSuccessHandler(model);
            } else {
                return ProgressEvent.defaultInProgressHandler(
                        callbackContext.toBuilder()
                                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining() - 1)
                                .build(),
                        POLL_RETRY_DELAY_SECONDS,
                        model);
            }
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, callbackContext.getServiceActionId(), ex);
        }
    }

    public static ProgressEvent<ResourceModel, CallbackContext> handleDeleteStabilizeRequest(final ActionAssociationController actionController, final ResourceHandlerRequest<ResourceModel> request,
                                                                                             final CallbackContext callbackContext,
                                                                                             final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        if (callbackContext.getStabilizationRetriesRemaining() == 0) {
            logger.log("Delete retries remaining zero");
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, callbackContext.getServiceActionId());
        }
        try {
            final boolean isMatch = actionController.isServiceActionAssociatedToPA(callbackContext.getProductId(), callbackContext.getProvisioningArtifactId(), callbackContext.getServiceActionId());
            logger.log(String.format(DELETE_RETRIES, callbackContext.getStabilizationRetriesRemaining()));
            if(isMatch){
                return ProgressEvent.defaultInProgressHandler(
                        callbackContext.toBuilder()
                                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining() - 1)
                                .build(),
                        POLL_RETRY_DELAY_SECONDS,
                        model);
            } else {
                return ProgressEvent.defaultSuccessHandler(null);
            }
        } catch (ResourceNotFoundException ex) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, callbackContext.getServiceActionId(), ex);
        }
    }
}
