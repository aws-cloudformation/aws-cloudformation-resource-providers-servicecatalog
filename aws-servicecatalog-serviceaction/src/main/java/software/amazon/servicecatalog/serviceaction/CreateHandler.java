package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.services.servicecatalog.model.CreateServiceActionResponse;
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.LimitExceededException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final String CREATE_ERROR_MSG = "Unable to create service action because: %s";

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger){

        ActionController actionController = ActionController
                .builder()
                .logger(logger)
                .proxy(proxy)
                .scClient(SCClientBuilder.getClient())
                .build();
        final ResourceModel desiredModel = request.getDesiredResourceState();
        final String idempotencyToken = request.getClientRequestToken();
        try {
            final CreateServiceActionResponse response = actionController.createServiceAction(desiredModel, idempotencyToken);
            final ResourceModel model = ActionController
                    .buildResourceModelFromServiceActionDetail(response.serviceActionDetail());
            return ProgressEvent.defaultSuccessHandler(model);

        } catch (InvalidParametersException e) {
            throw new CfnInvalidRequestException(String.format(CREATE_ERROR_MSG, e.getMessage()), e);

        } catch (LimitExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage(), e);
        }
    }
}
