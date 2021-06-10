package software.amazon.servicecatalog.serviceaction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import software.amazon.awssdk.services.servicecatalog.ServiceCatalogClient;
import software.amazon.awssdk.services.servicecatalog.model.CreateServiceActionRequest;
import software.amazon.awssdk.services.servicecatalog.model.CreateServiceActionResponse;
import software.amazon.awssdk.services.servicecatalog.model.DeleteServiceActionRequest;
import software.amazon.awssdk.services.servicecatalog.model.DescribeServiceActionRequest;
import software.amazon.awssdk.services.servicecatalog.model.DescribeServiceActionResponse;
import software.amazon.awssdk.services.servicecatalog.model.ListServiceActionsRequest;
import software.amazon.awssdk.services.servicecatalog.paginators.ListServiceActionsIterable;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionDetail;
import software.amazon.awssdk.services.servicecatalog.model.ServiceActionSummary;
import software.amazon.awssdk.services.servicecatalog.model.UpdateServiceActionRequest;
import software.amazon.awssdk.services.servicecatalog.model.UpdateServiceActionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ActionController {

    private static final String CREATE_SERVICE_ACTION_LOG = "Create serviceAction with Name: %s";
    private static final String DELETE_SERVICE_ACTION_LOG = "Delete serviceAction with Id: %s";
    private static final String UPDATE_SERVICE_ACTION_LOG = "Update serviceAction with Id: %s";
    private static final String DESCRIBE_SERVICE_ACTION_LOG = "Describe serviceAcion with id: %s";
    private static final String LIST_SERVICE_ACTIONS_LOG = "Listing all serviceAcions";

    private final Logger logger;
    private final ServiceCatalogClient scClient;
    private final AmazonWebServicesClientProxy proxy;

    public void deleteServiceAction(final String id) {
        final DeleteServiceActionRequest request = DeleteServiceActionRequest
                .builder()
                .id(id)
                .build();
        logger.log(String.format(DELETE_SERVICE_ACTION_LOG, id));
        proxy.injectCredentialsAndInvokeV2(request, scClient::deleteServiceAction);
    }

    public CreateServiceActionResponse createServiceAction(final ResourceModel desiredModel, final String idempotencyToken) {
        final Map<String, String> serviceActionDefinition = buildServiceActionDefinition(desiredModel.getDefinition());
        final CreateServiceActionRequest request = CreateServiceActionRequest
                .builder()
                .definitionWithStrings(serviceActionDefinition)
                .definitionType(desiredModel.getDefinitionType())
                .description(desiredModel.getDescription())
                .idempotencyToken(idempotencyToken)
                .name(desiredModel.getName())
                .build();
        logger.log(String.format(CREATE_SERVICE_ACTION_LOG, desiredModel.getName()));
        return proxy.injectCredentialsAndInvokeV2(request, scClient::createServiceAction);
    }

    public UpdateServiceActionResponse updateServiceAction(final ResourceModel model) {
        final Map<String, String> serviceActionDefinition = buildServiceActionDefinition(model.getDefinition());
        final UpdateServiceActionRequest request = UpdateServiceActionRequest
                .builder()
                .id(model.getId())
                .name(model.getName())
                .definitionWithStrings(serviceActionDefinition)
                .description(model.getDescription())
                .build();
        logger.log(String.format(UPDATE_SERVICE_ACTION_LOG, model.getId()));
        return proxy.injectCredentialsAndInvokeV2(request, scClient::updateServiceAction);
    }

    public DescribeServiceActionResponse describeServiceAction(final String id) {
        final DescribeServiceActionRequest request = DescribeServiceActionRequest
                .builder()
                .id(id)
                .build();

        logger.log(String.format(DESCRIBE_SERVICE_ACTION_LOG, id));
        return proxy.injectCredentialsAndInvokeV2(request, scClient::describeServiceAction);
    }

    public List<String> listAllServiceActionIds() {
        final ListServiceActionsRequest request = ListServiceActionsRequest
                .builder()
                .pageToken(null)
                .build();
        logger.log(LIST_SERVICE_ACTIONS_LOG);
        final ListServiceActionsIterable responses = proxy.injectCredentialsAndInvokeIterableV2(request, scClient::listServiceActionsPaginator);
        return responses.stream()
                .flatMap(r -> r.serviceActionSummaries().stream())
                .map(ServiceActionSummary::id)
                .collect(Collectors.toList());
    }

    private Map<String, String> buildServiceActionDefinition(final List<DefinitionParameter> definitions) {
        return definitions
                .stream()
                .collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    private static List<DefinitionParameter> buildResourceModelDefinition(final Map<String, String> definitions) {
        return definitions
                .entrySet()
                .stream()
                .map(definition -> DefinitionParameter
                        .builder()
                        .key(definition.getKey())
                        .value(definition.getValue())
                        .build()
                ).collect(Collectors.toList());
    }

    static public ResourceModel buildResourceModelFromServiceActionDetail(final ServiceActionDetail serviceActionDetail) {
        final ServiceActionSummary serviceActionSummary = serviceActionDetail.serviceActionSummary();
        return ResourceModel
                .builder()
                .name(serviceActionSummary.name())
                .definitionType(serviceActionSummary.definitionTypeAsString())
                .definition(buildResourceModelDefinition(serviceActionDetail.definitionAsStrings()))
                .description(serviceActionSummary.description())
                .id(serviceActionSummary.id())
                .build();
    }
}
