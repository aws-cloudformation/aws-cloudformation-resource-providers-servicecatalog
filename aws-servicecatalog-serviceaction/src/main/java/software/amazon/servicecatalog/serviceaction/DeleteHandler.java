package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.services.servicecatalog.model.ResourceInUseException;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

public class DeleteHandler extends BaseHandler<CallbackContext> {

    private static final String RESOURCE_IN_USE_ERROR = "Cannot delete resource %s because: %s";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ActionController actionController = ActionController
                .builder()
                .logger(logger)
                .proxy(proxy)
                .scClient(SCClientBuilder.getClient())
                .build();

        final ResourceModel desiredModel = request.getDesiredResourceState();

        try {
            actionController.deleteServiceAction(desiredModel.getId());
            return ProgressEvent.defaultSuccessHandler(null);

        } catch (ResourceInUseException e) {
            throw new CfnServiceInternalErrorException(String.format(RESOURCE_IN_USE_ERROR, desiredModel.getId(), e.getMessage()), e);

        } catch (ResourceNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, e.getMessage(), e);

        }
    }
}
