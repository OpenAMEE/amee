<#include 'dataCommon.ftl'>
<#include '/includes/before_content.ftl'>
<#include '/includes/furniture.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type='text/javascript'>

    document.observe('dom:loaded', function() {
        // hide n/a atom option
        $('showAPIATOM').style.visibility = "hidden";
        var dataItemApiService = new DataItemApiService({
            heading : "Item Values",
            headingElementName : "apiHeading",
            contentElementName : "apiContent",
            dataHeadingItem : 'Data Item Details',
            dataHeadingItemElementName : 'apiDataItemHeading',
            dataContentElementName : "apiDataItemContent",
            apiVersion : '2.0',
            drillDown : false,
            updateItem: true
        });
        dataItemApiService.apiRequest();
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

<#include '/includes/after_content.ftl'>