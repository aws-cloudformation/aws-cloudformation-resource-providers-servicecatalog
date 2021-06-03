package software.amazon.servicecatalog.serviceactionassociation;

import com.amazonaws.util.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClient;
import software.amazon.awssdk.services.servicecatalog.model.AssociateServiceActionWithProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.DisassociateServiceActionFromProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.DuplicateResourceException;
import software.amazon.awssdk.services.servicecatalog.model.LimitExceededException;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactRequest;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsForProvisioningArtifactResponse;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.servicecatalog.serviceactionassociation.model.UpdateAssociationStatus;

import java.util.ArrayList;
import java.util.List;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionAssociationController {

    private static final String ASSOCIATE_EXCEPTION = "In updateHandler, AssociateServiceAction received Exception %s";
    private static final String ASSOCIATE_SERVICE_ACTION_LOG = "Associate serviceAction: %s, with provisioningArtifact: %s and product: %s";
    private static final String DISASSOCIATE_EXCEPTION = "In updateHandler, DisassociateServiceAction received Exception %s";
    private static final String DISASSOCIATE_SERVICE_ACTION_LOG = "Disassociate serviceAction: %s, from provisioningArtifact: %s and product: %s";
    private static final String RESOURCE_NOT_FOUND_EXCEPTION = "ServiceAction %s with product id %s and provisioning artifact id %s not found";
    private static final String LIST_SERVICE_ACTION_LOG = "List service action associated to provisioningArtifact: %s of product: %s";
    private static final String SERVICE_ACTION_ASSOCIATED_TO_PA = "Service action: %s associated to provisioningArtifact: %s of product: %s";
    private static final String SERVICE_ACTION_NOT_ASSOCIATED_TO_PA = "Service action: %s not associated to provisioningArtifact: %s of product: %s";
    private static final String LIST_ALL_SERVICE_ACTION_LOG = "List all service actions associated to provisioningArtifact: %s of product: %s";

    private final Logger logger;
    private final ServiceCatalogClient scClient;
    private final AmazonWebServicesClientProxy proxy;

    private ListServiceActionsForProvisioningArtifactResponse listServiceActions(final String productId, final String provisioningArtifactId, final String pageToken) {
        ListServiceActionsForProvisioningArtifactRequest request = ListServiceActionsForProvisioningArtifactRequest.builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .pageToken(pageToken)
                .build();

        logger.log(String.format(LIST_SERVICE_ACTION_LOG, provisioningArtifactId, productId));
        return proxy.injectCredentialsAndInvokeV2(request, scClient::listServiceActionsForProvisioningArtifact);
    }

    public boolean isServiceActionAssociatedToPA(final String productId, final String provisioningArtifactId, final String serviceActionId) {
        String pageToken = null;
        do {
            final ListServiceActionsForProvisioningArtifactResponse response = listServiceActions(productId, provisioningArtifactId, pageToken);
            final List<ServiceActionSummary> serviceActions = response.serviceActionSummaries();
            pageToken = response.nextPageToken();
            if (serviceActions.stream().anyMatch(serviceActionSummary -> serviceActionId.equals(serviceActionSummary.id()))) {
                logger.log(String.format(SERVICE_ACTION_ASSOCIATED_TO_PA, serviceActionId, provisioningArtifactId, productId));
                return true;
            }
        } while(!StringUtils.isNullOrEmpty(pageToken));
        logger.log(String.format(SERVICE_ACTION_NOT_ASSOCIATED_TO_PA, serviceActionId, provisioningArtifactId, productId));
        return false;
    }

    public void associateServiceAction(final String productId, final String provisioningArtifactId, final String serviceActionId) {
        final AssociateServiceActionWithProvisioningArtifactRequest request = AssociateServiceActionWithProvisioningArtifactRequest
                .builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .serviceActionId(serviceActionId)
                .build();
        logger.log(String.format(ASSOCIATE_SERVICE_ACTION_LOG, serviceActionId, provisioningArtifactId, productId));
        proxy.injectCredentialsAndInvokeV2(request, scClient::associateServiceActionWithProvisioningArtifact);
    }

    public void disassociateServiceAction(final String productId, final String provisioningArtifactId, final String serviceActionId) {
        final DisassociateServiceActionFromProvisioningArtifactRequest request = DisassociateServiceActionFromProvisioningArtifactRequest
                .builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .serviceActionId(serviceActionId)
                .build();
        logger.log(String.format(DISASSOCIATE_SERVICE_ACTION_LOG, serviceActionId, provisioningArtifactId, productId));
        proxy.injectCredentialsAndInvokeV2(request, scClient::disassociateServiceActionFromProvisioningArtifact);
    }

    public List<String> listServiceActionsForProvisioningArtifact(final String productId, final String provisioningArtifactId) {
        String pageToken = null;
        List<String> serviceActionIds = new ArrayList<>();
        do {
            final ListServiceActionsForProvisioningArtifactRequest request = ListServiceActionsForProvisioningArtifactRequest.builder()
                    .productId(productId)
                    .provisioningArtifactId(provisioningArtifactId)
                    .pageToken(pageToken)
                    .build();
            logger.log(String.format(LIST_ALL_SERVICE_ACTION_LOG, provisioningArtifactId, productId));
            ListServiceActionsForProvisioningArtifactResponse response = proxy.injectCredentialsAndInvokeV2(request, scClient::listServiceActionsForProvisioningArtifact);
            List<ServiceActionSummary> serviceActionSummaries = response.serviceActionSummaries();
            pageToken = response.nextPageToken();
            for (ServiceActionSummary serviceActionSummary: serviceActionSummaries) {
                serviceActionIds.add(serviceActionSummary.id());
            }
        } while(!StringUtils.isNullOrEmpty(pageToken));

        return serviceActionIds;
    }

    public List<ResourceModel> buildListResourceModel(List<String> serviceActionIds, final String productId, final String provisioningArtifactId) {
        final List<ResourceModel> models = new ArrayList<>();
        for(String actionId : serviceActionIds){
            ResourceModel resourceModel = ResourceModel.builder()
                    .productId(productId)
                    .provisioningArtifactId(provisioningArtifactId)
                    .serviceActionId(actionId)
                    .build();
            models.add(resourceModel);
        }
        return models;
    }

    public UpdateAssociationStatus updateServiceActionAssociation(final ResourceModel previousModel, final ResourceModel desiredModel) {
        try {
            disassociateServiceAction(previousModel.getProductId(), previousModel.getProvisioningArtifactId(), previousModel.getServiceActionId());
        } catch (ResourceNotFoundException e) {
            logger.log(String.format(DISASSOCIATE_EXCEPTION, e.getMessage()));
        } catch (SdkException e) {
            logger.log(String.format(DISASSOCIATE_EXCEPTION, e.getMessage()));
            return UpdateAssociationStatus
                    .builder()
                    .resourceModel(previousModel)
                    .errorMessage(e.getMessage())
                    .status(OperationStatus.FAILED)
                    .errorCode(HandlerErrorCode.NotStabilized)
                    .build();
        }
        try {
            associateServiceAction(desiredModel.getProductId(), desiredModel.getProvisioningArtifactId(), desiredModel.getServiceActionId());
            return UpdateAssociationStatus
                    .builder()
                    .resourceModel(desiredModel)
                    .status(OperationStatus.SUCCESS)
                    .build();
        }  catch (SdkException e) {
            logger.log(String.format(ASSOCIATE_EXCEPTION, e.getMessage()));
            HandlerErrorCode handlerErrorCode = HandlerErrorCode.NotStabilized;
            if (e instanceof ResourceNotFoundException) {
                handlerErrorCode = HandlerErrorCode.NotFound;
            } else if (e instanceof  DuplicateResourceException) {
                handlerErrorCode = HandlerErrorCode.AlreadyExists;
            } else if (e instanceof LimitExceededException) {
                handlerErrorCode = HandlerErrorCode.ServiceLimitExceeded;
            }
            return UpdateAssociationStatus
                    .builder()
                    .resourceModel(ResourceModel.builder().build())
                    .errorMessage(e.getMessage())
                    .status(OperationStatus.FAILED)
                    .errorCode(handlerErrorCode)
                    .build();
        }
    }
}
