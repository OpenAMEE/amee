<#include '/dataCommon.ftl'>
<#include '/includes/before_content.ftl'>

<script src="/scripts/amee/api_service.js" type="text/javascript"></script>
<script src="/scripts/amee/data_service.js" type="text/javascript"></script>

<script type='text/javascript'>

    // create resource objects
    var DATA_ACTIONS = new ActionsResource({path: '/data/actions'});
    var dataItemValueApiService = new DataItemValueApiService({
        dataHeadingItem : 'Data Item Value Details',
        dataHeadingItemElementName : 'apiDataItemHeading',
        dataContentElementName : "apiDataItemContent",
        apiVersion : '2.0',
        updateItem: true});

    // use resource loader to load resources and notify on loaded
    var resourceLoader = new ResourceLoader();
    resourceLoader.addResource(DATA_ACTIONS);
    resourceLoader.observe('loaded', function() {
        dataItemValueApiService.start();
    });
    resourceLoader.start();

    document.observe('dom:loaded', function() {
        // hide n/a atom option
        $('showAPIATOM').style.visibility = "hidden";
    });

</script>

<h1>Data Item Value</h1>

<#include 'dataTrail.ftl'>

<h2 id="apiDataItemHeading"></h2>
<p id="apiDataItemContent"></p>
<div id="apiUpdateDataItemValue"></div>
<div id="apiUpdateSubmitStatus"></div><br/>

<#include '/includes/after_content.ftl'>