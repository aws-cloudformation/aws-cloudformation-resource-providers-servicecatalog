package software.amazon.servicecatalog.serviceactionassociation;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;
import software.amazon.servicecatalog.SCClientBuilder;

public class CreateHandler extends BaseHandler<CallbackContext> {

    private static final int MAX_LENGTH_CONFIGURATION_SET_ID = 64;

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
        final ResourceModel desiredModel = getDesiredModelWithPrimaryIdentifier(request);
        try {
            controller.associateServiceAction(desiredModel.getProductId(), desiredModel.getProvisioningArtifactId(), desiredModel.getServiceActionId());

        } catch (SdkException e) {
            ExceptionTranslator.translateToCfnException(e);
        }
        return ProgressEvent.defaultSuccessHandler(desiredModel);
    }

    private ResourceModel getDesiredModelWithPrimaryIdentifier(ResourceHandlerRequest<ResourceModel> request){
        ResourceModel desiredModel = request.getDesiredResourceState();
        if (StringUtils.isNullOrEmpty(desiredModel.getId())) {
            desiredModel.setId(IdentifierUtils.generateResourceIdentifier(
                    request.getLogicalResourceIdentifier(),
                    request.getClientRequestToken(),
                    MAX_LENGTH_CONFIGURATION_SET_ID
            ));
        }
        return desiredModel;
    }
}
