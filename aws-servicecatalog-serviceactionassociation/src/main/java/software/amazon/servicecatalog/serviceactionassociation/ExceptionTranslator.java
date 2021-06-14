package software.amazon.servicecatalog.serviceactionassociation;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.servicecatalog.model.DuplicateResourceException;
import software.amazon.awssdk.services.servicecatalog.model.InvalidParametersException;
import software.amazon.awssdk.services.servicecatalog.model.LimitExceededException;
import software.amazon.awssdk.services.servicecatalog.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;

public class ExceptionTranslator {
    public static RuntimeException translateToCfnException(
            final SdkException e) {
        if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(ResourceModel.TYPE_NAME, e.getMessage(), e);
        }
        if (e instanceof DuplicateResourceException) {
            return new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, e.getMessage(), e);
        }
        if (e instanceof LimitExceededException) {
            return new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.getMessage(), e);
        }
        if (e instanceof InvalidParametersException) {
            return new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        }
        return new CfnInternalFailureException(e);
    }
}
