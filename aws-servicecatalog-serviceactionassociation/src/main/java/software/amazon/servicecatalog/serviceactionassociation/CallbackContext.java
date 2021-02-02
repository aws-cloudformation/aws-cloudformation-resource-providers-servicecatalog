package software.amazon.servicecatalog.serviceactionassociation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackContext {
    private String serviceActionId;
    private String productId;
    private String provisioningArtifactId;
    private Integer stabilizationRetriesRemaining;
}
