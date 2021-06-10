# AWS::ServiceCatalog::ServiceActionAssociation

Resource Schema for AWS::ServiceCatalog::ServiceActionAssociation

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ServiceCatalog::ServiceActionAssociation",
    "Properties" : {
        "<a href="#productid" title="ProductId">ProductId</a>" : <i>String</i>,
        "<a href="#provisioningartifactid" title="ProvisioningArtifactId">ProvisioningArtifactId</a>" : <i>String</i>,
        "<a href="#serviceactionid" title="ServiceActionId">ServiceActionId</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::ServiceCatalog::ServiceActionAssociation
Properties:
    <a href="#productid" title="ProductId">ProductId</a>: <i>String</i>
    <a href="#provisioningartifactid" title="ProvisioningArtifactId">ProvisioningArtifactId</a>: <i>String</i>
    <a href="#serviceactionid" title="ServiceActionId">ServiceActionId</a>: <i>String</i>
</pre>

## Properties

#### ProductId

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9][a-zA-Z0-9_-]{1,99}\Z</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ProvisioningArtifactId

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9][a-zA-Z0-9_-]{1,99}\Z</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ServiceActionId

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Pattern_: <code>^[a-zA-Z0-9][a-zA-Z0-9_-]{1,99}\Z</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

