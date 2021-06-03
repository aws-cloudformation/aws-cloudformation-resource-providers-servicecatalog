package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {
    private static final String LIST_ERROR_MSG = "Unable to list service action because: %s";

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
        try {
            final List<String> serviceActionIds = actionController.listServiceAction();
            final List<ResourceModel> models = ActionController
                    .buildListResourceModel(serviceActionIds);
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(models)
                    .status(OperationStatus.SUCCESS)
                    .build();

        } catch (InvalidParametersException e) {
            throw new CfnInvalidRequestException(String.format(LIST_ERROR_MSG, e.getMessage()), e);
        }
    }
}
