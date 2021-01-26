# AWS::ServiceCatalog::ServiceAction

Resource Schema for AWS::ServiceCatalog::ServiceAction

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::ServiceCatalog::ServiceAction",
    "Properties" : {
        "<a href="#acceptlanguage" title="AcceptLanguage">AcceptLanguage</a>" : <i>String</i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::ServiceCatalog::ServiceAction
Properties:
    <a href="#acceptlanguage" title="AcceptLanguage">AcceptLanguage</a>: <i>String</i>
</pre>

## Properties

#### AcceptLanguage

_Required_: No

_Type_: String

_Allowed Values_: <code>en</code> | <code>jp</code> | <code>zh</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Id.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

Returns the <code>Id</code> value.

#### Name

Returns the <code>Name</code> value.

#### DefinitionType

Returns the <code>DefinitionType</code> value.

#### Definition

Returns the <code>Definition</code> value.

#### Description

Returns the <code>Description</code> value.

