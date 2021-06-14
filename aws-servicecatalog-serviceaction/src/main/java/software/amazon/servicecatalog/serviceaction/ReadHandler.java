package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.DescribeServiceActionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

public class ReadHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ActionController actionController = ActionController
                .builder()
                .proxy(proxy)
                .logger(logger)
                .scClient(SCClientBuilder.getClient())
                .build();
        final ResourceModel desiredModel = request.getDesiredResourceState();
        try {
            final DescribeServiceActionResponse response = actionController.describeServiceAction(desiredModel.getId());
            final ResourceModel model = ActionController
                    .buildResourceModelFromServiceActionDetail(response.serviceActionDetail());
            return ProgressEvent.defaultSuccessHandler(model);

        } catch (SdkException e) {
            throw ExceptionTranslator.translateToCfnException(e);
        }
    }
}
