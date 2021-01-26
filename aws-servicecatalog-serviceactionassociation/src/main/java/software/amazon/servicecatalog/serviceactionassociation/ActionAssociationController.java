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

import java.util.List;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionAssociationController {

    private static final String ASSOCIATE_EXCEPTION = "In updateHandler, AssociateServiceAction received Exception %s";
    private static final String ASSOCIATE_SERVICE_ACTION_LOG = "Associate serviceAction: %s, with provisioningArtifact: %s and product: %s";
    private static final String DISASSOCIATE_EXCEPTION = "In updateHandler, DisassociateServiceAction received Exception %s";
    private static final String DISASSOCIATE_SERVICE_ACTION_LOG = "Disassociate serviceAction: %s, from provisioningArtifact: %s and product: %s";
    private static final String RESOURCE_NOT_FOUND_EXCEPTION = "ServiceAction %s with product id %s and provisioning artifact id %s not found";

    private final Logger logger;
    private final ServiceCatalogClient scClient;
    private final AmazonWebServicesClientProxy proxy;

    private ListServiceActionsForProvisioningArtifactResponse listServiceActions(final String productId, final String provisioningArtifactId, final String pageToken) {
        ListServiceActionsForProvisioningArtifactRequest request = ListServiceActionsForProvisioningArtifactRequest.builder()
                .productId(productId)
                .provisioningArtifactId(provisioningArtifactId)
                .pageToken(pageToken)
                .build();

        return proxy.injectCredentialsAndInvokeV2(request, scClient::listServiceActionsForProvisioningArtifact);
    }

    public ResourceModel describeServiceActionAssociation(final String productId, final String provisioningArtifactId, final String serviceActionId) {
        String pageToken = null;
        do {
            final ListServiceActionsForProvisioningArtifactResponse response = listServiceActions(productId, provisioningArtifactId, pageToken);
            final List<ServiceActionSummary> serviceActions = response.serviceActionSummaries();
            pageToken = response.nextPageToken();
            boolean isMatch = serviceActions.stream().anyMatch(serviceActionSummary -> serviceActionId.equals(serviceActionSummary.id()));
            if (isMatch) {
                return ResourceModel
                        .builder()
                        .serviceActionId(serviceActionId)
                        .provisioningArtifactId(provisioningArtifactId)
                        .productId(productId)
                        .build();
            }

        } while(!StringUtils.isNullOrEmpty(pageToken));
        logger.log(String.format(RESOURCE_NOT_FOUND_EXCEPTION, serviceActionId, productId, provisioningArtifactId));
        throw ResourceNotFoundException.builder().message(String.format(RESOURCE_NOT_FOUND_EXCEPTION, serviceActionId, productId, provisioningArtifactId)).build();
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
