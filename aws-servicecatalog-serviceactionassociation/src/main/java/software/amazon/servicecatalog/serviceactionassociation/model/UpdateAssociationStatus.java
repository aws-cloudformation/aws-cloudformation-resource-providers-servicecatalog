package software.amazon.servicecatalog.serviceactionassociation.model;

import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import lombok.Builder;
import lombok.Getter;
import software.amazon.servicecatalog.serviceactionassociation.ResourceModel;

@Builder
@Getter
public class UpdateAssociationStatus {
    private OperationStatus status;
    private String errorMessage;
    private ResourceModel resourceModel;
    private HandlerErrorCode errorCode;
}
