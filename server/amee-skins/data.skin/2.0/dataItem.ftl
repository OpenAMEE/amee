<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<#include '/includes/furniture.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type='text/javascript'>

    var AUTHORIZATION_CONTEXT = new AuthorizationContext(${authorizationContext.getJSONObject()});

    // create resource objects
    var ITEM_VALUE_DEFINITIONS = new ItemValueDefinitionsResource('${dataItem.itemDefinition.uid}');
    var dataItemApiService = new DataItemApiService({
        heading : "Item Values",
        headingElementName : "apiHeading",
        contentElementName : "apiContent",
        dataHeadingItem : 'Data Item Details',
        dataHeadingItemElementName : 'apiDataItemHeading',
        dataContentElementName : "apiDataItemContent",
        apiVersion : '2.0',
        updateItem: true,
        createItemValue: true});

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.addResource(ITEM_VALUE_DEFINITIONS);
    resourceLoader.observe('loaded', function() {
        dataItemApiService.start();
    });
    resourceLoader.start();

    document.observe('dom:loaded', function() {
        // hide n/a atom option
        $('showAPIATOM').style.visibility = "hidden";
    });

</script>

<h1>Data Item</h1>

<#include 'dataTrail.ftl'>

<h2 id="apiDataItemHeading"></h2>
<p id="apiDataItemContent"></p>
<h2 id="apiHeading"></h2>
<table id="apiContent"></table>
<div id="apiUpdateDataItem"></div>
<div id="apiUpdateSubmitStatus"></div><br/>
<div id="apiCreateDataItemValue"></div>
<div id="apiCreateSubmitStatus"></div>

<#include '/includes/after_content.ftl'>