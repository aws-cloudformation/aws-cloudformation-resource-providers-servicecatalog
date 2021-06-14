package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.CreateServiceActionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

public class CreateHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger){

        final ActionController actionController = ActionController
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

        } catch (SdkException e) {
            throw ExceptionTranslator.translateToCfnException(e);
        }
    }
}
