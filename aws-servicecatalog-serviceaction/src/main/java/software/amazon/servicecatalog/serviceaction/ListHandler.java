package software.amazon.servicecatalog.serviceaction;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.servicecatalog.SCClientBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandler<CallbackContext> {

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
        List<ResourceModel> models = new ArrayList<>();
        try {
            final List<String> serviceActionIds = actionController.listAllServiceActionIds();
            models = buildListResourceModel(serviceActionIds);
        } catch (SdkException e) {
            ExceptionTranslator.translateToCfnException(e);
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<ResourceModel> buildListResourceModel(List<String> serviceActionIds) {
        return serviceActionIds.stream().map(actionId -> ResourceModel.builder()
                .id(actionId)
                .build()).collect(Collectors.toList());
    }
}
